package nl.noscapect.emergentcraft;

/** Non-persistent execution state associated with one stable agent identity. */
public final class AgentRuntime {
    public final AgentLifecycle lifecycle = new AgentLifecycle();
    public MoveExecution move;
}
