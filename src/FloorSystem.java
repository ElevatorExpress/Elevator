import Messages.ElevatorMessage;
import Messages.ElevatorSignal;
import Messages.MessageInterface;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Represents the Floor
 * @author Mayukh Gautam 101181018
 */
public class FloorSystem implements Runnable, SubSystem<MessageInterface<String, ElevatorSignal>> {
    private final FloorInfoReader currentFloorInfoReader;
    private ElevatorMessage<String> floorInfoMessage;
    /** @noinspection MismatchedQueryAndUpdateOfCollection*/
    private final SubSystem<ElevatorMessage<?>> scheduler;
    private final MessageBuffer inboundMessageBuffer;
    private final MessageBuffer outboundMessageBuffer;

    public FloorSystem(SubSystem<ElevatorMessage<?>> mainScheduler) {
        scheduler = mainScheduler;
        inboundMessageBuffer = new MessageBuffer(100);
        outboundMessageBuffer = new MessageBuffer(100);
        floorInfoMessage = null; // In case of error during real init
        currentFloorInfoReader = new FloorInfoReader(new File("./elevator_requests.txt"));
        createMessages();
    }

    private void createMessages(){
        // Loop for each entry in file.
        HashMap<String, String> floorDataMap = new HashMap<>();
        floorDataMap.putIfAbsent("Time", currentFloorInfoReader.getTime());
        floorDataMap.putIfAbsent("RequestFloor", currentFloorInfoReader.getRequestFloor());
        floorDataMap.putIfAbsent("RequestDirection", currentFloorInfoReader.getDirection());
        floorDataMap.putIfAbsent("Floor", currentFloorInfoReader.getServiceFloor());
        floorInfoMessage = new ElevatorMessage<>(null, null, floorDataMap, ElevatorSignal.DONE/* TEMP. IT SHOULD BE: FloorSignal.WORK_REQ*/);
    }

    private void prepareAndSendMessage(){
        outboundMessageBuffer.put(new MessageInterface[]{floorInfoMessage});

        sendMessage(outboundMessageBuffer.get());
    }

    public void run(){
        // It-1 only one cycle
        prepareAndSendMessage();
    }

    @Override
    public void receiveMessage(MessageInterface<String, ElevatorSignal>[] message) {
        // Do nothing but store for It-1. This is the end of the message chain.
        inboundMessageBuffer.put(message);
    }

    @Override
    public String[] sendMessage(MessageInterface<String, ElevatorSignal>[] message) {
        // Send to scheduler
        scheduler.receiveMessage((ElevatorMessage<?>[]) message);
        return Arrays.stream(message).map(msg -> msg.getData().toString()).toArray(String[]::new);
    }

}
