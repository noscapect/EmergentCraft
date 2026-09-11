package nl.noscapect.emergentcraft.actions;

/** Pure guards used before a world-changing executor runs. */
public final class ActionValidation {
    private ActionValidation() { }
    public static boolean canBreak(float hardness,double distanceSqr,boolean exactOfferedBlock) { return exactOfferedBlock&&hardness>=0&&distanceSqr<=9; }
    public static boolean canPickUp(double distanceSqr,boolean exactOfferedItem,boolean capacity) { return exactOfferedItem&&capacity&&distanceSqr<=9; }
}
