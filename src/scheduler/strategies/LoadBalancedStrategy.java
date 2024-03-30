package scheduler.strategies;

import util.SubSystemSharedState;
import util.WorkAssignment;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class LoadBalancedStrategy extends AllocationStrategy{
    /**
     * Creates a LoadBalancedStrategy
     * @param sharedState The shared state system
     */
    public LoadBalancedStrategy(SubSystemSharedState sharedState) {
            super(sharedState);
        }

    /**
     * Allocates work assignments
     * @param workAssignment The wor assignment to allocate
     */
    @Override
    public void allocate(WorkAssignment workAssignment) {
        for (int elevatorId : sharedState.getElevatorStates().keySet()) {
            if (sharedState.getElevatorStates().keySet().size() == 1) sharedState.addWorkAssignment(elevatorId, workAssignment);
            else if (sharedState.getWorkAssignments().get(elevatorId).isEmpty()) {
                sharedState.addWorkAssignment(elevatorId, workAssignment);
                break;
            }
            else if (smallestAssignment(elevatorId, sharedState.getWorkAssignments())) {
                sharedState.addWorkAssignment(elevatorId, workAssignment);
                break;
            }
        }
    }

    /**
     * Checks if the given elevator is assigned to the smallest work assignment
     * @param elevatorId The elevator to check
     * @param workAssignments The assignments to check from
     * @return True if the elevator is assigned to the smallest work assignment
     */
    public boolean smallestAssignment(int elevatorId, HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments) {
        int id = elevatorId;
        int min = workAssignments.get(elevatorId).size();
        for (int eleId : workAssignments.keySet()) {
            if (workAssignments.get(eleId).size() <= min) {
                min = workAssignments.get(eleId).size();
                id = eleId;
            }
        }
        if (workAssignments.keySet().size() == 1) return false;
        return id == elevatorId;
    }
}

