package scheduler.strategies;

import util.SubSystemSharedState;
import util.WorkAssignment;

public abstract class AllocationStrategy {
    protected SubSystemSharedState sharedState;

    /**
     * Creates an allocation Strategy
     * @param sharedState The shared state system
     */
    AllocationStrategy(SubSystemSharedState sharedState) {
        this.sharedState = sharedState;
    }

    /**
     * Allocates work assignments
     * @param workAssignment The wor assignment to allocate
     */
    public abstract void allocate(WorkAssignment workAssignment);
}
