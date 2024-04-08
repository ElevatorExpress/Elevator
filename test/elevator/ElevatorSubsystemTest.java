package elevator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import util.*;
import util.Messages.Signal;

import java.net.*;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

class ElevatorSubsystemTest {
    static SubSystemSharedState schedulerSharedObject;
    static SubSystemSharedStateInterface elevatorSharedObject;

    @BeforeAll
    static void createElevatorSystem() throws RemoteException, MalformedURLException, AlreadyBoundException, UnknownHostException, NotBoundException {

        schedulerSharedObject = new SubSystemSharedState();
        LocateRegistry.createRegistry(1099);
        Naming.bind("SharedObjectTest", schedulerSharedObject);
        elevatorSharedObject = (SubSystemSharedStateInterface) Naming.lookup("rmi://localhost/SharedObjectTest");

        HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> testWorkAssignments = new HashMap<>();
        ConcurrentLinkedDeque<WorkAssignment> testWorkAssignmentsQueue = new ConcurrentLinkedDeque<>();
        testWorkAssignmentsQueue.add(new WorkAssignment(
                        1,
                        2,
                        "6",
                        Direction.UP,
                        UUID.randomUUID().toString(),
                        InetAddress.getLocalHost().toString(),
                        8082,
                        Signal.WORK_REQ,
                        0
                )
        );

        testWorkAssignments.put(1, testWorkAssignmentsQueue);
        schedulerSharedObject.setWorkAssignments(testWorkAssignments);
    }
    @Test
    void receiveMessage() throws RemoteException, InterruptedException {
        ElevatorSubsystem  testElevator = new ElevatorSubsystem(1, null);
        testElevator.start();

        for (WorkAssignment workAssignment : elevatorSharedObject.getWorkAssignments().get(1)) {
            testElevator.addTrackedRequest(workAssignment);
            testElevator.setFloorStopQueue(new ArrayList<>(List.of(1,2)));
            testElevator.setUpFloorStopQueue(new ArrayList<>(List.of(1,2)));
        }
        Assertions.assertNotEquals(testElevator.getElevatorInfo().getStateSignal(), Signal.IDLE);
        while (true) {
            if (testElevator.getElevatorInfo().getStateSignal() != Signal.IDLE) break;
        }
        testElevator.setStopBit(true);
        testElevator.join();
    }

    @Test
    void sendMessage() throws RemoteException {
        WorkAssignment workAssignment = elevatorSharedObject.getWorkAssignments().get(1).peek();
        assert workAssignment != null;
        workAssignment.setSignal(Signal.DONE);
        ConcurrentLinkedDeque<WorkAssignment> workAssignmentQueueUpdated = new ConcurrentLinkedDeque<>();
        workAssignmentQueueUpdated.add(workAssignment);
        elevatorSharedObject.setWorkAssignmentQueue(1, workAssignmentQueueUpdated);

        Assertions.assertEquals(Objects.requireNonNull(schedulerSharedObject.getWorkAssignments().get(1).peek()).getSignal(), Signal.DONE);
    }


}