package scheduler.strategies;

import util.SubSystemSharedState;
import util.WorkAssignment;

import java.util.ArrayList;

public abstract class AllocationStrategy {
    protected SubSystemSharedState sharedState;
    AllocationStrategy(SubSystemSharedState sharedState) {
        this.sharedState = sharedState;
    }
    public abstract void allocate(WorkAssignment workAssignment);
}
