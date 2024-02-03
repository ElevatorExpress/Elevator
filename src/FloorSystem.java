import Messages.ElevatorMessage;
import Messages.ElevatorSignal;
import Messages.MessageInterface;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Represents the Floor
 * @author Mayukh Gautam 101181018
 */
public class FloorSystem implements Runnable, SubSystem<MessageInterface<String, ElevatorSignal>> {
    private final FloorInfoReader currentFloorInfoReader;
    private ElevatorMessage<String> floorInfoMessage;
    /** @noinspection MismatchedQueryAndUpdateOfCollection*/
    private final MessageBuffer inboundMessageBuffer;
    private final MessageBuffer outboundMessageBuffer;

    public FloorSystem(MessageBuffer outbound, MessageBuffer inbound) {

        /*
        new FloorMessage:UUID-1, FID -> Sch -> FloorMessage:UUID-1, FID -> Elevator -> new Elevator:UUID-2 -> Sch -> new FloorMessage:UUID-4, FID
         */

        inboundMessageBuffer = inbound;
        outboundMessageBuffer = outbound;
        floorInfoMessage = null; // In case of error during real init
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
            floorInfoMessage = new ElevatorMessage<>(null, null, floorDataMap, ElevatorSignal.DONE/* TEMP. IT SHOULD BE: FloorSignal.WORK_REQ*/);
        }
    }

    private void prepareAndSendMessage(){
        //sendMessage();
    }

    public void run(){
        // It-1 only one cycle
        prepareAndSendMessage();
    }

    @Override
    public void receiveMessage(MessageInterface<String, ElevatorSignal>[] message) {
        // Do nothing but store for It-1. This is the end of the message chain.
        while (true) {
            outboundMessageBuffer.get();
        }
    }

    @Override
    public String[] sendMessage(MessageInterface<String, ElevatorSignal>[] message) {
        // Send to scheduler
        inboundMessageBuffer.put(message);
        return Arrays.stream(message).map(msg -> msg.getData().toString()).toArray(String[]::new);
    }

}
