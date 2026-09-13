package nl.noscapect.emergentcraft;

import nl.noscapect.emergentcraft.actions.OfferedAction;
import nl.noscapect.emergentcraft.task.NativeActivityTask;
import nl.noscapect.emergentcraft.perception.PerceptionModels.RecentEvent;
import java.util.*;

/** Non-persistent execution state associated with one stable agent identity. */
public final class AgentRuntime {
    public final AgentLifecycle lifecycle = new AgentLifecycle();
    public MoveExecution move;
    public SkillExecution skill;
    public NativeActivityTask task;
    public BuildPlanExecution buildPlan;
    public SearchExecution search;
    public Map<String,OfferedAction> offeredActions=Map.of();
    public final Deque<RecentEvent> recentEvents=new ArrayDeque<>();
    private final Map<UUID,FailedApproach> failedApproaches=new HashMap<>();
    public float lastHealth=Float.NaN;
    public void event(String kind,String text) { if(recentEvents.size()>=12) recentEvents.removeFirst(); recentEvents.addLast(new RecentEvent(kind,text.length()>240?text.substring(0,240):text)); }
    public List<RecentEvent> recent() { return List.copyOf(recentEvents); }
    public boolean suppressApproach(UUID entityId,net.minecraft.world.phys.Vec3 position,long tick) { FailedApproach failure=failedApproaches.get(entityId); if(failure==null||tick>=failure.untilTick||failure.position.distanceToSqr(position)>4) { failedApproaches.remove(entityId); return false; } return true; }
    public void failedApproach(UUID entityId,net.minecraft.world.phys.Vec3 position,long tick) { failedApproaches.put(entityId,new FailedApproach(position,tick+100)); }
    private record FailedApproach(net.minecraft.world.phys.Vec3 position,long untilTick) { }
    public String describeState() { if(search!=null)return "EXECUTING_ACTION (SEARCH "+(search.leg()+1)+"/"+search.maxLegs()+")"; if(lifecycle.state()==AgentLifecycle.State.EXECUTING_ACTION&&buildPlan!=null)return "EXECUTING_ACTION (BUILD_PLAN "+(buildPlan.index()+1)+"/"+buildPlan.placements().size()+")"; if(task!=null)return "EXECUTING_ACTION ("+task.name()+" "+task.state()+" "+Math.round(task.progress()*100)+"%)"; if(skill!=null)return "EXECUTING_ACTION ("+skill.kind()+"/"+skill.phase()+")"; return lifecycle.state() == AgentLifecycle.State.EXECUTING_ACTION && move != null && move.diagnostic() ? "EXECUTING_ACTION (movetest)" : lifecycle.state().toString(); }
}
