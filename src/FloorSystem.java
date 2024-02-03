import Messages.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Represents the Floor
 * @author Mayukh Gautam 101181018
 */
public class FloorSystem implements Runnable, SubSystem<FloorMessage<String>> {

    private static final FloorMessageFactory<String> MESSAGE_FACTORY = new FloorMessageFactory<>();
    private final FloorInfoReader currentFloorInfoReader;
    private final MessageBuffer inboundMessageBuffer;
    private final MessageBuffer outboundMessageBuffer;
    private final HashMap<String, FloorMessage<String>> requestsBuffer;

    /**
     * Creates the FloorSystem
     * @param outbound Outbound buffer from the perspective of the scheduler. FloorSystem will read messages from here.
     * @param inbound Inbound buffer from the perspective of the scheduler. FloorSystem will send messages from here.
     */
    public FloorSystem(MessageBuffer outbound, MessageBuffer inbound) {
        inboundMessageBuffer = inbound;
        outboundMessageBuffer = outbound;
        requestsBuffer = new HashMap<>();
        currentFloorInfoReader = new FloorInfoReader(new File("./Floor data.txt"));
        createMessages();
    }

    /**
     * Creates messages from an input file.
     */
    private void createMessages(){
        Iterator<FloorInfoReader.Data> iterator = currentFloorInfoReader.getRequestQueue();
        while (iterator.hasNext()) {
            FloorInfoReader.Data floorData = iterator.next();
            HashMap<String, String> floorDataMap = new HashMap<>();
            floorDataMap.putIfAbsent("Time", floorData.time());
            floorDataMap.putIfAbsent("ServiceFloor", floorData.serviceFloor());
            floorDataMap.putIfAbsent("RequestDirection", floorData.direction());
            floorDataMap.putIfAbsent("Floor", floorData.requestFloor());
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
            sendMessage(requestsBuffer.values().toArray(FloorMessage[]::new));
        }
    }

    /**
     * Runs the thread
     */
    public void run(){
        // It-1 only one cycle
        prepareAndSendMessage();
    }

    /**
     * Retrieves messages from the scheduler's perspective outbound buffer and interprets them.
     * Close requests once they are fulfilled.
     */
    @Override
    public void receiveMessage() {
        // Do nothing but store for It-1. This is the end of the message chain.
        while (!requestsBuffer.isEmpty()) {
            MessageInterface<?>[] receivedMessages = outboundMessageBuffer.get();
            if (receivedMessages instanceof ElevatorMessage<FloorMessage<String>>[] receivedElevatorMessages){
                for (ElevatorMessage<FloorMessage<String>> elevatorMessages : receivedElevatorMessages) {
                    String originalRequestID = elevatorMessages.data().get("Servicing").id();
                    Signal signal = elevatorMessages.getSignal();
                    if (signal == Signal.DONE){
                        requestsBuffer.remove(originalRequestID);
                    }
                    else if (signal == Signal.WORKING) {/*Nothing Yet*/}
                    else if (signal == Signal.IDLE) {/*Nothing Yet*/}
                }
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

}
