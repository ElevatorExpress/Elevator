//
//import Messages.ElevatorSignal;
//import org.junit.Test;
//import static org.junit.Assert.*;
//import Messages.MessageInterface;
//import Messages.ElevatorMessage;
//import org.mockito.Mock;
//
//import java.util.HashMap;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//import static org.mockito.Mockito.after;
//import static org.mockito.Mockito.*;
//
//public class SchedulerMessageBufferIntegrationTest {
//    @Mock
//    private SubSystem<MessageInterface> mockFloor;
//
//    @Mock
//    private SubSystem<MessageInterface> mockElevator;
//
//    private Scheduler scheduler;
//    private MessageBuffer messageBuffer;
//    @Test
//    public void testSchedulerReadsMessageBufferCorrectly() throws InterruptedException {
//        // Initialize the message buffer with a capacity
//        MessageBuffer messageBuffer = new MessageBuffer(10);
//
//        // Mock or create real instances of MessageInterface messages
//        HashMap<String, String> data = new HashMap<>();
//        data.put("floor", "1");
//        HashMap<String,String> data2 = (HashMap<String, String>) data.clone();
//        MessageInterface<ElevatorSignal> m = new ElevatorMessage<>("Elevator", "1",data, ElevatorSignal.IDLE);
//        MessageInterface<ElevatorSignal> f1 = new ElevatorMessage<>("Floor", "1", data2, ElevatorSignal.IDLE);
//        MessageInterface[] e_messagesToAdd = {m};
//        MessageInterface[] messagesToAdd = {f1};
//
//
//        when(mockElevator.receiveMessage(e_messagesToAdd)).thenReturn(m.getId()).thenReturn(f1.getId());
//
//
////        // Create the Scheduler with mock or real subscribers if necessary
////        HashMap<String, SubSystem<MessageInterface>> floorSubscribers = new HashMap<>();
////        HashMap<String, SubSystem<MessageInterface>> elevatorSubscribers = new HashMap<>();
////        Scheduler scheduler = new Scheduler(messageBuffer, );
//        //Mock Floor and Elevator
//        Floor mockFloor = mock(Floor.class);
//
//        // Use a separate thread to simulate adding messages to the buffer
//        Thread producerThread = new Thread(() -> {
//            try {
//                messageBuffer.put(messagesToAdd);
//            } catch (Exception e) {
//                fail("Exception when adding messages to the buffer: " + e.getMessage());
//            }
//        });
//
//        // Start the producer thread
//        producerThread.start();
//
//        // Wait a bit to ensure the message is added (in a real scenario, use proper synchronization)
//        Thread.sleep(100); // This is a simplistic approach for demonstration purposes only
//
//        // Act: Trigger the scheduler to read from the buffer
//        scheduler.readBuffer(); // This might need to be run in a loop or in a separate thread based on implementation
//
//        // Assert: Verify the messages were processed correctly
//        assertFalse("Buffer should be empty after processing", messageBuffer.isBufferEmpty());
////        assertTrue("Elevator request buffer should contain the message", scheduler.elevatorRequestBuffer.containsKey(elevatorRequest.getMessageId()));
//
//        // Cleanup: Ensure threads are properly managed
//        producerThread.join(); // Ensure the producer thread has finished execution
//    }
//}
