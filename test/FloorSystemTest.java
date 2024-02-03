import Messages.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

class FloorSystemTest {

    MessageBuffer SCH_INBOUND;
    MessageBuffer SCH_OUTBOUND;
    static ElevatorMessageFactory<FloorMessage<String>> ELEV_FACTORY = new ElevatorMessageFactory<>();

    FloorSystem floorSystem;

    @BeforeEach
    void createSystem(){
        SCH_INBOUND = new MessageBuffer(10);
        SCH_OUTBOUND = new MessageBuffer(10);
        floorSystem = new FloorSystem(SCH_OUTBOUND, SCH_INBOUND);
    }

    @Test
    void startFloorInteractions() {
        /*
         Tests if messages are:
            * Being placed into queues properly
            * Being removed properly
            * Being Interpreted properly
         */
        Thread consumerProducerThread = new Thread(() -> {
            MessageInterface<?>[] messages = SCH_INBOUND.get();
            Assertions.assertTrue(messages.length != 0, "No Messages Were Found");

            MessageInterface<?>[] elevatorMessages =  new MessageInterface<?>[messages.length];

            for (int i = 0; i < messages.length; i++){
                HashMap<String, FloorMessage<String>> tmpMap = new HashMap<>();
                tmpMap.put("Servicing", (FloorMessage<String>) messages[i]);
                elevatorMessages[i] = ELEV_FACTORY.createElevatorMessage("1", tmpMap, Signal.DONE);
            }

            SCH_OUTBOUND.put(elevatorMessages);
        });

        consumerProducerThread.start();
        floorSystem.startFloorInteractions();
    }


}