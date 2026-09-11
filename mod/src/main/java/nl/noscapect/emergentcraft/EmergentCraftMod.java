package nl.noscapect.emergentcraft;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class EmergentCraftMod implements ModInitializer {
    public static final String MOD_ID = "emergentcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final long STUCK_TICKS = 60, MOVE_TIMEOUT_TICKS = 400;
    private static final double ARRIVAL_DISTANCE_SQR = 2.25;
    private final AgentRegistry registry = new AgentRegistry();
    private final BrainGateway brain = new BrainGateway();
    private final Map<UUID, AgentRuntime> runtimes = new HashMap<>();

    @Override public void onInitialize() {
        registerCommands(); ServerLifecycleEvents.SERVER_STARTED.register(this::restoreEmbodiments); ServerTickEvents.END_SERVER_TICK.register(this::tick);
        LOGGER.info("[EmergentCraft] First Steps initialized; brain is loopback-only by default.");
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> dispatcher.register(Commands.literal("ec").requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
            .then(Commands.literal("spawn").then(Commands.argument("name", StringArgumentType.word()).executes(c -> { String name=StringArgumentType.getString(c,"name"); AgentRecord r=registry.get(name).orElseGet(() -> registry.create(name)); spawn(r,c.getSource().getServer(),c.getSource().getPlayerOrException()); c.getSource().sendSuccess(() -> Component.literal("EmergentCraft inhabitant "+name+" embodied."),false); return 1; })))
            .then(Commands.literal("list").executes(c -> { c.getSource().sendSuccess(() -> Component.literal(registry.all().stream().map(r -> r.name()+" ["+(r.alive()?"alive":"dead")+"]").reduce("Inhabitants: ",(a,b)->a+" "+b)),false); return 1; }))
            .then(Commands.literal("inspect").then(Commands.argument("name",StringArgumentType.word()).executes(c -> { AgentRecord r=require(StringArgumentType.getString(c,"name")); c.getSource().sendSuccess(() -> Component.literal(r+" runtime="+runtime(r).describeState()),false); return 1; })))
            .then(Commands.literal("pause").then(Commands.argument("name",StringArgumentType.word()).executes(c -> { AgentRecord r=require(StringArgumentType.getString(c,"name")); cancelRuntime(c.getSource().getServer(),r); registry.put(r.withPaused(true)); return 1; })))
            .then(Commands.literal("resume").then(Commands.argument("name",StringArgumentType.word()).executes(c -> { AgentRecord r=require(StringArgumentType.getString(c,"name")); registry.put(r.withPaused(false)); return 1; })))
            .then(Commands.literal("movetest").then(Commands.argument("name",StringArgumentType.word()).executes(c -> {
                AgentRecord r=require(StringArgumentType.getString(c,"name")); cancelRuntime(c.getSource().getServer(),r); Optional<GoalLessVillager> body=controlledBody(c.getSource().getServer(),r); AgentRuntime runtime=runtime(r);
                if(body.isEmpty()) { c.getSource().sendFailure(Component.literal(r.name()+" movement test failed: controlled body unavailable")); return 0; }
                runtime.lifecycle.beginAdminActionForcefully();
                Optional<Vec3> target=findReachableTestTarget(body.get());
                if(target.isEmpty() || !startMove(r,body.get(),runtime,"movetest",r.epoch(),target.get(),true)) { runtime.lifecycle.finishAction(); c.getSource().sendFailure(Component.literal(r.name()+" movement test failed: no path")); return 0; }
                c.getSource().sendSuccess(() -> Component.literal(r.name()+" movement test started"),false); return 1;
            })))
            .then(Commands.literal("remove").then(Commands.argument("name",StringArgumentType.word()).executes(c -> { String n=StringArgumentType.getString(c,"name"); registry.get(n).ifPresent(r -> { cancelRuntime(c.getSource().getServer(),r); entity(c.getSource().getServer(),r).ifPresent(Entity::discard); runtimes.remove(r.id()); }); registry.remove(n); return 1; })))
            .then(Commands.literal("revive").then(Commands.argument("name",StringArgumentType.word()).executes(c -> { AgentRecord old=require(StringArgumentType.getString(c,"name")); cancelRuntime(c.getSource().getServer(),old); AgentRecord r=old.alive()?old:new AgentRecord(old.id(),old.name(),old.createdAt(),true,false,null,old.epoch()+1); registry.put(r); spawn(r,c.getSource().getServer(),c.getSource().getPlayerOrException()); return 1; })))
            .then(Commands.literal("brainstatus").executes(c -> { c.getSource().sendSuccess(() -> Component.literal("Brain endpoint: "+System.getProperty("emergentcraft.brainUrl","http://127.0.0.1:3847")),false); return 1; }))));
    }

    private AgentRecord require(String name) { return registry.get(name).orElseThrow(() -> new IllegalArgumentException("Unknown inhabitant: "+name)); }
    private AgentRuntime runtime(AgentRecord r) { return runtimes.computeIfAbsent(r.id(), ignored -> new AgentRuntime()); }

    private void spawn(AgentRecord record, MinecraftServer server, ServerPlayer near) { if(entity(server,record).isPresent()) return; ServerLevel level=(ServerLevel)near.level(); GoalLessVillager body=createBody(record,level,new Vec3(near.getX()+1.5,near.getY(),near.getZ()+1.5),near.getYRot()); level.addFreshEntity(body); registry.put(record.withEmbodiment(body.getUUID())); }
    @SuppressWarnings("unchecked") private GoalLessVillager createBody(AgentRecord record, ServerLevel level, Vec3 pos, float yaw) {
        EntityType<?> raw=BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("villager")); GoalLessVillager body=new GoalLessVillager((EntityType<? extends Villager>)raw,level);
        body.snapTo(pos.x,pos.y,pos.z,yaw,0); body.setCustomName(Component.literal(record.name())); body.setCustomNameVisible(true); body.removeFreeWill(); body.setNoAi(false); body.setPersistenceRequired(); return body;
    }
    /** Vanilla saves deserialize the type as Villager; convert only registered bodies after startup, never during movement. */
    private void restoreEmbodiments(MinecraftServer server) { for(AgentRecord r:registry.all()) { if(!r.alive()) continue; Optional<Entity> e=entity(server,r); if(e.isEmpty() || e.get() instanceof GoalLessVillager) continue; if(e.get() instanceof Villager v && v.level() instanceof ServerLevel level) { GoalLessVillager replacement=createBody(r,level,v.position(),v.getYRot()); level.addFreshEntity(replacement); v.discard(); registry.put(r.withEmbodiment(replacement.getUUID())); LOGGER.info("[EmergentCraft] Restored goal-less embodiment for {}",r.name()); } } }
    private Optional<Entity> entity(MinecraftServer server, AgentRecord r) { if(r.embodimentId()==null) return Optional.empty(); for(ServerLevel level:server.getAllLevels()) { Entity found=level.getEntity(r.embodimentId()); if(found!=null) return Optional.of(found); } return Optional.empty(); }
    private Optional<GoalLessVillager> controlledBody(MinecraftServer server, AgentRecord r) { return entity(server,r).filter(GoalLessVillager.class::isInstance).map(GoalLessVillager.class::cast); }

    private void tick(MinecraftServer server) { updateActions(server); if(server.getTickCount()%100!=0) return; for(AgentRecord r:registry.all()) { AgentRuntime runtime=runtime(r); if(!r.alive()||r.paused()||!runtime.lifecycle.canRequestCognition()) continue; controlledBody(server,r).ifPresent(body -> requestDecision(server,r,body)); } }
    private void requestDecision(MinecraftServer server, AgentRecord stored, GoalLessVillager body) {
        AgentRecord current=stored.nextEpoch(); if(!runtime(current).lifecycle.beginCognition(current.epoch())) return; registry.put(current); String wait="wait:"+current.epoch(), move="move:"+current.epoch(); Vec3 target=new Vec3(body.getX()+3,body.getY(),body.getZ()); ServerLevel world=(ServerLevel)body.level();
        String snapshot="{\"agentId\":\""+current.id()+"\",\"name\":\""+escape(current.name())+"\",\"epoch\":"+current.epoch()+",\"self\":{\"position\":{\"x\":"+body.getX()+",\"y\":"+body.getY()+",\"z\":"+body.getZ()+"},\"health\":20,\"hunger\":20,\"heldItem\":\"minecraft:air\"},\"environment\":{\"dimension\":\""+escape(world.dimension().identifier().toString())+"\",\"timeOfDay\":"+world.getOverworldClockTime()+",\"weather\":\"unknown\",\"light\":0,\"biome\":\"local\"},\"entities\":[],\"events\":[],\"candidates\":[{\"id\":\""+wait+"\",\"kind\":\"WAIT\",\"description\":\"Wait and observe.\"},{\"id\":\""+move+"\",\"kind\":\"MOVE_TO\",\"description\":\"Walk a short distance east on foot.\",\"target\":{\"x\":"+target.x+",\"y\":"+target.y+",\"z\":"+target.z+"}}]}";
        LOGGER.info("[EmergentCraft] {} perceived 0 entities, 2 candidate actions",current.name()); brain.decide(snapshot).thenAccept(action -> server.execute(() -> applyDecision(server,current,wait,move,target,action)));
    }
    private void applyDecision(MinecraftServer server, AgentRecord current, String wait, String move, Vec3 target, Optional<String> action) {
        AgentRuntime runtime=runtime(current); if(!runtime.lifecycle.acceptsDecision(current.epoch())) return; AgentRecord latest=registry.get(current.name()).orElse(null);
        if(latest==null||latest.epoch()!=current.epoch()||latest.paused()||!latest.alive()) { runtime.lifecycle.finishCognition(current.epoch()); return; } Optional<GoalLessVillager> body=controlledBody(server,latest); if(body.isEmpty()) { runtime.lifecycle.finishCognition(current.epoch()); return; }
        if(action.filter(wait::equals).isPresent()) { runtime.lifecycle.finishCognition(current.epoch()); LOGGER.info("[EmergentCraft] {} chose WAIT",latest.name()); return; }
        if(action.filter(move::equals).isPresent() && runtime.lifecycle.beginAction(current.epoch())) startMove(latest,body.get(),runtime,move,current.epoch(),target,false); else runtime.lifecycle.finishCognition(current.epoch());
    }
    private boolean startMove(AgentRecord record, GoalLessVillager body, AgentRuntime runtime, String actionId, long epoch, Vec3 target, boolean diagnostic) {
        double distance=body.position().distanceTo(target); if(!diagnostic) brain.appendFactualEvent(record.id(),epoch,"ACTION","Attempted to move toward a nearby location.");
        if(!body.getNavigation().moveTo(target.x,target.y,target.z,0.35)) { finishMove(record,body,runtime,false,"Could not find a path to the destination.",diagnostic); return false; }
        long tick=((ServerLevel)body.level()).getServer().getTickCount(); runtime.move=new MoveExecution(actionId,epoch,body.position(),target,tick,tick,distance,diagnostic); LOGGER.info("[EmergentCraft] {} started {}",record.name(),diagnostic?"movement test":"MOVE_TO"); return true;
    }
    private void updateActions(MinecraftServer server) { long tick=server.getTickCount(); for(AgentRecord r:registry.all()) { AgentRuntime runtime=runtime(r); MoveExecution move=runtime.move; if(runtime.lifecycle.state()!=AgentLifecycle.State.EXECUTING_ACTION||move==null) continue; Optional<GoalLessVillager> body=controlledBody(server,r); if(body.isEmpty()) { finishMove(r,null,runtime,false,"Movement failed because the body disappeared.",move.diagnostic()); continue; } double distance=body.get().position().distanceTo(move.target()); if(distance*distance<=ARRIVAL_DISTANCE_SQR) { finishMove(r,body.get(),runtime,true,"Reached the destination.",move.diagnostic()); continue; } if(body.get().getNavigation().isDone()) { finishMove(r,body.get(),runtime,false,"Movement path ended before reaching the destination.",move.diagnostic()); continue; } if(tick-move.lastProgressTick()>=STUCK_TICKS) { if(move.lastDistance()-distance>=0.25) runtime.move=move.withProgress(tick,distance); else { finishMove(r,body.get(),runtime,false,"Movement failed because the body became stuck.",move.diagnostic()); continue; } } if(tick-move.startTick()>=MOVE_TIMEOUT_TICKS) finishMove(r,body.get(),runtime,false,"Movement timed out before reaching the destination.",move.diagnostic()); } }
    private void finishMove(AgentRecord r, GoalLessVillager body, AgentRuntime runtime, boolean success, String outcome, boolean diagnostic) { if(body!=null) body.getNavigation().stop(); runtime.move=null; runtime.lifecycle.finishAction(); if(!diagnostic) brain.appendFactualEvent(r.id(),r.epoch(),"OUTCOME",outcome); LOGGER.info("[EmergentCraft] {} {} {}: {}",r.name(),diagnostic?"movement test":"MOVE_TO",success?"completed":"failed",outcome); }
    private void cancelRuntime(MinecraftServer server, AgentRecord r) { controlledBody(server,r).ifPresent(body -> body.getNavigation().stop()); AgentRuntime runtime=runtime(r); runtime.move=null; runtime.lifecycle.cancel(); }
    private Optional<Vec3> findReachableTestTarget(GoalLessVillager body) { int[][] offsets={{4,0},{-4,0},{0,4},{0,-4},{3,3},{3,-3},{-3,3},{-3,-3}}; for(int[] offset:offsets) { BlockPos candidate=body.blockPosition().offset(offset[0],0,offset[1]); Path path=body.getNavigation().createPath(candidate,0); if(path!=null&&path.canReach()) { BlockPos end=path.getTarget(); return Optional.of(new Vec3(end.getX()+0.5,end.getY(),end.getZ()+0.5)); } } return Optional.empty(); }
    private static String escape(String value) { return value.replace("\\","\\\\").replace("\"","\\\""); }
}
