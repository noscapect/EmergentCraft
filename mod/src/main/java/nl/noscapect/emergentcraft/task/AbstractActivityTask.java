package nl.noscapect.emergentcraft.task;

/**
 * Adapted from zoyluoblue/mc_aiplayer, task/AbstractTask.java, commit a029fa6a3760fd0f83834c104051b041d986da60 (MIT).
 * Removed AIPlayer/action-pack dependencies and retained the lifecycle/postcondition boundary for native Villager activities.
 */
public abstract class AbstractActivityTask implements ActivityTask {
    private TaskState state=TaskState.PENDING; private String failureReason=""; private int elapsed;
    @Override public final void start(){if(state!=TaskState.PENDING)return;state=TaskState.RUNNING;onStart();}
    @Override public final void tick(){if(state!=TaskState.RUNNING)return;elapsed++;onTick();}
    @Override public final void pause(){if(state!=TaskState.RUNNING)return;state=TaskState.PAUSED;onPause();}
    @Override public final void resume(){if(state!=TaskState.PAUSED)return;state=TaskState.RUNNING;onResume();}
    @Override public final void abort(){if(terminal())return;state=TaskState.FAILED;failureReason="aborted";onAbort();}
    @Override public final void cancel(String reason){if(terminal())return;state=TaskState.CANCELLED;failureReason=reason==null?"":reason;onAbort();}
    @Override public final TaskState state(){return state;} @Override public final String failureReason(){return failureReason;} @Override public final int elapsedTicks(){return elapsed;}
    protected final void complete(){state=TaskState.COMPLETED;} protected final void fail(String reason){state=TaskState.FAILED;failureReason=reason==null?"":reason;}
    private boolean terminal(){return state==TaskState.COMPLETED||state==TaskState.FAILED||state==TaskState.CANCELLED;}
    protected abstract void onStart(); protected abstract void onTick(); protected void onPause(){} protected void onResume(){} protected void onAbort(){}
}
