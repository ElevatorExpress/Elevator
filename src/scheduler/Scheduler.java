package scheduler;

import util.ElevatorLogger;
import util.MessageHelper;
import util.Messages.MessageTypes;
import util.Messages.SerializableMessage;
import util.states.SchedulerScheduling;
import util.states.SchedulerState;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


/**
 * The scheduler.Scheduler class is responsible for managing elevator and floor requests,
 * assigning idle elevators to floor requests, and handling elevator status updates.
 * It implements the Runnable interface to allow it to be executed in a separate thread.
 * At hte moment it is at risk of circular wait, and needs to be refactored to use semaphores.
 */
public class Scheduler {
    private DatagramSocket inSocket;
    private DatagramSocket outSocket;
    private SchedulerState currentState;

    // Key is Message ID, Value is the message
    //Requests are taken from the shared buffer and parsed into the appropriate buffer, these are requests that have yet
    //to be serviced
    final ConcurrentHashMap<String, SerializableMessage> elevatorRequestBuffer = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SerializableMessage> floorRequestBuffer = new ConcurrentHashMap<>();

    //Key is Elevator/floor ID, Value is the message, store for elevators ready for work
    private final HashMap<String, SerializableMessage> idleElevators = new HashMap<>();

    //Key is Elevator/floor ID, Value is the message, store for elevators currently working
    private final HashMap<String, SerializableMessage> workingElevators = new HashMap<>();

    //Key is Message ID, Value is the message, store for floor requests that are pending
    private final HashMap<String, SerializableMessage> pendingFloorRequests = new HashMap<>();

    //Key is Message ID, Value is the message, store for elevator requests that are pending
    private HashMap<String, SerializableMessage> pendingElevatorRequests = new HashMap<>();
    private static final ElevatorLogger logger = new ElevatorLogger("Scheduler");

    private int elevatorSubSystemPort;
    private int floorSubSystemPort;
    volatile boolean testStopBit;


