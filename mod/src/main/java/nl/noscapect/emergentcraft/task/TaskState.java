package nl.noscapect.emergentcraft.task;

/**
 * Adapted from zoyluoblue/mc_aiplayer, task/TaskState.java, commit a029fa6a3760fd0f83834c104051b041d986da60 (MIT).
 * This is execution state only; it carries no assigned goal or survival policy.
 */
public enum TaskState { PENDING, RUNNING, PAUSED, COMPLETED, FAILED, CANCELLED }
