package scheduler;

import elevator.ElevatorControlSystem;
import elevator.ElevatorSubsystem;
import floor.FloorInfoReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scheduler.strategies.AllocationStrategy;
import scheduler.strategies.LoadBalancedStrategy;
import util.Direction;
import util.MessageBuffer;
import util.Messages.MessageTypes;
import util.Messages.SerializableMessage;
import util.Messages.Signal;
import util.SubSystemSharedState;
import util.WorkAssignment;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchedulerTest {
    private Scheduler scheduler;
    private DatagramSocket outSocketMock;
    private MessageBuffer floorMessageBufferMock;
    private AllocationStrategy allocationStrategyMock;
    private SubSystemSharedState sharedStateMock;

    @BeforeEach
    void setUp() throws Exception {
        outSocketMock = new DatagramSocket();
        SubSystemSharedState sharedState = new SubSystemSharedState();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost(), 8080);
        floorMessageBufferMock = new MessageBuffer("FloorMessageBuffer", outSocketMock, inetSocketAddress, 8080);
        allocationStrategyMock = new LoadBalancedStrategy(sharedState);
        scheduler = new Scheduler(InetAddress.getLocalHost(), 8080, 8082, allocationStrategyMock, sharedState);
        sharedState.setScheduler(scheduler);
        scheduler.startSystem();
    }

    private void setPrivateField(Object targetObject, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = targetObject.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(targetObject, value);
    }

    @Test
    void testHandleECSUpdateWithEmptyBuffer() throws InterruptedException, IOException, NotBoundException {
        ElevatorControlSystem ecs = new ElevatorControlSystem(1);
        ElevatorSubsystem ess = new ElevatorSubsystem(1, ecs);
        ess.start();
        boolean updated = scheduler.handleECSUpdate();
        Assertions.assertFalse(updated);
    }

    @Test
    void testHandleECSUpdateWithMessages() throws InterruptedException, IOException, NotBoundException {
        ArrayList<WorkAssignment> workAssignments = new ArrayList<>();
        ElevatorControlSystem ecs = new ElevatorControlSystem(1);
        ElevatorSubsystem ess = new ElevatorSubsystem(1, ecs);
        ess.start();
        FloorInfoReader.Data d =  new FloorInfoReader.Data("4:12", "2", "up", "9", "0");
        FloorInfoReader.Data d2 =  new FloorInfoReader.Data("4:13", "3", "up", "10", "0");
        InetAddress addr = InetAddress.getLocalHost();
        int port = 8080;
        SerializableMessage m1 = new SerializableMessage(InetAddress.getLocalHost().toString(), 8080, Signal.WORK_REQ, MessageTypes.FLOOR, 2, UUID.randomUUID().toString(),null, d);
        SerializableMessage m2 = new SerializableMessage(InetAddress.getLocalHost().toString(), 8080, Signal.WORK_REQ, MessageTypes.FLOOR, 2, UUID.randomUUID().toString(),null, d2);

        scheduler.getMessageBuffer().getMessageBuffer().put(m1);
        scheduler.getMessageBuffer().getMessageBuffer().put(m2);

        boolean updated = scheduler.handleECSUpdate();

        assertTrue(updated);
        assertEquals(2, scheduler.getSharedState().getWorkAssignments().get(1).size());
        assertEquals(1, scheduler.getSharedState().getElevatorStates().size());



    }

    @Test
    void testHandleECSEmergency() throws IOException, NotBoundException, InterruptedException {
        ArrayList<WorkAssignment> workAssignments = new ArrayList<>();
        ElevatorControlSystem ecs = new ElevatorControlSystem(1);
        ElevatorSubsystem ess = new ElevatorSubsystem(1, ecs);
        ess.start();
        WorkAssignment assignment1 = new WorkAssignment(1,3,"7:15",Direction.UP,UUID.randomUUID().toString(), InetAddress.getLocalHost().toString(),8080, Signal.WORKING, 0);
        WorkAssignment assignment2 = new WorkAssignment(4,10,"7:25",Direction.UP,UUID.randomUUID().toString(), InetAddress.getLocalHost().toString(),8080, Signal.WORKING, 0);
        workAssignments.add(assignment1);
        workAssignments.add(assignment2);
        boolean result = scheduler.handleECSEmergency(workAssignments);
        System.out.println(scheduler.getSharedState().getWorkAssignments());
        Assertions.assertFalse(result);
    }
}
