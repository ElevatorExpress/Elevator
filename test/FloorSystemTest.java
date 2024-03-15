import floor.FloorSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.MessageBuffer;
import util.Messages.*;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;

class FloorSystemTest {

    MessageBuffer COMM_BUFF;
    FloorSystem floorSystem;

    @BeforeEach
    void createSystem() throws SocketException {
        COMM_BUFF = new MessageBuffer(
                1, // Obsolete
                "FloorSystem ->",
                new DatagramSocket(8082),
                new InetSocketAddress("localhost", 8080),
                8080
        );
        floorSystem = new FloorSystem(COMM_BUFF);
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
            SerializableMessage[] messages = COMM_BUFF.get();
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