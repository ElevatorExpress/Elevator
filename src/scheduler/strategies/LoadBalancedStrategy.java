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
        boolean allocated = false;
        for (int elevatorId : sharedState.getElevatorStates().keySet()) {
            ElevatorStateUpdate stateUsedForAllocation = sharedState.getElevatorStates().get(elevatorId);
            if (stateUsedForAllocation.getDirection() == Direction.ANY) {
                if (!(largestAssignment(elevatorId, sharedState.getWorkAssignments()))){
                    sharedState.addWorkAssignment(elevatorId, workAssignment);
                    allocated = true;
                    break;
                }
            }
            else if (stateUsedForAllocation.getDirection() == Direction.DOWN) {
                if (workAssignment.getDirection() == Direction.DOWN && workAssignment.getServiceFloor() < stateUsedForAllocation.getFloor()) {
                    if (!(largestAssignment(elevatorId, sharedState.getWorkAssignments()))) {
                        sharedState.addWorkAssignment(elevatorId, workAssignment);
                        allocated = true;
                        break;
                    }
                }
            }
            else if (stateUsedForAllocation.getDirection() == Direction.UP){
               if (workAssignment.getServiceFloor() > stateUsedForAllocation.getFloor()) {
                   if (!(largestAssignment(elevatorId, sharedState.getWorkAssignments()))) {
                       sharedState.addWorkAssignment(elevatorId, workAssignment);
                       allocated = true;
                       break;
                   }
               }
            }
        }
        if (!allocated) sharedState.addWorkAssignment(1, workAssignment);

        for (int elevatorId : sharedState.getElevatorStates().keySet()) {
            ElevatorStateUpdate stateUsedForAllocation = sharedState.getElevatorStates().get(elevatorId);
//            if (stateUsedForAllocation.getWorkAssignments() != null) {
                System.out.println(elevatorId + ": " + stateUsedForAllocation.getDirection() + " " + stateUsedForAllocation.getFloor() + " " + sharedState.getWorkAssignments().get(elevatorId).size());
//            }
//            else {
//                System.out.println(elevatorId + ": " + stateUsedForAllocation.getDirection() + " " + stateUsedForAllocation.getFloor());
//
//            }
        }
        System.out.println();
    }


    public boolean largestAssignment(int elevatorId, HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments) {
        int id = 0;
        int max = 0;
        for (int eleId : workAssignments.keySet()) {
            if (workAssignments.get(eleId).size() >= max) {
                max = workAssignments.get(eleId).size();
                id = eleId;
            }
        }
        return id == elevatorId;
    }
}

