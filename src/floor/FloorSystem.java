package floor;

import util.ElevatorLogger;
import util.MessageBuffer;
import util.Messages.MessageTypes;
import util.Messages.SerializableMessage;
import util.Messages.Signal;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.*;
import java.util.*;

/**
 * Represents the Floor
 * @author Mayukh Gautam 101181018
 */
public class FloorSystem {

    private final FloorInfoReader currentFloorInfoReader;
    private final MessageBuffer commBuffer;
    private final HashMap<String, SerializableMessage> requestsBuffer;
    public static int PORT = 8082;
    private final Thread senderThread;
    private static final ElevatorLogger logger = new ElevatorLogger("FloorSystem");

    /**
     * Creates the floor.FloorSystem
     * @param commBuffer Outbound buffer from the perspective of the scheduler. floor.FloorSystem will read messages from here.
     */
    public FloorSystem(MessageBuffer commBuffer) throws UnknownHostException {
        this.commBuffer = commBuffer;
        requestsBuffer = new HashMap<>();
        senderThread = new Thread(this::prepareAndSendMessage);
        //If there is input for the file throw an exception and stop
        try {
            currentFloorInfoReader = new FloorInfoReader(new File("./FloorData.txt"));
        } catch (FileNotFoundException fileNotFoundException){
            throw new RuntimeException(fileNotFoundException);
        }
        createMessages();
    }

    /**
     * Creates messages from an input file.
     */
    private void createMessages() throws UnknownHostException {
        //Gets the iterator for the arraylist of FloorInfoData
        Iterator<FloorInfoReader.Data> iterator = currentFloorInfoReader.getRequestQueue();
        while (iterator.hasNext()) {

            // Recreating floor data
            FloorInfoReader.Data floorData = iterator.next();
            FloorInfoReader.Data data = new FloorInfoReader.Data(
                    floorData.time(),
                    floorData.serviceFloor(),
                    floorData.direction(),
                    floorData.requestFloor(),
                    floorData.error()
            );

            //Create a request object with the above info
            String msgID = UUID.randomUUID().toString();
            SerializableMessage request = new SerializableMessage(
                    InetAddress.getLocalHost().toString(),
                    PORT,
                    Signal.WORK_REQ,
                    MessageTypes.FLOOR,
                    Integer.parseInt(floorData.serviceFloor()),
                    msgID,
                    msgID,
                    data
            );

            requestsBuffer.putIfAbsent(request.reqID(), request);
        }
    }

    /**
     * Packages messages and sends them to the scheduler
     */
    private void prepareAndSendMessage(){
        if (!requestsBuffer.isEmpty()){
            SerializableMessage[] sorted = requestsBuffer.values().stream()
                    .sorted(Comparator.comparingInt((serMessage) -> Integer.parseInt(serMessage.data().time())))
                    .toArray(SerializableMessage[]::new);

            int cumulativeDelay = 0;
            for (SerializableMessage message : sorted) {
                int delaySec = (Integer.parseInt(message.data().time()) * 1000) - cumulativeDelay; // Delays the correct amount of time
                cumulativeDelay += delaySec;
                try {
                    Thread.sleep(delaySec);
                } catch (InterruptedException ignored) {}
                sendMessage(new SerializableMessage[]{message});
                logger.info("SENT MESSAGE: " + message);
            }
//            SerializableMessage[] requests = requestsBuffer.values().toArray(new SerializableMessage[0]);
//            sendMessage(requestsBuffer.values().toArray(requests));
        } else {
            throw new RuntimeException("Request buffer was empty, did not send messages");
        }
    }

    public void startFloorInteractions(){
        commBuffer.listenAndFillBuffer();
        senderThread.start();
        receiveMessage();
        try {
            senderThread.join(1000);
        } catch (InterruptedException ignored) {}
        if (senderThread.isAlive()){
            throw new RuntimeException("senderThread should not still be alive!");
        }
    }

    /**
     * Retrieves messages from the scheduler's perspective outbound buffer and interprets them.
     * Close requests once they are fulfilled.
     */
    public void receiveMessage() {
        while (!requestsBuffer.isEmpty()) {
            //Grab all the messages
            SerializableMessage[] receivedMessages = new SerializableMessage[0];
            try {
                receivedMessages = commBuffer.get();
            } catch (InterruptedException ignored) {}
            //Look through each message
            for (SerializableMessage elevatorMessages : receivedMessages) {
                String originalRequestID = elevatorMessages.reqID();
                Signal signal = elevatorMessages.signal();
                //If the response is a DONE type
                if (signal == Signal.DONE){
                    //Removes the request via its id
                    logger.info("Checking if completed by Elevator: " + elevatorMessages.senderID() + " On reqID: " + elevatorMessages.reqID());
                    if (requestsBuffer.remove(originalRequestID) == null) {
                        throw new IllegalArgumentException("ID not found in internal request buffer was found in a DONE message. ID: " + originalRequestID);
                    } else {
                        logger.info("Confirmed completed removing from internal list");
                    }
                    logger.info("Remaining requests to be fulfilled: " + requestsBuffer.size() + "\n");
                }
                //A floor.FloorSystem doesn't need to do anything with other message types
                else if (signal == Signal.WORKING) {/*Nothing*/}
                else if (signal == Signal.IDLE) {/*Nothing*/}
            }
        }
    }

    /**
     * Places messages into the scheduler's perspective inbound buffer.
     * @param message Array of messages to be sent
     * @return Data in string form from all sent messages.
     */
    public String[] sendMessage(SerializableMessage[] message) {
        // Send to scheduler
        commBuffer.put(new ArrayList<>(List.of(message)));
        return Arrays.stream(message).map(msg -> msg.data().toString()).toArray(String[]::new);
    }

    public static void main(String[] args) throws SocketException, UnknownHostException {
        MessageBuffer schedulerBuffer = new MessageBuffer(
                "FloorSystem ->",
                new DatagramSocket(8082),
                new InetSocketAddress(InetAddress.getLocalHost(), 8080),
                8080
        );

        FloorSystem f = new FloorSystem(schedulerBuffer);
        f.startFloorInteractions();
    }

}
