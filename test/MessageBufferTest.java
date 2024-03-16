//import util.Messages.ElevatorMessage;
//import util.Messages.MessageInterface;
//import util.Messages.MessageTypes;
//import util.Messages.Signal;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import util.MessageBuffer;
//
//import java.util.Collections;
//
///**
// * Testing util.MessageBuffer
// * @author Joshua Braddon
// */
//class MessageBufferTest {
//
//    MessageBuffer emptyBuffer;
//    ElevatorMessage testMessage = new ElevatorMessage<>(MessageTypes.ELEVATOR, "1", Collections.singletonMap("2", "3"), Signal.WORK_REQ, "2");
//    ElevatorMessage[] testMessageList = {testMessage, testMessage};
//
//    @BeforeEach
//    void setUp() {
//        emptyBuffer = new MessageBuffer(2, "");
//    }
//
//    @Test
//    void testEmptyBuffer() {
//        //Fill the buffer with the messages
//        emptyBuffer.put(testMessageList);
//        MessageInterface[] messages = emptyBuffer.get();
//        //Makes sure the buffer is the correct size after the insert
//        Assertions.assertEquals(2, messages.length, "Put did not copy the into the buffer");
//        //Makes sure the buffer is correctly marked as empty after the insert
//        Assertions.assertTrue(emptyBuffer.isBufferEmpty(), "Buffer did not empty after get()");
//    }
//}