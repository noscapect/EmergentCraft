package nl.noscapect.emergentcraft;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SearchPolicyTest {
    @Test void waypointsStayWithinRequestedRadiusAndLegLimit() {
        BlockPos origin=new BlockPos(0,64,0);
        double previous=0;
        for(int leg=0;leg<SearchPolicy.MAX_LEGS;leg++) { double distance=SearchPolicy.waypoint(origin,12,leg).distSqr(origin); assertTrue(distance<=12*12); assertTrue(distance>=previous); previous=distance; }
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
    @Test void exhaustedSearchIsSuppressedLocallyAndClearedByMeaningfulMove() {
        AgentRuntime runtime=new AgentRuntime(); BlockPos origin=new BlockPos(0,64,0);
        runtime.exhaustedSearch(true,"minecraft:clay",origin,10);
        assertTrue(runtime.suppressExhaustedSearch(true,"minecraft:clay",new BlockPos(2,64,0),20));
        assertFalse(runtime.suppressExhaustedSearch(true,"minecraft:sand",origin,20));
        runtime.clearSearchSuppressionAfterMove(new BlockPos(9,64,0));
        assertFalse(runtime.suppressExhaustedSearch(true,"minecraft:clay",origin,20));
    }
}
