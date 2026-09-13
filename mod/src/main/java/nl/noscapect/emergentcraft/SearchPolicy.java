package nl.noscapect.emergentcraft;

import net.minecraft.core.BlockPos;

/** Pure bounds for server-owned searches. */
public final class SearchPolicy {
    public static final int MAX_RANGE=48, MAX_LEGS=8, MAX_TICKS=800;
    private static final int[][] DIRECTIONS={{1,0},{0,1},{-1,0},{0,-1},{1,1},{-1,1},{-1,-1},{1,-1}};
    private SearchPolicy() { }
    public static BlockPos waypoint(BlockPos origin,int range,int leg) { if(leg<0||leg>=MAX_LEGS) return null; int safe=Math.max(1,Math.min(MAX_RANGE,range)); int[] direction=DIRECTIONS[leg]; double length=Math.hypot(direction[0],direction[1]); int distance=(int)Math.floor(safe/length); return origin.offset(direction[0]*distance,0,direction[1]*distance); }
    public static boolean expired(SearchExecution search,long tick) { return tick>=search.deadlineTick(); }
}
