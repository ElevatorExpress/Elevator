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
    class Graph {
        private Map<Integer, List<Integer>> adjList = new HashMap<>();

        public void addEdge(int from, int to) {
            if (!adjList.containsKey(from)) {
                adjList.put(from, new ArrayList<>());
            }
            adjList.get(from).add(to);
        }

        public Map<Integer, List<Integer>> getAdjList() {
            return adjList;
        }
    }

    private Graph constructFloorGraph(List<WorkAssignment> sortedAssignments) {
        Graph graph = new Graph();
        for (WorkAssignment assignment : sortedAssignments) {
            graph.addEdge(assignment.getServiceFloor(), assignment.getDestinationFloor());
        }
        return graph;
    }
    private List<WorkAssignment> prioritizeAssignments(ConcurrentLinkedDeque<WorkAssignment> assignments, ElevatorStateUpdate state) {
        return assignments.stream()
                .sorted(Comparator.comparingInt((WorkAssignment a) ->
                        Math.abs(a.getServiceFloor()- state.getDestinationFloor())))
                .collect(Collectors.toList());
    }
    private ArrayList<Integer> topologicalSort(Graph graph) {
        ArrayList<Integer> result = new ArrayList<>();
        Map<Integer, Boolean> visited = new HashMap<>();
        Stack<Integer> stack = new Stack<>();

        for (Integer node : graph.getAdjList().keySet()) {
            if (visited.get(node) == null)
                topologicalSortUtil(node, visited, stack, graph);
        }

        while (!stack.empty()) {
            result.add(stack.pop());
        }

        return result;
    }

    private void topologicalSortUtil(int node, Map<Integer, Boolean> visited, Stack<Integer> stack, Graph graph) {
        visited.put(node, true);

        for (Integer neighbor : graph.getAdjList().getOrDefault(node, new ArrayList<>())) {
            if (visited.get(neighbor) == null) {
                topologicalSortUtil(neighbor, visited, stack, graph);
            }
        }
        stack.push(node);
    }
    public ArrayList<Integer> determineFloorStops(ConcurrentLinkedDeque<WorkAssignment> workAssignments, ElevatorStateUpdate elevatorState) {
        List<WorkAssignment> sortedAssignments = prioritizeAssignments(workAssignments, elevatorState);
        Graph floorGraph = constructFloorGraph(sortedAssignments);
        ArrayList<Integer> floorStops = topologicalSort(floorGraph);
        return floorStops;
    }

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
//        for (int elevatorId : sharedState.getElevatorStates().keySet()) {
//            if (sharedState.getElevatorStates().keySet().size() == 1) sharedState.addWorkAssignment(elevatorId, workAssignment);
//            else if (sharedState.getWorkAssignments().get(elevatorId).isEmpty()) {
//                sharedState.addWorkAssignment(elevatorId, workAssignment);
//                break;
//            }
//            else if (smallestAssignment(elevatorId, sharedState.getWorkAssignments())) {
//                sharedState.addWorkAssignment(elevatorId, workAssignment);
//                break;
//            }
//        }
        allocateImproved(workAssignment);
    }




