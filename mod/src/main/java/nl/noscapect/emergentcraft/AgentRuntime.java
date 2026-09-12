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
    public Map<String,OfferedAction> offeredActions=Map.of();
    public final Deque<RecentEvent> recentEvents=new ArrayDeque<>();
    public float lastHealth=Float.NaN;
    public void event(String kind,String text) { if(recentEvents.size()>=12) recentEvents.removeFirst(); recentEvents.addLast(new RecentEvent(kind,text.length()>240?text.substring(0,240):text)); }
    public List<RecentEvent> recent() { return List.copyOf(recentEvents); }
    public String describeState() { if(lifecycle.state()==AgentLifecycle.State.EXECUTING_ACTION&&buildPlan!=null)return "EXECUTING_ACTION (BUILD_PLAN "+(buildPlan.index()+1)+"/"+buildPlan.placements().size()+")"; if(task!=null)return "EXECUTING_ACTION ("+task.name()+" "+task.state()+" "+Math.round(task.progress()*100)+"%)"; if(skill!=null)return "EXECUTING_ACTION ("+skill.kind()+"/"+skill.phase()+")"; return lifecycle.state() == AgentLifecycle.State.EXECUTING_ACTION && move != null && move.diagnostic() ? "EXECUTING_ACTION (movetest)" : lifecycle.state().toString(); }
}
