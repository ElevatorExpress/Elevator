package scheduler;

import floor.FloorInfoReader;
import util.MessageHelper;
import util.Messages.ElevatorMessage;
import util.Messages.MessageInterface;
import util.Messages.MessageTypes;
import util.ElevatorLogger;
import util.MessageBuffer;
import util.Messages.SerializableMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


/**
 * The scheduler.Scheduler class is responsible for managing elevator and floor requests,
 * assigning idle elevators to floor requests, and handling elevator status updates.
 * It implements the Runnable interface to allow it to be executed in a separate thread.
 * At hte moment it is at risk of circular wait, and needs to be refactored to use semaphores.
 */
public class Scheduler {
    private DatagramSocket inSocket;
    private DatagramSocket outSocket;



    // Key is Message ID, Value is the message
    //Requests are taken from the shared buffer and parsed into the appropriate buffer, these are requests that have yet
    //to be serviced
    HashMap<String, SerializableMessage> elevatorRequestBuffer = new HashMap<>();
    private HashMap<String, SerializableMessage> floorRequestBuffer = new HashMap<>();

    //Key is Elevator/floor ID, Value is the message, store for elevators ready for work
    private HashMap<String, SerializableMessage> idleElevators = new HashMap<>();

    //Key is Elevator/floor ID, Value is the message, store for elevators currently working
    private HashMap<String, SerializableMessage> workingElevators = new HashMap<>();

    //Key is Message ID, Value is the message, store for floor requests that are pending
    private HashMap<String, SerializableMessage> pendingFloorRequests = new HashMap<>();

    //Key is Message ID, Value is the message, store for elevator requests that are pending
    private HashMap<String, SerializableMessage> pendingElevatorRequests = new HashMap<>();
    private static final ElevatorLogger logger = new ElevatorLogger("scheduler.Scheduler");

    private int schedulerPort;
    private InetAddress schedulerAddr;
    private int elevatorSubSystemPort;
    private int floorSubSystemPort;



    //ToDo: Replace synchronized with semaphores to avoid circular wait.
    
    /**
     * The scheduler.Scheduler class represents a scheduler in an elevator system.
     * It is responsible for managing message buffers and handling floor requests.
     */
        /**
         * Constructs a new scheduler.Scheduler object with the specified message buffers.
         * 
         * @param floorOutBuffer   Shared message buffer for outgoing messages to floors
         * @param elevatorOutBuffer   Shared message buffer for outgoing messages to elevators
         */
        public Scheduler(InetAddress schedulerAddr, int schedulerPort, int elevatorSubSystemPort, int floorSubSystemPort) {
            try {
                inSocket = new DatagramSocket(schedulerPort, schedulerAddr);
                outSocket = new DatagramSocket();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
    public HashMap<String, SerializableMessage> getFloorRequestBuffer() {
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
            logger.info("Elevator Request: " + message);
            if (message.type().equals(MessageTypes.ELEVATOR)){
                switch (message.signal()) {
                    case IDLE:
                        idleElevators.put(message.senderID(), message);
                        elevatorRequestBuffer.remove(messageId);
                        logger.info("Elevator " + message.senderID() + " is now idle");
                        break;
                    case WORKING:
                        //USES Sender ID as KEY
                        workingElevators.put(message.senderID(), message);
                        logger.info("Elevator " + message.senderID() + " is now working");
                        break;
                    case DONE:
                        logger.info("Elevator " + message.senderID() + " is now done");
                        workingElevators.remove(message.senderID());
                        if(message.data() == null || !message.data().containsKey("Servicing")){
                            throw new IllegalArgumentException("Invalid data: " + message.data() + " for message: " + message);
                        }
                        //values associated with servicing is the origional message that was sent to the elevator
                        SerializableMessage doneFMessage = (SerializableMessage) message.data().get("Servicing");
                        String completedFloorReqID = doneFMessage.senderID();
                        pendingFloorRequests.remove(completedFloorReqID);
//                        floorOutBuffer.put(new MessageInterface[]{message});
                        InetAddress floorAddr = InetAddress.getByName(message.senderAddr());
                        MessageHelper.SendMessage(outSocket, message,  floorAddr,floorSubSystemPort);
                        idleElevators.put(message.senderID(), message);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid signal");
                }
            }
        }
    }


    /**
     * Retrieves messages from the shared buffer and parses them into the appropriate buffer.
     * This is called after the thread is woken up from wait and messages are in the buffer.
     * No params, no return, public for testing purposes
     */
    public int readBuffer(){
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while (true) {
            System.out.println("Waiting for packet");
            try {
                inSocket.receive(packet);
                try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer, 0, packet.getLength()))) {
                    SerializableMessage sm = (SerializableMessage) ois.readObject();
                    System.out.println("Received id: " + sm.senderID());
                    System.out.println("Received addr: " + sm.senderAddr());
                    System.out.println("Received port: " + sm.senderPort());
                    System.out.println("Received payload: " + sm.data());


                    switch (sm.type()) {
                        case ELEVATOR:
                            elevatorRequestBuffer.put(sm.senderID(), sm);
                            break;
                        case FLOOR:
                            floorRequestBuffer.put(sm.senderID(), sm);
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid message type");
                    }


                } catch (Exception e) {
                    System.err.println("Error during deserialization: " + e.getMessage());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Done \n\n");
        }
    }



    /**
     * Assigns idle elevators to floor requests and adds the request to pending floor requests.
     * Pending floor requests are ones that are in the process of being serviced, they
     * will have a counterpart "working elevator"
     *
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

                    if(floorRequest[0] == null){
                        throw new IllegalArgumentException("Invalid floorRequest: " + null + " for floorRequestId: " + floorRequestId);
                    }

                    elevatorOutMessagePayload.addAll(Arrays.stream(floorRequest).toList());
                    floorRequestBuffer.remove(floorRequestId);
                    idleElevators.remove(idleElevatorId);
                }
            }
            MessageHelper.SendMessages(outSocket, elevatorOutMessagePayload, InetAddress.getLocalHost(), elevatorSubSystemPort);
        }
    public void startSystem() throws IOException {
        while (true){
            readBuffer();
            serveElevatorReqs();
            serveFloorRequests();
        }
    }

    public static void main(String[] args) throws IOException {
        //TODO: Message buffers will no longer be shared objects, constructor needs to be reworked
        Scheduler s = new Scheduler(InetAddress.getLocalHost(),8080, 8081,8082); // This is a dummy constructor for testing do not use this
        s.startSystem();
    }
}
