package scheduler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scheduler.strategies.LoadBalancedStrategy;
import util.Direction;
import util.ElevatorStateUpdate;
import util.Messages.Signal;
import util.SubSystemSharedState;
import util.WorkAssignment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

public class LoadBalancedStrategyTest {
    static SubSystemSharedState sharedState;
    static LoadBalancedStrategy lbs;

    @BeforeAll
    static void setUp() {
        try {
            HashMap<Integer, ElevatorStateUpdate> testStates = new HashMap<>();
            testStates.put(1, new ElevatorStateUpdate(1, 1, Direction.ANY, null, false));
            sharedState = new SubSystemSharedState();
            sharedState.setElevatorStates(testStates);
            HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> testWorkAssignments = new HashMap<>();
            ConcurrentLinkedDeque<WorkAssignment> testWorkAssignmentsQueue = new ConcurrentLinkedDeque<>();
            testWorkAssignments.put(1, testWorkAssignmentsQueue);
            sharedState.setWorkAssignments(testWorkAssignments);
            lbs = new LoadBalancedStrategy(sharedState);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void TestAllocate() throws UnknownHostException {
        Assertions.assertEquals(0, sharedState.getWorkAssignments().get(1).size());
        lbs.allocate(new WorkAssignment(
                1,
                2,
                "6",
                Direction.UP,
                UUID.randomUUID().toString(),
                InetAddress.getLocalHost().toString(),
                8082,
                Signal.WORK_REQ,
                0
        ));
        Assertions.assertEquals(1, sharedState.getWorkAssignments().get(1).size());
    }

    @Test
    void TestSmallestAssignment() throws UnknownHostException {
        lbs.allocate(new WorkAssignment(
                1,
                2,
                "6",
                Direction.UP,
                UUID.randomUUID().toString(),
                InetAddress.getLocalHost().toString(),
                8082,
                Signal.WORK_REQ,
                0
        ));
        Assertions.assertTrue(lbs.smallestAssignment(1, sharedState.getWorkAssignments()));
    }
}
