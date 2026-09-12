package nl.noscapect.emergentcraft.task;

/**
 * Adapted from zoyluoblue/mc_aiplayer, task/Task.java, commit a029fa6a3760fd0f83834c104051b041d986da60 (MIT).
 * The caller supplies actual Minecraft operations; tasks never decide motivations or issue commands.
 */
public interface ActivityTask {
    String name(); TaskState state(); String failureReason(); void start(); void tick(); void pause(); void resume(); void abort(); void cancel(String reason); double progress(); int elapsedTicks();
}
