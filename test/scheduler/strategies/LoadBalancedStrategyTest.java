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
    private LoadBalancedStrategy loadBalancedStrategy;

    @BeforeEach
    void setUp() throws RemoteException {
        workAssignments = new ConcurrentLinkedDeque<>();
        elevatorState = new ElevatorStateUpdate(elevatorId, 1, Direction.UP, new ArrayList<>(), false);
        sharedState = new SubSystemSharedState();
        ArrayList<Integer> floorStopQueue = new ArrayList<>();
        sharedState.addElevatorState(elevatorId, elevatorState);
        sharedState.setElevatorStopQueues(elevatorId, floorStopQueue);
        loadBalancedStrategy = new LoadBalancedStrategy(sharedState);
    }

    @Test
    void testAddToEmptyQueue(){
        workAssignments.add(new WorkAssignment(3, 8, "123", Direction.UP, "request1", "localhost", 8080, Signal.WORK_REQ, 0));
        ArrayList<Integer> ret = loadBalancedStrategy.determineFloorStops(workAssignments, elevatorState);
        assertEquals(2, ret.size());
        assertEquals(3, ret.get(0));
        assertEquals(8, ret.get(1));
    }

    @Test
    void testTwoRequestsInDirectionOfTravel() {
        //Setup elevatorState's floorStopQueue
        HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments = new HashMap<>();
        ConcurrentLinkedDeque<WorkAssignment> workAssignmentsQueue = new ConcurrentLinkedDeque<>();

        // requests for testing
        WorkAssignment wa1 = new WorkAssignment(2, 3, "123", Direction.UP, "request1", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa2 = new WorkAssignment(3,2,"234", Direction.DOWN, "request2", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa3 = new WorkAssignment(2, 9, "345", Direction.UP, "request3", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa4 = new WorkAssignment(9, 1, "345", Direction.DOWN, "request3", "localhost", 8080, Signal.WORK_REQ, 0);

        // Adding requests to work assignment queue
        workAssignmentsQueue.add(wa1);
        workAssignmentsQueue.add(wa2);
        workAssignmentsQueue.add(wa3);
        workAssignmentsQueue.add(wa4);

        //Adding queue to hashmap
        workAssignments.put(elevatorId, workAssignmentsQueue);

        //Setting shared state work assignments
        sharedState.setWorkAssignments(workAssignments);

        //Yasir: Fix this portion - no idea how allocateImproved works
        ArrayList<Integer> upQueueTest = loadBalancedStrategy.determineFloorStops(workAssignmentsQueue,elevatorState);
        ArrayList<Integer> downQueueTest = loadBalancedStrategy.determineFloorStops(workAssignmentsQueue,elevatorState);


        assertEquals(4, upQueueTest.size());
        assertEquals(4, downQueueTest.size());
        assertEquals(2, upQueueTest.get(0));
        assertEquals(3, upQueueTest.get(1));
        assertEquals(3, downQueueTest.get(0));
        assertEquals(2, downQueueTest.get(1));
    }

    @Test
    void testRequestsInUpDirection(){
        //Setup elevatorState's floorStopQueue
        HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments = new HashMap<>();
        ConcurrentLinkedDeque<WorkAssignment> workAssignmentsQueue = new ConcurrentLinkedDeque<>();

        // requests for testing - expected order should be 1, 4, 6, 7, 8, 10, 11
        WorkAssignment wa1 = new WorkAssignment(4, 10, "123", Direction.UP, "request1", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa2 = new WorkAssignment(6,7,"234", Direction.UP, "request2", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa3 = new WorkAssignment(1, 5, "345", Direction.UP, "request3", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa4 = new WorkAssignment(8, 11, "345", Direction.UP, "request3", "localhost", 8080, Signal.WORK_REQ, 0);

        // Adding requests to work assignment queue
        workAssignmentsQueue.add(wa1);
        workAssignmentsQueue.add(wa2);
        workAssignmentsQueue.add(wa3);
        workAssignmentsQueue.add(wa4);

        //Adding queue to hashmap
        workAssignments.put(elevatorId, workAssignmentsQueue);

        //Setting shared state work assignments
        sharedState.setWorkAssignments(workAssignments);
    }

    @Test
    void testRequestsInDownDirection(){
        //Setup elevatorState's floorStopQueue
        HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments = new HashMap<>();
        ConcurrentLinkedDeque<WorkAssignment> workAssignmentsQueue = new ConcurrentLinkedDeque<>();

        // requests for testing - expected order should be 11, 10, 8, 7, 6, 5, 4, 1
        WorkAssignment wa1 = new WorkAssignment(10, 4, "123", Direction.DOWN, "request1", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa2 = new WorkAssignment(7,6,"234", Direction.DOWN, "request2", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa3 = new WorkAssignment(5, 1, "345", Direction.DOWN, "request3", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa4 = new WorkAssignment(11, 8, "345", Direction.DOWN, "request3", "localhost", 8080, Signal.WORK_REQ, 0);

        // Adding requests to work assignment queue
        workAssignmentsQueue.add(wa1);
        workAssignmentsQueue.add(wa2);
        workAssignmentsQueue.add(wa3);
        workAssignmentsQueue.add(wa4);

        //Adding queue to hashmap
        workAssignments.put(elevatorId, workAssignmentsQueue);

        //Setting shared state work assignments
        sharedState.setWorkAssignments(workAssignments);
        //Yasir: Fix this portion - no idea how allocateImproved works
    }

    @Test
    void testOverlappingRequestsInUpDirection1(){
        //Setup elevatorState's floorStopQueue
        HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments = new HashMap<>();
        ConcurrentLinkedDeque<WorkAssignment> workAssignmentsQueue = new ConcurrentLinkedDeque<>();

        // requests for testing - expected order should be 4,5,6,6,7,7,10,10,11,11,12,13
        WorkAssignment wa1 = new WorkAssignment(4, 6, "123", Direction.UP, "request1", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa2 = new WorkAssignment(5,6,"234", Direction.UP, "request2", "localhost", 8080, Signal.WORK_REQ, 0);

        WorkAssignment wa3 = new WorkAssignment(7, 10, "345", Direction.UP, "request3", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa4 = new WorkAssignment(7, 10, "375", Direction.UP, "request4", "localhost", 8080, Signal.WORK_REQ, 0);

        WorkAssignment wa5 = new WorkAssignment(11, 12, "435", Direction.UP, "request5", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa6 = new WorkAssignment(11, 13, "545", Direction.UP, "request6", "localhost", 8080, Signal.WORK_REQ, 0);


        // Adding requests to work assignment queue
        workAssignmentsQueue.add(wa1);
        workAssignmentsQueue.add(wa2);
        workAssignmentsQueue.add(wa3);
        workAssignmentsQueue.add(wa4);
        workAssignmentsQueue.add(wa5);
        workAssignmentsQueue.add(wa6);

        //Adding queue to hashmap
        workAssignments.put(elevatorId, workAssignmentsQueue);

        //Setting shared state work assignments
        sharedState.setWorkAssignments(workAssignments);
        //Yasir: Fix this portion - no idea how allocateImproved works
    }

    @Test
    void testOverlappingRequestsInDownDirection1(){
        //Setup elevatorState's floorStopQueue
        HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments = new HashMap<>();
        ConcurrentLinkedDeque<WorkAssignment> workAssignmentsQueue = new ConcurrentLinkedDeque<>();

        // requests for testing - expected order should be 13,12,11,11,10,10,7,7,6,6,5,4
        WorkAssignment wa1 = new WorkAssignment(13, 11, "123", Direction.DOWN, "request1", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa2 = new WorkAssignment(12,11,"234", Direction.DOWN, "request2", "localhost", 8080, Signal.WORK_REQ, 0);

        WorkAssignment wa3 = new WorkAssignment(10, 7, "345", Direction.DOWN, "request3", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa4 = new WorkAssignment(10, 7, "375", Direction.DOWN, "request4", "localhost", 8080, Signal.WORK_REQ, 0);

        WorkAssignment wa5 = new WorkAssignment(6, 5, "435", Direction.DOWN, "request5", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa6 = new WorkAssignment(6, 4, "545", Direction.DOWN, "request6", "localhost", 8080, Signal.WORK_REQ, 0);

        // Adding requests to work assignment queue
        workAssignmentsQueue.add(wa1);
        workAssignmentsQueue.add(wa2);
        workAssignmentsQueue.add(wa3);
        workAssignmentsQueue.add(wa4);
        workAssignmentsQueue.add(wa5);
        workAssignmentsQueue.add(wa6);

        //Adding queue to hashmap
        workAssignments.put(elevatorId, workAssignmentsQueue);

        //Setting shared state work assignments
        sharedState.setWorkAssignments(workAssignments);
        //Yasir: Fix this portion - no idea how allocateImproved works
    }

    @Test
    void testOverlappingRequestsInUpDirection2(){
        //Setup elevatorState's floorStopQueue
        HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments = new HashMap<>();
        ConcurrentLinkedDeque<WorkAssignment> workAssignmentsQueue = new ConcurrentLinkedDeque<>();

        // requests for testing - expected order should be 2,3,3,4,5,6,6,7,7,8
        WorkAssignment wa1 = new WorkAssignment(2, 3, "123", Direction.UP, "request1", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa2 = new WorkAssignment(3,4,"234", Direction.UP, "request2", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa3 = new WorkAssignment(5, 6, "345", Direction.UP, "request3", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa4 = new WorkAssignment(6, 7, "375", Direction.UP, "request4", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa5 = new WorkAssignment(7, 8, "435", Direction.UP, "request5", "localhost", 8080, Signal.WORK_REQ, 0);

        // Adding requests to work assignment queue
        workAssignmentsQueue.add(wa1);
        workAssignmentsQueue.add(wa2);
        workAssignmentsQueue.add(wa3);
        workAssignmentsQueue.add(wa4);
        workAssignmentsQueue.add(wa5);

        //Adding queue to hashmap
        workAssignments.put(elevatorId, workAssignmentsQueue);

        //Setting shared state work assignments
        sharedState.setWorkAssignments(workAssignments);
        //Yasir: Fix this portion - no idea how allocateImproved works
    }

    @Test
    void testOverlappingRequestsInDownDirection2(){
        //Setup elevatorState's floorStopQueue
        HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments = new HashMap<>();
        ConcurrentLinkedDeque<WorkAssignment> workAssignmentsQueue = new ConcurrentLinkedDeque<>();

        // requests for testing - expected order should be 8,7,7,6,5,4,4,3,3,2
        WorkAssignment wa1 = new WorkAssignment(8, 7, "123", Direction.DOWN, "request1", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa2 = new WorkAssignment(7,6,"234", Direction.DOWN, "request2", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa3 = new WorkAssignment(5, 4, "345", Direction.DOWN, "request3", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa4 = new WorkAssignment(4, 3, "375", Direction.DOWN, "request4", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa5 = new WorkAssignment(3, 2, "435", Direction.DOWN, "request5", "localhost", 8080, Signal.WORK_REQ, 0);

        // Adding requests to work assignment queue
        workAssignmentsQueue.add(wa1);
        workAssignmentsQueue.add(wa2);
        workAssignmentsQueue.add(wa3);
        workAssignmentsQueue.add(wa4);
        workAssignmentsQueue.add(wa5);

        //Adding queue to hashmap
        workAssignments.put(elevatorId, workAssignmentsQueue);

        //Setting shared state work assignments
        sharedState.setWorkAssignments(workAssignments);
        //Yasir: Fix this portion - no idea how allocateImproved works
    }

    @Test
    void testOverlappingRequestsInUpDirection3(){
        //Setup elevatorState's floorStopQueue
        HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments = new HashMap<>();
        ConcurrentLinkedDeque<WorkAssignment> workAssignmentsQueue = new ConcurrentLinkedDeque<>();

        // requests for testing - expected order should be 2,3,3,4,5,6,6,7,7,8
        WorkAssignment wa1 = new WorkAssignment(2, 3, "123", Direction.UP, "request1", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa2 = new WorkAssignment(3,4,"234", Direction.UP, "request2", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa3 = new WorkAssignment(5, 6, "345", Direction.UP, "request3", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa4 = new WorkAssignment(6, 7, "375", Direction.UP, "request4", "localhost", 8080, Signal.WORK_REQ, 0);
        WorkAssignment wa5 = new WorkAssignment(7, 8, "435", Direction.UP, "request5", "localhost", 8080, Signal.WORK_REQ, 0);

        // Adding requests to work assignment queue
        workAssignmentsQueue.add(wa1);
        workAssignmentsQueue.add(wa2);
        workAssignmentsQueue.add(wa3);
        workAssignmentsQueue.add(wa4);
        workAssignmentsQueue.add(wa5);

        //Adding queue to hashmap
        workAssignments.put(elevatorId, workAssignmentsQueue);

        //Setting shared state work assignments
        sharedState.setWorkAssignments(workAssignments);
        //Yasir: Fix this portion - no idea how allocateImproved works
    }
}
