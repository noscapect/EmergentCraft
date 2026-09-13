package nl.noscapect.emergentcraft;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SearchPolicyTest {
    @Test void waypointsStayWithinRequestedRadiusAndLegLimit() {
        BlockPos origin=new BlockPos(0,64,0);
        for(int leg=0;leg<SearchPolicy.MAX_LEGS;leg++) assertTrue(SearchPolicy.waypoint(origin,12,leg).distSqr(origin)<=12*12);
        assertNull(SearchPolicy.waypoint(origin,12,SearchPolicy.MAX_LEGS));
    }
    @Test void deadlineIsBounded() {
        SearchExecution search=new SearchExecution("s",true,"minecraft:stone",12,new BlockPos(0,64,0),10,20,0,SearchPolicy.MAX_LEGS);
        assertFalse(SearchPolicy.expired(search,19)); assertTrue(SearchPolicy.expired(search,20));
    }
    @Test void unchangedFailedApproachIsBrieflySuppressedButMovementReoffers() {
        AgentRuntime runtime=new AgentRuntime(); java.util.UUID id=java.util.UUID.randomUUID(); net.minecraft.world.phys.Vec3 at=new net.minecraft.world.phys.Vec3(4,64,4);
        runtime.failedApproach(id,at,10); assertTrue(runtime.suppressApproach(id,at,20)); assertFalse(runtime.suppressApproach(id,new net.minecraft.world.phys.Vec3(7,64,4),20));
    }
}
