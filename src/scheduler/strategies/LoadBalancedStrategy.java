package scheduler.strategies;

import util.SubSystemSharedState;

import java.util.*;

public class LoadBalancedStrategy {


        public void allocate(SubSystemSharedState sharedState) {
            HashMap<int, floorRequests = sharedState.getFloorRequests();
            elevatorStates = sharedState.getElevatorStates();
            int numRequestsPerElevator = floorRequests.size() / elevatorCars.size();
            int numExtraRequests = floorRequests.size() % elevatorCars.size();

            List<Integer> numStops = new ArrayList<>(Collections.nCopies(elevatorCars.size(), 0));

            int requestIndex = 0;
            for (int i = 0; i < elevatorCars.size(); i++) {
                int numRequests = numRequestsPerElevator;
                if (numExtraRequests > 0) {
                    numRequests++;
                    numExtraRequests--;
                }

                int minStopsIndex = 0;
                for (int j = 1; j < elevatorCars.size(); j++) {
                    if (numStops.get(j) < numStops.get(minStopsIndex)) {
                        minStopsIndex = j;
                    }
                }

                for (int j = 0; j < numRequests; j++) {
                    FloorRequest request = floorRequests.get(requestIndex);
                    elevatorCars.get(minStopsIndex).assignFloorRequest(request.floor, request.direction);
                    numStops.set(minStopsIndex, numStops.get(minStopsIndex) + 1);
                    requestIndex++;
                }
            }

            floorRequests.clear();
        }
    }
}
