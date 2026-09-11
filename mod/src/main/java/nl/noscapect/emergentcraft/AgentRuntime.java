package nl.noscapect.emergentcraft;

/** Non-persistent execution state associated with one stable agent identity. */
public final class AgentRuntime {
    public final AgentLifecycle lifecycle = new AgentLifecycle();
    public MoveExecution move;
    public String describeState() { return lifecycle.state() == AgentLifecycle.State.EXECUTING_ACTION && move != null && move.diagnostic() ? "EXECUTING_ACTION (movetest)" : lifecycle.state().toString(); }
}
