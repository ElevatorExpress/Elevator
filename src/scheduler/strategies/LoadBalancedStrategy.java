package scheduler.strategies;

import util.Direction;
import util.ElevatorStateUpdate;
import util.Messages.Signal;
import util.SubSystemSharedState;
import util.WorkAssignment;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 * Class for splitting requests among the elevators
 */
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
     * @param workAssignment The work assignment to allocate
     */
    @Override
    public void allocate(WorkAssignment workAssignment) throws RemoteException {
        allocateImproved(workAssignment);
    }




    public void determineFloorStops(ArrayList<Integer> upfloorStops, ArrayList<Integer> downFloorStops, WorkAssignment workAssignment, ElevatorStateUpdate elevatorStateUpdate, int retId) throws RemoteException {


        Direction direction = elevatorStateUpdate.getDirection();
        int curFloor = elevatorStateUpdate.getFloor();

//        floorStops = determineFloorStops(prevFloorStopsCopy, workAssignment, elevatorStates.get(retId));
        if (workAssignment.getDirection() == Direction.UP) {

            if (workAssignment.getServiceFloor() > curFloor || direction == Direction.DOWN) {
                int index = 0;
                while (index < upfloorStops.size()) {
                    if (workAssignment.getServiceFloor() < upfloorStops.get(index)) {
                        upfloorStops.add(index, workAssignment.getServiceFloor());
                        break;
                    }
                    index++;
                }
                if (index == upfloorStops.size()) {
                    upfloorStops.add(workAssignment.getServiceFloor());
                }
                while (index < upfloorStops.size()) {
                    if (workAssignment.getDestinationFloor() < upfloorStops.get(index)) {
                        upfloorStops.add(index, workAssignment.getDestinationFloor());
                        break;
                    }
                    index++;
                }
                if (index == upfloorStops.size()) {
                    upfloorStops.add(workAssignment.getDestinationFloor());
                }
            } else {
                upfloorStops.add(workAssignment.getServiceFloor());
                upfloorStops.add(workAssignment.getDestinationFloor());
            }
            sharedState.setElevatorUpStopQueue(retId, upfloorStops);
            sharedState.setElevatorStopQueue(retId, upfloorStops);
        } else {
            if (workAssignment.getServiceFloor() < curFloor || direction == Direction.UP) {
                int index = 0;
                while (index < downFloorStops.size()) {
                    if (workAssignment.getServiceFloor() > downFloorStops.get(index)) {
                        downFloorStops.add(index, workAssignment.getServiceFloor());
                        break;
                    }
                    index++;
                }
                if (index == downFloorStops.size()) {
                    downFloorStops.add(workAssignment.getServiceFloor());
                }
                while (index < downFloorStops.size()) {
                    if (workAssignment.getDestinationFloor() > downFloorStops.get(index)) {
                        downFloorStops.add(index, workAssignment.getDestinationFloor());
                        break;
                    }
                    index++;
                }
                if (index == downFloorStops.size()) {
                    downFloorStops.add(workAssignment.getDestinationFloor());
                }
            }else{
                downFloorStops.add(workAssignment.getServiceFloor());
                downFloorStops.add(workAssignment.getDestinationFloor());
            }
            sharedState.setElevatorDownStopQueue(retId, downFloorStops);
            sharedState.setElevatorStopQueue(retId, downFloorStops);
        }
    }
    private int checkFullOverLap(WorkAssignment workAssignment, HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments, HashMap<Integer,ElevatorStateUpdate> elevatorStates){
        for (int elevatorId : elevatorStates.keySet()) {
            ConcurrentLinkedDeque<WorkAssignment> elevatorWorkAssignments = workAssignments.get(elevatorId);
            Direction direction = elevatorStates.get(elevatorId).getDirection();
            int curFloor = elevatorStates.get(elevatorId).getFloor();
            int maxFloor = Integer.MIN_VALUE;
            int minFloor = Integer.MAX_VALUE;
            Direction workDirection = workAssignment.getDirection();

            if (workDirection != direction){
                continue;
            }
            if (direction == Direction.UP && workAssignment.getServiceFloor() < curFloor){
                continue;
            }
            if (direction == Direction.DOWN && workAssignment.getServiceFloor() > curFloor){
                continue;
            }

            for (WorkAssignment elevatorWorkAssignment : elevatorWorkAssignments) {
                if (direction == Direction.UP) {
                    if (elevatorWorkAssignment.getDestinationFloor() > maxFloor) {
                        maxFloor = elevatorWorkAssignment.getDestinationFloor();
                    }
                } else {
//                    maxFloor = Integer.MAX_VALUE;
                    if (elevatorWorkAssignment.getDestinationFloor() < minFloor) {
                        minFloor = elevatorWorkAssignment.getDestinationFloor();
                    }
                }

            }


            if (direction == Direction.UP) {
                if (workAssignment.getDestinationFloor() > curFloor && workAssignment.getDestinationFloor() < maxFloor) {
                    sharedState.addWorkAssignment(elevatorId, workAssignment);
                    return elevatorId;
                }
            } else {
                maxFloor = minFloor;
                if (workAssignment.getDestinationFloor() < curFloor && workAssignment.getDestinationFloor() > maxFloor) {
                    sharedState.addWorkAssignment(elevatorId, workAssignment);
                    return elevatorId;
                }
            }
        }
        return -1;
    }


    private int checkPartialOverlap(WorkAssignment workAssignment, HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments, HashMap<Integer,ElevatorStateUpdate> elevatorStates){
        for (int elevatorId : elevatorStates.keySet()) {
            ConcurrentLinkedDeque<WorkAssignment> elevatorWorkAssignments = workAssignments.get(elevatorId);
            Direction direction = elevatorStates.get(elevatorId).getDirection();
            int curFloor = elevatorStates.get(elevatorId).getFloor();
            int maxFloor = Integer.MIN_VALUE;
            Direction workDirection = workAssignment.getDirection();

            if (workDirection != direction){
                continue;
            }
            if (direction == Direction.UP && workAssignment.getServiceFloor() < curFloor){
                continue;
            }
            if (direction == Direction.DOWN && workAssignment.getServiceFloor() > curFloor){
                continue;
            }

            for (WorkAssignment elevatorWorkAssignment : elevatorWorkAssignments) {
                if (direction == Direction.UP) {
                    if (elevatorWorkAssignment.getDestinationFloor() > maxFloor) {
                        maxFloor = elevatorWorkAssignment.getDestinationFloor();
                    }
                } else {
                    maxFloor = Integer.MAX_VALUE;
                    if (elevatorWorkAssignment.getDestinationFloor() < maxFloor) {
                        maxFloor = elevatorWorkAssignment.getDestinationFloor();
                    }
                }

            }

            if (direction == Direction.UP) {
                if (workAssignment.getServiceFloor() > curFloor ) {
                    sharedState.addWorkAssignment(elevatorId, workAssignment);
                    return elevatorId;
                }
            } else {
                if (workAssignment.getServiceFloor() < curFloor ) {
                    sharedState.addWorkAssignment(elevatorId, workAssignment);
                    return elevatorId;
                }
            }
        }
        return -1;
    }



    private int assignToElevatorWithSmallestQueue(WorkAssignment workAssignment, HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments, HashMap<Integer,ElevatorStateUpdate> elevatorStates){
        int min = Integer.MAX_VALUE;
        int elevatorId = -1;
        for (int eleId : workAssignments.keySet()) {
            if (workAssignments.get(eleId).size() < min && !sharedState.getElevatorStates().get(eleId).isFull()){
                min = workAssignments.get(eleId).size();
                elevatorId = eleId;
            }
        }
        sharedState.addWorkAssignment(elevatorId, workAssignment);
        return elevatorId;
    }

    public ArrayList<ArrayList<Integer>> allocateImproved(WorkAssignment workAssignment) throws RemoteException {
        System.out.println("ALLOCATE: " + workAssignment);
        HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments = sharedState.getWorkAssignments();
        HashMap<Integer, ElevatorStateUpdate> elevatorStates = sharedState.getElevatorStates();
        ArrayList<Integer> floorStops = new ArrayList<>();


        int retId = checkFullOverLap(workAssignment, workAssignments, elevatorStates);
        if (retId == -1) {
            retId = checkPartialOverlap(workAssignment, workAssignments, elevatorStates);
        }
        if (retId == -1) {
            retId = assignToElevatorWithSmallestQueue(workAssignment, workAssignments, elevatorStates);
        }
        ArrayList<Integer> upfloorStops = sharedState.getElevatorUpStopQueue(retId);
        ArrayList<Integer> downFloorStops = sharedState.getElevatorDownStopQueue(retId);

        Direction direction = elevatorStates.get(retId).getDirection();
        int curFloor = elevatorStates.get(retId).getFloor();
        determineFloorStops(upfloorStops, downFloorStops, workAssignment, elevatorStates.get(retId), retId);
        ArrayList<ArrayList<Integer>> ret = new ArrayList<>();
        ret.add(upfloorStops);
        ret.add(downFloorStops);
        return ret;

//        floorStops = determineFloorStops(prevFloorStopsCopy, workAssignment, elevatorStates.get(retId));
//        if (workAssignment.getDirection() == Direction.UP) {
//
//            if (workAssignment.getServiceFloor() > curFloor || direction == Direction.DOWN) {
//                int index = 0;
//                while (index < upfloorStops.size()) {
//                    if (workAssignment.getServiceFloor() < upfloorStops.get(index)) {
//                        upfloorStops.add(index, workAssignment.getServiceFloor());
//                        break;
//                    }
//                    index++;
//                }
//                if (index == upfloorStops.size()) {
//                    upfloorStops.add(workAssignment.getServiceFloor());
//                }
//                while (index < upfloorStops.size()) {
//                    if (workAssignment.getDestinationFloor() < upfloorStops.get(index)) {
//                        upfloorStops.add(index, workAssignment.getDestinationFloor());
//                        break;
//                    }
//                    index++;
//                }
//                if (index == upfloorStops.size()) {
//                    upfloorStops.add(workAssignment.getDestinationFloor());
//                }
//            } else {
//                upfloorStops.add(workAssignment.getServiceFloor());
//                upfloorStops.add(workAssignment.getDestinationFloor());
//            }
//            sharedState.setElevatorUpStopQueue(retId, upfloorStops);
//            sharedState.setElevatorStopQueue(retId, upfloorStops);
//            return upfloorStops;
//        } else {
//            if (workAssignment.getServiceFloor() < curFloor || direction == Direction.UP) {
//                int index = 0;
//                while (index < downFloorStops.size()) {
//                    if (workAssignment.getServiceFloor() > downFloorStops.get(index)) {
//                        downFloorStops.add(index, workAssignment.getServiceFloor());
//                        break;
//                    }
//                    index++;
//                }
//                if (index == downFloorStops.size()) {
//                    downFloorStops.add(workAssignment.getServiceFloor());
//                }
//                while (index < downFloorStops.size()) {
//                    if (workAssignment.getDestinationFloor() > downFloorStops.get(index)) {
//                        downFloorStops.add(index, workAssignment.getDestinationFloor());
//                        break;
//                    }
//                    index++;
//                }
//                if (index == downFloorStops.size()) {
//                    downFloorStops.add(workAssignment.getDestinationFloor());
//                }
//            }else{
//                downFloorStops.add(workAssignment.getServiceFloor());
//                downFloorStops.add(workAssignment.getDestinationFloor());
//            }
//            sharedState.setElevatorDownStopQueue(retId, downFloorStops);
//            sharedState.setElevatorStopQueue(retId, downFloorStops);
//            return downFloorStops;
//        }
//        sharedState.setElevatorStopQueue(retId, floorStops);
//        return floorStops;

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
        return id == elevatorId;
    }
}

