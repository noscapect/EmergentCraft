package nl.noscapect.emergentcraft.actions;

/** Pure guards used before a world-changing executor runs. */
public final class ActionValidation {
    private ActionValidation() { }
    public static boolean canBreak(float hardness,double distanceSqr,boolean exactOfferedBlock) { return exactOfferedBlock&&hardness>=0&&distanceSqr<=9; }
    public static boolean canPickUp(double distanceSqr,boolean exactOfferedItem,boolean capacity) { return exactOfferedItem&&capacity&&distanceSqr<=9; }
    public static boolean canAttack(double distanceSqr,boolean offeredLivingTarget,boolean alive) { return offeredLivingTarget&&alive&&distanceSqr<=9; }
    public static boolean canPlace(boolean offeredBlockItem,boolean inventoryHasItem,boolean targetReplaceable,boolean supported,double distanceSqr) { return offeredBlockItem&&inventoryHasItem&&targetReplaceable&&supported&&distanceSqr<=9; }
    public static boolean boundedBuildPlan(int placements,boolean allOwned,boolean allLocal) { return placements>0&&placements<=24&&allOwned&&allLocal; }
}
