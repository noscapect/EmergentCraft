package nl.noscapect.emergentcraft;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AgentLifecycleTest {
    @Test void executingActionBlocksNewCognition() {
        AgentLifecycle lifecycle = new AgentLifecycle();
        assertTrue(lifecycle.beginCognition(4));
        assertTrue(lifecycle.beginAction(4));
        assertFalse(lifecycle.canRequestCognition());
        assertFalse(lifecycle.beginCognition(5));
        lifecycle.finishAction();
        assertTrue(lifecycle.canRequestCognition());
    }

    @Test void staleCognitionCannotReplaceTheCurrentAction() {
        AgentLifecycle lifecycle = new AgentLifecycle();
        assertTrue(lifecycle.beginCognition(7));
        assertFalse(lifecycle.acceptsDecision(6));
        assertFalse(lifecycle.beginAction(6));
        assertTrue(lifecycle.beginAction(7));
        assertFalse(lifecycle.acceptsDecision(7));
    }

    @Test void movetestPreemptsWaitingCognitionAndInvalidatesLateResponse() {
        AgentLifecycle lifecycle = new AgentLifecycle();
        assertTrue(lifecycle.beginCognition(10));
        lifecycle.beginAdminActionForcefully();
        assertEquals(AgentLifecycle.State.EXECUTING_ACTION, lifecycle.state());
        assertFalse(lifecycle.acceptsDecision(10));
    }

    @Test void movetestPreemptsMoveAndCognitionCanResumeAfterItFinishes() {
        AgentLifecycle lifecycle = new AgentLifecycle();
        assertTrue(lifecycle.beginCognition(3)); assertTrue(lifecycle.beginAction(3));
        lifecycle.beginAdminActionForcefully();
        assertEquals(AgentLifecycle.State.EXECUTING_ACTION, lifecycle.state());
        lifecycle.finishAction();
        assertTrue(lifecycle.canRequestCognition());
        assertTrue(lifecycle.beginCognition(4));
    }
}
