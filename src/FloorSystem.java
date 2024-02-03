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

    public FloorSystem(MessageBuffer outbound, MessageBuffer inbound) {
        inboundMessageBuffer = inbound;
        outboundMessageBuffer = outbound;
        requestsBuffer = new HashMap<>();
        currentFloorInfoReader = new FloorInfoReader(new File("./Floor data.txt"));
        createMessages();
    }

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
                    MESSAGE_FACTORY.createFloorMessage(floorData.requestFloor(), floorDataMap, FloorSignal.WORK_REQ);
            requestsBuffer.putIfAbsent(request.id(), request);
        }
    }

    private void prepareAndSendMessage(){
        if (!requestsBuffer.isEmpty()){
            // Ignore unchecked warning, types will always be correct.
            sendMessage(requestsBuffer.values().toArray(FloorMessage[]::new));
        }
    }

    public void run(){
        prepareAndSendMessage();
        receiveMessage();
    }

    @Override
    public void receiveMessage() {
        while (!requestsBuffer.isEmpty()) {
            MessageInterface<?, ?>[] receivedMessages = outboundMessageBuffer.get();
            if (receivedMessages instanceof ElevatorMessage<FloorMessage<String>>[] receivedElevatorMessages){
                for (ElevatorMessage<FloorMessage<String>> elevatorMessages : receivedElevatorMessages) {
                    String originalRequestID = elevatorMessages.data().get("Servicing").id();
                    ElevatorSignal signal = elevatorMessages.getSignal();
                    if (signal == ElevatorSignal.DONE){
                        requestsBuffer.remove(originalRequestID);
                    }
                    else if (signal == ElevatorSignal.WORKING) {/*Nothing Yet*/}
                    else if (signal == ElevatorSignal.IDLE) {/*Nothing Yet*/}
                }
            }
        }
    }

    @Override
    public String[] sendMessage(FloorMessage<String>[] message) {
        // Send to scheduler
        inboundMessageBuffer.put(message);
        return Arrays.stream(message).map(msg -> msg.getData().toString()).toArray(String[]::new);
    }

}