    /**
     *  Creates a Scheduler
     * @param schedulerAddr The Internet address of the scheduler
     * @param schedulerPort The port that the scheduler listens to
     * @param elevatorSubSystemPort The port that the elevator system listens to
     * @param floorSubSystemPort The port that the floor system listens to
     */
        public Scheduler(InetAddress schedulerAddr, int schedulerPort, int elevatorSubSystemPort, int floorSubSystemPort) {
            try {
                this.floorSubSystemPort = floorSubSystemPort;
                this.elevatorSubSystemPort = elevatorSubSystemPort;
                inSocket = new DatagramSocket(schedulerPort, schedulerAddr);
                outSocket = new DatagramSocket();
                testStopBit = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            currentState = null; // Started with startSystem()
        }





    /**
     *
     * @return A HashMap containing the idle elevators.
     */

    public HashMap<String, SerializableMessage> getIdleElevators() {
        return idleElevators;
    }

    /**
     *
     * @return A HashMap containing the working elevators.
     */
    public HashMap<String, SerializableMessage> getWorkingElevators() {
        return workingElevators;
    }

    /**
     *
     * @return A HashMap containing the pending floor requests.
     */
    public HashMap<String, SerializableMessage> getPendingFloorRequests() {
        return pendingFloorRequests;
    }

    /**
     *
     * @return A HashMap containing the pending elevator requests.
     */
    public ConcurrentHashMap<String, SerializableMessage> getFloorRequestBuffer() {
        return floorRequestBuffer;
    }


    /**
     * Retrieves messages from the elevatorRequestBuffer, this is called after the messages have been removed from the
     * shared buffer. Only scheduler.Scheduler has access to this buffer.
     * Close requests once they are fulfilled. it is called after thread woken up from wait
     * and messages are in the buffer.
     * No params, no return, public for testing purposes
     */
    public void serveElevatorReqs() throws IOException {
        String[] keys = elevatorRequestBuffer.keySet().toArray(new String[0]);
        for (String messageId : keys) {
            SerializableMessage message = elevatorRequestBuffer.get(messageId);
            logger.info("Elevator Request. SenderID: " + message.senderID() + " Signal: " + message.signal());
            if (message.type().equals(MessageTypes.ELEVATOR)){
                switch (message.signal()) {
                    case IDLE:
                        idleElevators.put(String.valueOf(message.senderID()), message);
                        logger.info("Elevator " + message.senderID() + " is now idle");
                        break;
                    case WORKING:
                        //USES Sender ID as KEY
                        workingElevators.put(String.valueOf(message.senderID()), message);
                        logger.info("Elevator " + message.senderID() + " is now working");
                        break;
                    case DONE:
                        logger.info("Elevator " + message.senderID() + " is now done");
                        workingElevators.remove(message.senderID());
                        if(message.signal() == null){
                            currentState.handleBadMessage();
                            throw new IllegalArgumentException("Invalid data: " + " for message: " + message);
                        }
                        //values associated with servicing is the original message that was sent to the elevator
                        String completedFloorReqID = message.reqID();
                        elevatorRequestBuffer.remove(messageId);
                        pendingFloorRequests.remove(completedFloorReqID);
                        InetAddress floorAddr = InetAddress.getByName(message.senderAddr());
                        MessageHelper.SendMessage(outSocket, message, floorAddr, floorSubSystemPort);
                        idleElevators.put(String.valueOf(message.senderID()), message);
                        break;
                    default:
                        currentState.handleBadMessage();
                        throw new IllegalArgumentException("Invalid signal");
                }
            }
        }
        currentState.handleDoneServing();
    }


    /**
     * Retrieves messages from the shared buffer and parses them into the appropriate buffer.
     * This is called after the thread is woken up from wait and messages are in the buffer.
     * No params, no return, public for testing purposes
     */
    public void readBuffer(){
        ElevatorLogger tLogger = new ElevatorLogger("ReaderThread");
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while (testStopBit) {
            tLogger.info("Waiting for packet");
            try {
                inSocket.receive(packet);
                try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer, 0, packet.getLength()))) {
                    SerializableMessage sm = (SerializableMessage) ois.readObject();
                    tLogger.info("Received id: " + sm.senderID() + " Received addr: " + sm.senderAddr() + " Received port: " + sm.senderPort() + " Received payload: " + sm.data());

                    switch (sm.type()) {
                        case ELEVATOR -> elevatorRequestBuffer.put(sm.messageID(), sm);
                        case FLOOR -> floorRequestBuffer.put(sm.messageID(), sm);
                        default -> throw new IllegalArgumentException("Invalid message type");
                    }

                    currentState.handleDoneReadingRequest();

                    if (currentState instanceof SchedulerScheduling state){
                        SchedulerScheduling.SubState subState = state.getSubState();
                        while (subState != SchedulerScheduling.SubState.READ_BUFF) {
                            subState = state.getSubState();
                            if (subState == SchedulerScheduling.SubState.SERVING_ELEVATORS)
                                serveElevatorReqs();
                            else if (subState == SchedulerScheduling.SubState.SERVING_FLOORS)
                                serveFloorRequests();
                        }
                    }

                } catch (Exception e) {
                    System.err.println("Error during deserialization: " + e.getMessage());
                    currentState.handleBadMessage();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }



    /**
     * Assigns idle elevators to floor requests and adds the request to pending floor requests.
     * Pending floor requests are ones that are in the process of being serviced, they
     * will have a counterpart "working elevator"
     * <p>
     * No params, no return, public for testing purposes
     */
    public void serveFloorRequests() throws IOException {
        //go through idle elevators and assign them to floor requests, add the request to pending floor requests
        String[] floorRequestKeys = floorRequestBuffer.keySet().toArray(new String[0]);
        ArrayList<SerializableMessage> elevatorOutMessagePayload = new ArrayList<>();
        for (String floorRequestId : floorRequestKeys) {
            if (!idleElevators.isEmpty() && !floorRequestBuffer.isEmpty()) {

                String idleElevatorId = idleElevators.keySet().iterator().next();
                pendingFloorRequests.put(floorRequestId, floorRequestBuffer.get(floorRequestId));
                SerializableMessage[] floorRequest = {pendingFloorRequests.get(floorRequestId)};

                if (floorRequest[0] == null) {
                    throw new IllegalArgumentException("Invalid floorRequest: " + null + " for floorRequestId: " + floorRequestId);
                }

                elevatorOutMessagePayload.addAll(Arrays.stream(floorRequest).toList());
                floorRequestBuffer.remove(floorRequestId);
                idleElevators.remove(idleElevatorId);
            }
        }
        if (!elevatorOutMessagePayload.isEmpty()){
            logger.info("Sending A Message to Elevators");
            MessageHelper.SendMessages(outSocket, elevatorOutMessagePayload, InetAddress.getLocalHost(), elevatorSubSystemPort);
        }
        currentState.handleDoneServing();
    }

    /**
     * Starts the system
     */
    public void startSystem() {
        currentState = SchedulerState.start(this);
        readBuffer();
        // Normal function of this class should not  reach this point. This is for testing only
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        inSocket.close();
        outSocket.close();
    }

    public static void main(String[] args) throws IOException {
        Scheduler s = new Scheduler(InetAddress.getLocalHost(),8080, 8081,8082);
        s.startSystem();
    }
}
