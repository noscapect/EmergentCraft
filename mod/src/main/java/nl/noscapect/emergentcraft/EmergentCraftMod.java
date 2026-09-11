package nl.noscapect.emergentcraft;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.EntitySpawnReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public final class EmergentCraftMod implements ModInitializer {
    public static final String MOD_ID = "emergentcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private final AgentRegistry registry = new AgentRegistry();
    private final BrainGateway brain = new BrainGateway();
    private final Set<UUID> pending = new HashSet<>();

    @Override public void onInitialize() {
        registerCommands();
        ServerTickEvents.END_SERVER_TICK.register(this::tick);
        LOGGER.info("[EmergentCraft] First Contact initialized; brain is loopback-only by default.");
    }
    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> dispatcher.register(Commands.literal("ec").requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
            .then(Commands.literal("spawn").then(Commands.argument("name", StringArgumentType.word()).executes(c -> { String name=StringArgumentType.getString(c,"name"); AgentRecord record=registry.get(name).orElseGet(() -> registry.create(name)); spawn(record,c.getSource().getServer(),c.getSource().getPlayerOrException()); c.getSource().sendSuccess(() -> Component.literal("EmergentCraft inhabitant " + name + " embodied."), false); return 1; })))
            .then(Commands.literal("list").executes(c -> { c.getSource().sendSuccess(() -> Component.literal(registry.all().stream().map(r -> r.name()+" ["+(r.alive()?"alive":"dead")+"]").reduce("Inhabitants: ", (a,b)->a+" "+b)), false); return 1; }))
            .then(Commands.literal("inspect").then(Commands.argument("name", StringArgumentType.word()).executes(c -> { AgentRecord r=require(StringArgumentType.getString(c,"name")); c.getSource().sendSuccess(() -> Component.literal(r.toString()), false); return 1; })))
            .then(Commands.literal("pause").then(Commands.argument("name", StringArgumentType.word()).executes(c -> { AgentRecord r=require(StringArgumentType.getString(c,"name")); registry.put(r.withPaused(true)); return 1; })))
            .then(Commands.literal("resume").then(Commands.argument("name", StringArgumentType.word()).executes(c -> { AgentRecord r=require(StringArgumentType.getString(c,"name")); registry.put(r.withPaused(false)); return 1; })))
            .then(Commands.literal("remove").then(Commands.argument("name", StringArgumentType.word()).executes(c -> { String name=StringArgumentType.getString(c,"name"); registry.get(name).flatMap(r -> entity(c.getSource().getServer(), r)).ifPresent(Entity::discard); registry.remove(name); return 1; })))
            .then(Commands.literal("revive").then(Commands.argument("name", StringArgumentType.word()).executes(c -> { AgentRecord old=require(StringArgumentType.getString(c,"name")); AgentRecord revived = old.alive()?old:new AgentRecord(old.id(),old.name(),old.createdAt(),true,false,null,old.epoch()+1); registry.put(revived); spawn(revived,c.getSource().getServer(),c.getSource().getPlayerOrException()); return 1; })))
            .then(Commands.literal("brainstatus").executes(c -> { c.getSource().sendSuccess(() -> Component.literal("Brain endpoint: " + System.getProperty("emergentcraft.brainUrl", "http://127.0.0.1:3847")), false); return 1; }))));
    }
    private AgentRecord require(String name) { return registry.get(name).orElseThrow(() -> new IllegalArgumentException("Unknown inhabitant: " + name)); }
    private void spawn(AgentRecord record, MinecraftServer server, ServerPlayer near) {
        Optional<Entity> extant=entity(server,record); if (extant.isPresent()) return;
        ServerLevel level=(ServerLevel) near.level();
        EntityType<?> villagerType=BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("villager"));
        Entity created=villagerType.create(level, EntitySpawnReason.COMMAND);
        if (!(created instanceof Villager body)) throw new IllegalStateException("Could not create villager embodiment");
        body.snapTo(near.getX()+1.5,near.getY(),near.getZ()+1.5,near.getYRot(),0); body.setCustomName(Component.literal(record.name())); body.setCustomNameVisible(true); body.setNoAi(true); body.setPersistenceRequired(); level.addFreshEntity(body); registry.put(record.withEmbodiment(body.getUUID()));
    }
    private Optional<Entity> entity(MinecraftServer server, AgentRecord record) { if (record.embodimentId()==null) return Optional.empty(); for (ServerLevel level:server.getAllLevels()) { Entity found=level.getEntity(record.embodimentId()); if(found!=null) return Optional.of(found); } return Optional.empty(); }
    private void tick(MinecraftServer server) { if (server.getTickCount()%100 != 0) return; for (AgentRecord stored:registry.all()) { if(!stored.alive()||stored.paused()||pending.contains(stored.id())) continue; Optional<Entity> body=entity(server,stored); if(body.isEmpty()) continue; requestDecision(server, stored, body.get()); } }
    private void requestDecision(MinecraftServer server, AgentRecord stored, Entity body) {
        AgentRecord current=stored.nextEpoch(); registry.put(current); pending.add(current.id());
        String wait="wait:"+current.epoch(), move="move:"+current.epoch();
        ServerLevel world=(ServerLevel) body.level();
        String snapshot="{\"agentId\":\""+current.id()+"\",\"name\":\""+escape(current.name())+"\",\"epoch\":"+current.epoch()+",\"self\":{\"position\":{\"x\":"+body.getX()+",\"y\":"+body.getY()+",\"z\":"+body.getZ()+"},\"health\":20,\"hunger\":20,\"heldItem\":\"minecraft:air\"},\"environment\":{\"dimension\":\""+escape(world.dimension().identifier().toString())+"\",\"timeOfDay\":"+world.getOverworldClockTime()+",\"weather\":\"unknown\",\"light\":0,\"biome\":\"local\"},\"entities\":[],\"events\":[],\"candidates\":[{\"id\":\""+wait+"\",\"kind\":\"WAIT\",\"description\":\"Wait and observe.\"},{\"id\":\""+move+"\",\"kind\":\"MOVE_TO\",\"description\":\"Walk a short distance east on foot.\",\"target\":{\"x\":"+(body.getX()+3)+",\"y\":"+body.getY()+",\"z\":"+body.getZ()+"}}]}";
        LOGGER.info("[EmergentCraft] {} perceived 0 entities, 2 candidate actions", current.name());
        brain.decide(snapshot).thenAccept(action -> server.execute(() -> { pending.remove(current.id()); AgentRecord latest=registry.get(current.name()).orElse(null); if(latest==null||latest.epoch()!=current.epoch()||latest.paused()||!latest.alive()) return; Optional<Entity> fresh=entity(server,latest); if(fresh.isEmpty()) return; action.filter(id -> id.equals(wait)||id.equals(move)).ifPresent(id -> { if(id.equals(move) && fresh.get() instanceof Villager villager) { villager.getNavigation().moveTo(villager.getX()+3,villager.getY(),villager.getZ(),0.35); LOGGER.info("[EmergentCraft] {} chose MOVE_TO", latest.name()); } else LOGGER.info("[EmergentCraft] {} chose WAIT", latest.name()); }); }));
    }
    private static String escape(String value) { return value.replace("\\","\\\\").replace("\"","\\\""); }
}
