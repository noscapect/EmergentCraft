package nl.noscapect.emergentcraft;

import nl.noscapect.emergentcraft.actions.OfferedAction;

/** Runtime-only public progress for a multi-primitive activity. */
public record SkillExecution(String actionId,OfferedAction.Kind kind,String phase,long startedTick,long lastProgressTick,String target,int progress,int total,boolean diagnostic) {
    public SkillExecution phase(String next,long tick) { return new SkillExecution(actionId,kind,next,startedTick,tick,target,progress,total,diagnostic); }
    public SkillExecution progress(int next,String nextPhase,long tick) { return new SkillExecution(actionId,kind,nextPhase,startedTick,tick,target,next,total,diagnostic); }
}
