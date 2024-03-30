package scheduler.strategies;

import util.Direction;
import util.ElevatorStateUpdate;
import util.SubSystemSharedState;
import util.WorkAssignment;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class LoadBalancedStrategy extends AllocationStrategy{
        public LoadBalancedStrategy(SubSystemSharedState sharedState) {
            super(sharedState);
        }

//        public void allocate(SubSystemSharedState sharedState) {
//            HashMap<int, floorRequests = sharedState.getFloorRequests();
//            elevatorStates = sharedState.getElevatorStates();
//            int numRequestsPerElevator = floorRequests.size() / elevatorCars.size();
//            int numExtraRequests = floorRequests.size() % elevatorCars.size();
//
//            List<Integer> numStops = new ArrayList<>(Collections.nCopies(elevatorCars.size(), 0));
//
//            int requestIndex = 0;
//            for (int i = 0; i < elevatorCars.size(); i++) {
//                int numRequests = numRequestsPerElevator;
//                if (numExtraRequests > 0) {
//                    numRequests++;
//                    numExtraRequests--;
//                }
//
//                int minStopsIndex = 0;
//                for (int j = 1; j < elevatorCars.size(); j++) {
//                    if (numStops.get(j) < numStops.get(minStopsIndex)) {
//                        minStopsIndex = j;
//                    }
//                }
//
//                for (int j = 0; j < numRequests; j++) {
//                    FloorRequest request = floorRequests.get(requestIndex);
//                    elevatorCars.get(minStopsIndex).assignFloorRequest(request.floor, request.direction);
//                    numStops.set(minStopsIndex, numStops.get(minStopsIndex) + 1);
//                    requestIndex++;
//                }
//            }
//
//            floorRequests.clear();
//        }
//
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

