package floor;

import util.Messages.FloorMessage;
import util.Messages.FloorMessageFactory;
import util.Messages.MessageInterface;
import util.Messages.Signal;
import util.ElevatorLogger;
import util.MessageBuffer;
import util.SubSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Represents the Floor
 * @author Mayukh Gautam 101181018
 */
public class FloorSystem implements SubSystem<FloorMessage<String>> {

    private static final FloorMessageFactory<String> MESSAGE_FACTORY = new FloorMessageFactory<>();
    private final FloorInfoReader currentFloorInfoReader;
    private final MessageBuffer inboundMessageBuffer;
    private final MessageBuffer outboundMessageBuffer;
    private final HashMap<String, FloorMessage<String>> requestsBuffer;
    private static final ElevatorLogger logger = new ElevatorLogger("floor.FloorSystem");

    /**
     * Creates the floor.FloorSystem
     * @param outbound Outbound buffer from the perspective of the scheduler. floor.FloorSystem will read messages from here.
     * @param inbound Inbound buffer from the perspective of the scheduler. floor.FloorSystem will send messages from here.
     */
    public FloorSystem(MessageBuffer outbound, MessageBuffer inbound) {
        inboundMessageBuffer = inbound;
        outboundMessageBuffer = outbound;
        requestsBuffer = new HashMap<>();
        //If there is input for the file throw an exception and stop
        try {
            currentFloorInfoReader = new FloorInfoReader(new File("./FloorData.txt"));
        } catch (FileNotFoundException fileNotFoundException){
            throw new RuntimeException(fileNotFoundException);
        }
        createMessages();
    }

    public FloorSystem() {
        this(new MessageBuffer(10, "DummyOut"), new MessageBuffer(10, "DummyIn"));
    }

    /**
     * Creates messages from an input file.
     */
    private void createMessages(){
        //Gets the iterator for the arraylist of FloorInfoData
        Iterator<FloorInfoReader.Data> iterator = currentFloorInfoReader.getRequestQueue();
        while (iterator.hasNext()) {
            //Filling the floorDataMap with the appropriate values
            FloorInfoReader.Data floorData = iterator.next();
            HashMap<String, String> floorDataMap = new HashMap<>();
            floorDataMap.putIfAbsent("Time", floorData.time());
            floorDataMap.putIfAbsent("ServiceFloor", floorData.serviceFloor());
            floorDataMap.putIfAbsent("RequestDirection", floorData.direction());
            floorDataMap.putIfAbsent("Floor", floorData.requestFloor());
            //Create a request object with the above info
            FloorMessage<String> request =
                    MESSAGE_FACTORY.createFloorMessage(floorData.requestFloor(), floorDataMap, Signal.WORK_REQ);
            requestsBuffer.putIfAbsent(request.id(), request);
        }
    }

    /**
     * Packages messages and sends them to the scheduler
     */
    private void prepareAndSendMessage(){
        if (!requestsBuffer.isEmpty()){
            // Ignore unchecked warning, types will always be correct.
            FloorMessage<String>[] requests = requestsBuffer.values().toArray(new FloorMessage[0]);
            sendMessage(requestsBuffer.values().toArray(requests));
        }
    }

    public void startFloorInteractions(){
        prepareAndSendMessage();
        receiveMessage();
    }

    /**
     * Retrieves messages from the scheduler's perspective outbound buffer and interprets them.
     * Close requests once they are fulfilled.
     */
    @Override
    public void receiveMessage() {
        while (!requestsBuffer.isEmpty()) {
            //Grab all the messages
            MessageInterface<FloorMessage<String>>[] receivedMessages = outboundMessageBuffer.get();
            //Look through each message
            for (MessageInterface<FloorMessage<String>> elevatorMessages : receivedMessages) {
                String originalRequestID = elevatorMessages.getData().get("Servicing").id();
                Signal signal = elevatorMessages.getSignal();
                //If the response is a DONE type
                if (signal == Signal.DONE){
                    //Removes the request via its id
                    logger.info("Checking if completed: " + elevatorMessages);
                    requestsBuffer.remove(originalRequestID);
                    logger.info("Confirmed completed removing from internal list");
                }
                //A floor.FloorSystem doesn't need to do anything with other message types
                else if (signal == Signal.WORKING) {/*Nothing Yet*/}
                else if (signal == Signal.IDLE) {/*Nothing Yet*/}
            }
        }
    }

    /**
     * Places messages into the scheduler's perspective inbound buffer.
     * @param message Array of messages to be sent
     * @return Data in string form from all sent messages.
     */
    @Override
    public String[] sendMessage(FloorMessage<String>[] message) {
        // Send to scheduler
        inboundMessageBuffer.put(message);
        return Arrays.stream(message).map(msg -> msg.getData().toString()).toArray(String[]::new);
    }

    public static void main(String[] args) {
        FloorSystem f = new FloorSystem();
        f.startFloorInteractions();
    }

}