//    public ArrayList<ArrayList<Integer>,ArrayList<Integer>> determineFloorStops(ArrayList<Integer> upQ, ArrayList<Integer> downQ, WorkAssignment workAssignment, ElevatorStateUpdate elevatorStateUpdate){
//        Direction direction = elevatorStateUpdate.getDirection();
//        int curFloor = elevatorStateUpdate.getFloor();
//        if (direction == Direction.UP){
//            if (workAssignment.getServiceFloor() > curFloor){
//                upQ.add(workAssignment.getServiceFloor());
//                upQ.add(workAssignment.getDestinationFloor());
//            }
//
//
//            //ToDo: Handle else
//        }else{
//            if (workAssignment.getServiceFloor() < curFloor){
//                downQ.add(workAssignment.getServiceFloor());
//                downQ.add(workAssignment.getDestinationFloor());
//            }
//        }
//
//        //Find the correct place in the floorStops queue to insert the pickup and drop off stops
////
////        if (direction == Direction.UP) {
////            if (floorStops.isEmpty()) {
////                floorStops.add(workAssignment.getServiceFloor());
////                floorStops.add(workAssignment.getDestinationFloor());
////                return floorStops;
////            }
////            int index = 0;
////            while (index < floorStops.size()) {
////                if (workAssignment.getServiceFloor() < floorStops.get(index) && workAssignment.getServiceFloor() > curFloor) {
////                    floorStops.add(index, workAssignment.getServiceFloor());
////                    break;
////                }
////                index++;
////            }
////            if (index == floorStops.size()) {
////                floorStops.add(workAssignment.getServiceFloor());
////                floorStops.add(workAssignment.getDestinationFloor());
////                return floorStops;
////            }
////            while (index < floorStops.size()) {
////                if (workAssignment.getDestinationFloor() < floorStops.get(index) && workAssignment.getDestinationFloor() > curFloor) {
////                    floorStops.add(index, workAssignment.getDestinationFloor());
////                    return floorStops;
////                }
////                index++;
////            }
////            if (index == floorStops.size()) {
////                floorStops.add(workAssignment.getDestinationFloor());
////                return floorStops;
////            }
////        }else{
////            if (floorStops.isEmpty()) {
////                floorStops.add(workAssignment.getServiceFloor());
////                floorStops.add(workAssignment.getDestinationFloor());
////                return floorStops;
////            }
////            int index = 0;
////            while (index < floorStops.size()) {
////                if (workAssignment.getServiceFloor() > floorStops.get(index) && workAssignment.getServiceFloor() < curFloor) {
////                    floorStops.add(index, workAssignment.getServiceFloor());
////                    break;
////                }
////                index++;
////            }
////            if (index == floorStops.size()) {
////                floorStops.add(workAssignment.getServiceFloor());
////                floorStops.add(workAssignment.getDestinationFloor());
////                return floorStops;
////            }
////            while (index < floorStops.size()) {
////                if (workAssignment.getDestinationFloor() > floorStops.get(index) && workAssignment.getDestinationFloor() < curFloor) {
////                    floorStops.add(index, workAssignment.getDestinationFloor());
////                    return floorStops;
////                }
////                index++;
////            }
////            if (index == floorStops.size()) {
////                floorStops.add(workAssignment.getDestinationFloor());
////                return floorStops;
////            }
////        }
////
////        floorStops.add(workAssignment.getServiceFloor());
////        floorStops.add(workAssignment.getDestinationFloor());
////        return floorStops;
//    }
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

    public ArrayList<Integer> allocateImproved(WorkAssignment workAssignment) throws RemoteException {
        System.out.println("ALLOCATE: " + workAssignment);
        HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments = sharedState.getWorkAssignments();
        HashMap<Integer, ElevatorStateUpdate> elevatorStates = sharedState.getElevatorStates();
        ArrayList<Integer> floorStops = new ArrayList<>();


//        if (checkFullOverLap(workAssignment, workAssignments, elevatorStates)){
//            return;
//        }
//
//        if (checkPartialOverlap(workAssignment, workAssignments, elevatorStates)){
//            return;
//        }
//
//        if (assignToElevatorWithSmallestQueue(workAssignment, workAssignments, elevatorStates)){
//            return;
//        }
//
        int retId = checkFullOverLap(workAssignment, workAssignments, elevatorStates);
        if (retId == -1) {
            retId = checkPartialOverlap(workAssignment, workAssignments, elevatorStates);
        }
        if (retId == -1) {
            retId = assignToElevatorWithSmallestQueue(workAssignment, workAssignments, elevatorStates);
        }
        if (retId == -1) {
            System.err.println("\n\nNo elevator found\n\n");
        }
        ArrayList<Integer> prevFloorStops = elevatorStates.get(retId).getFloorStopQueue();
        ArrayList<Integer> prevFloorStopsCopy = sharedState.getElevatorStopQueue(retId);
        ArrayList<Integer> upfloorStops = sharedState.getElevatorUpStopQueue(retId);
        ArrayList<Integer> downFloorStops = sharedState.getElevatorDownStopQueue(retId);

        Direction direction = elevatorStates.get(retId).getDirection();
        int curFloor = elevatorStates.get(retId).getFloor();

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
            return upfloorStops;
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
            return downFloorStops;
        }
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

