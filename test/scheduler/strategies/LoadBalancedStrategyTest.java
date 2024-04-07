package scheduler.strategies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Direction;
import util.ElevatorStateUpdate;
import util.Messages.Signal;
import util.SubSystemSharedState;
import util.WorkAssignment;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoadBalancedStrategyTest {

    private ElevatorStateUpdate elevatorState;

    private ConcurrentLinkedDeque<WorkAssignment> workAssignments;
    private SubSystemSharedState sharedState;
    private final int elevatorId = 1;
    private int currentFloor = 1;
    private final boolean isFull = false;
    private ArrayList<Integer> floorStopQueue;

    @BeforeEach
    void setUp() throws RemoteException {
        workAssignments = new ConcurrentLinkedDeque<>();
        elevatorState = new ElevatorStateUpdate(elevatorId, currentFloor, Direction.UP, new ArrayList<>(), isFull);
        sharedState = new SubSystemSharedState();
        floorStopQueue = new ArrayList<>();
        sharedState.addElevatorState(elevatorId, elevatorState);
        sharedState.setElevatorStopQueues(elevatorId, floorStopQueue);
    }

    @Test
    void testAddToEmptyQueue(){
        //Setup elevatorState's floorStopQueue


        //setup dummy work assignment
        workAssignments.add(new WorkAssignment(3, 8, "123", Direction.UP, "request1", "localhost", 8080, Signal.WORK_REQ, 0));


        LoadBalancedStrategy loadBalancedStrategy = new LoadBalancedStrategy(sharedState);

//        ArrayList<Integer> ret = loadBalancedStrategy.determineFloorStops(workAssignments, elevatorState);

//        assertEquals(2, ret.size());
//        assertEquals(3, ret.get(0));
//        assertEquals(8, ret.get(1));
    }

    @Test
    void testTwoRequestsInDirectionOfTravel() throws RemoteException {
//        //Setup elevatorState's floorStopQueue
//        ArrayList<Integer> floorStopQueue = new ArrayList<>();
//        sharedState.addElevatorState(elevatorId, elevatorState);
//        sharedState.setElevatorStopQueue(elevatorId, floorStopQueue);
//
//        WorkAssignment wa1 = new WorkAssignment(2, 3, "123", Direction.UP, "request1", "localhost", 8080, Signal.WORK_REQ, 0);
//        WorkAssignment wa2 = new WorkAssignment(3,2,"234", Direction.DOWN, "request2", "localhost", 8080, Signal.WORK_REQ, 0);
//        WorkAssignment wa3 = new WorkAssignment(2, 9, "345", Direction.UP, "request3", "localhost", 8080, Signal.WORK_REQ, 0);
//        WorkAssignment wa4 = new WorkAssignment(9, 1, "345", Direction.DOWN, "request3", "localhost", 8080, Signal.WORK_REQ, 0);
//        HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments = new HashMap<>();
//        workAssignments.put(elevatorId, new ConcurrentLinkedDeque<>());
//        workAssignments.get(elevatorId).add(wa1);
//        workAssignments.get(elevatorId).add(wa2);
//        workAssignments.get(elevatorId).add(wa3);
//        workAssignments.get(elevatorId).add(wa4);
//        sharedState.setWorkAssignments(workAssignments);
//        LoadBalancedStrategy loadBalancedStrategy = new LoadBalancedStrategy(sharedState);
//        ConcurrentLinkedDeque<WorkAssignment> workAssignments1 = workAssignments.get(elevatorId);
////        assertEquals(2, sharedState.getElevatorStopQueue(elevatorId).size());
////        assertEquals(2, sharedState.getElevatorStopQueue(elevatorId).get(0));
////        assertEquals(3, sharedState.getElevatorStopQueue(elevatorId).get(1));
//        ArrayList<Integer> retUp = loadBalancedStrategy.determineFloorStops(workAssignments1,elevatorState);
//        ArrayList<Integer> retDown = loadBalancedStrategy.determineFloorStops(workAssignments1,elevatorState);
//
////        loadBalancedStrategy.determineFloorStops(floorStopQueue, wa1, elevatorState);
//        assertEquals(4, retUp.size());
//        assertEquals(4, retDown.size());
//        assertEquals(2, retUp.get(0));
//        assertEquals(3, retUp.get(1));
//        assertEquals(3, retDown.get(0));
//        assertEquals(2, retDown.get(1));
//



//        assertEquals(2, sharedState.getElevatorStopQueue(elevatorId).get(0));
//        assertEquals(3, sharedState.getElevatorStopQueue(elevatorId).get(1));
//        assertEquals(3, sharedState.getElevatorStopQueue(elevatorId).get(2));
//        assertEquals(2, sharedState.getElevatorStopQueue(elevatorId).get(3));

    }

//    void testRequestsInDirectionOfTravel() throws RemoteException {
//        //Setup elevatorState's floorStopQueue
//        ArrayList<Integer> floorStopQueue = new ArrayList<>();
//        sharedState.addElevatorState(elevatorId, elevatorState);
//        sharedState.setElevatorStopQueue(elevatorId, floorStopQueue);
//
//        //setup dummy work assignment
//        WorkAssignment wa1 = new WorkAssignment(2, 3, "123", Direction.UP, "request1", "localhost", 8080, Signal.WORK_REQ, 0);
//        WorkAssignment wa2 = new WorkAssignment(4, 6, "123", Direction.UP, "request2", "localhost", 8080, Signal.WORK_REQ, 0);
//        HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments = new HashMap<>();
//        workAssignments.put(elevatorId, new ConcurrentLinkedDeque<>());
//        workAssignments.get(elevatorId).add(wa1);
//        workAssignments.get(elevatorId).add(wa2);
//        sharedState.setWorkAssignments(workAssignments);
//        LoadBalancedStrategy loadBalancedStrategy = new LoadBalancedStrategy(sharedState);
//
//        loadBalancedStrategy.determineFloorStops(floorStopQueue, wa1, elevatorState);
//        assertEquals(2, sharedState.getElevatorStopQueue(elevatorId).size());
//        assertEquals(3, sharedState.getElevatorStopQueue(elevatorId).get(0));
//        assertEquals(9, sharedState.getElevatorStopQueue(elevatorId).get(1));
//        loadBalancedStrategy.determineFloorStops(floorStopQueue, wa2, elevatorState);
//
//
//        assertEquals(4, sharedState.getElevatorStopQueue(elevatorId).size());
//        assertEquals(3, sharedState.getElevatorStopQueue(elevatorId).get(0));
//        assertEquals(4, sharedState.getElevatorStopQueue(elevatorId).get(1));
//        assertEquals(6, sharedState.getElevatorStopQueue(elevatorId).get(2));
//        assertEquals(9, sharedState.getElevatorStopQueue(elevatorId).get(3));
//    }
}
