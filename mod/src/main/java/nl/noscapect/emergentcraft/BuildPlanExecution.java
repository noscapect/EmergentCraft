package nl.noscapect.emergentcraft;

import nl.noscapect.emergentcraft.actions.OfferedAction;
import java.util.List;

/** A model-authored, bounded geometry plan; only its already-validated placements are executed. */
public record BuildPlanExecution(String purpose,List<OfferedAction> placements,int index) {
    public OfferedAction current() { return placements.get(index); }
    public boolean hasNext() { return index+1<placements.size(); }
    public BuildPlanExecution next() { return new BuildPlanExecution(purpose,placements,index+1); }
}
