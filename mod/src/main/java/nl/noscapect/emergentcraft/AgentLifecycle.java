package nl.noscapect.emergentcraft;

/** Runtime-only gate preventing cognitive responses from overlapping physical work. */
public final class AgentLifecycle {
    public enum State { IDLE, WAITING_FOR_COGNITION, EXECUTING_ACTION }
    private State state = State.IDLE;
    private long waitingEpoch = -1;

    public State state() { return state; }
    public boolean canRequestCognition() { return state == State.IDLE; }
    public boolean beginCognition(long epoch) {
        if (!canRequestCognition()) return false;
        state = State.WAITING_FOR_COGNITION; waitingEpoch = epoch; return true;
    }
    public boolean acceptsDecision(long epoch) { return state == State.WAITING_FOR_COGNITION && waitingEpoch == epoch; }
    public boolean beginAction(long epoch) {
        if (!acceptsDecision(epoch)) return false;
        state = State.EXECUTING_ACTION; return true;
    }
    public boolean beginAdminAction() {
        if (!canRequestCognition()) return false;
        state = State.EXECUTING_ACTION; return true;
    }
    public void finishCognition(long epoch) { if (acceptsDecision(epoch)) { state = State.IDLE; waitingEpoch = -1; } }
    public void finishAction() { if (state == State.EXECUTING_ACTION) { state = State.IDLE; waitingEpoch = -1; } }
    public void cancel() { state = State.IDLE; waitingEpoch = -1; }
}
