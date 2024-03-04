import util.Messages.FloorMessageFactory;
import util.Messages.MessageInterface;
import util.Messages.Signal;
import elevator.ElevatorSubsystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.MessageBuffer;

import java.util.HashMap;

class ElevatorSubsystemTest {

    MessageBuffer sharedBuffer = new MessageBuffer(1, "");
    ElevatorSubsystem elevator;
    MessageInterface<String>[] request = new MessageInterface[1];

    @BeforeEach
    void createElevator(){
        elevator = new ElevatorSubsystem(sharedBuffer, sharedBuffer);

        HashMap<String, String> workData = new HashMap<>();
        workData.put("Time", "14:05:15.0");
        workData.put("ServiceFloor", "2");
        workData.put("RequestDirection", "up");
        workData.put("Floor", "4");
        request[0] = new FloorMessageFactory<String>().createFloorMessage("FloorID-1", workData, Signal.WORK_REQ);
    }

    @Test
    void sendMessage() {
        elevator.sendMessage(request);
        Assertions.assertEquals(1, sharedBuffer.getBufferLength());
    }

    @Test
    void receiveMessage() {
        elevator.sendMessage(request);
        elevator.receiveMessage();
        Assertions.assertTrue(sharedBuffer.isBufferEmpty());
    }




}