package nl.noscapect.emergentcraft;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AgentMetabolismStoreTest {
    @Test void drain_uses_saturation_before_hunger_and_never_goes_negative() { var fed=new AgentMetabolismStore.State(10,2f,0); var first=AgentMetabolismStore.drained(fed,1,10); assertEquals(10,first.hunger()); assertEquals(1f,first.saturation()); var second=AgentMetabolismStore.drained(first,3,20); assertEquals(8,second.hunger()); assertEquals(0f,second.saturation()); var empty=AgentMetabolismStore.drained(new AgentMetabolismStore.State(0,0,0),5,30); assertEquals(0,empty.hunger()); assertTrue(empty.starving()); }
}
