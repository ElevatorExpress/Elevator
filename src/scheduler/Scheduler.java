package scheduler;

import util.Messages.ElevatorMessage;
import util.Messages.MessageInterface;
import util.Messages.MessageTypes;
import util.ElevatorLogger;
import util.MessageBuffer;

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
    private MessageBuffer messageBuffer;
    private MessageBuffer floorOutBuffer;
    private MessageBuffer elevatorOutBuffer;

    // Key is Message ID, Value is the message
    //Requests are taken from the shared buffer and parsed into the appropriate buffer, these are requests that have yet
    //to be serviced
    HashMap<String, ElevatorMessage> elevatorRequestBuffer = new HashMap<>();
    private HashMap<String, MessageInterface> floorRequestBuffer = new HashMap<>();

    //Key is Elevator/floor ID, Value is the message, store for elevators ready for work
    private HashMap<String, ElevatorMessage> idleElevators = new HashMap<>();

    //Key is Elevator/floor ID, Value is the message, store for elevators currently working
    private HashMap<String, ElevatorMessage> workingElevators = new HashMap<>();

    //Key is Message ID, Value is the message, store for floor requests that are pending
    private HashMap<String, MessageInterface> pendingFloorRequests = new HashMap<>();

    //Key is Message ID, Value is the message, store for elevator requests that are pending
    private HashMap<String, MessageInterface> pendingElevatorRequests = new HashMap<>();
    private static final ElevatorLogger logger = new ElevatorLogger("scheduler.Scheduler");


    //ToDo: Replace synchronized with semaphores to avoid circular wait.
    
    /**
     * The scheduler.Scheduler class represents a scheduler in an elevator system.
     * It is responsible for managing message buffers and handling floor requests.
     */
        /**
         * Constructs a new scheduler.Scheduler object with the specified message buffers.
         * 
         * @param messageBuffer    Shared buffer for incoming messages
         * @param floorOutBuffer   Shared message buffer for outgoing messages to floors
         * @param elevatorOutBuffer   Shared message buffer for outgoing messages to elevators
         */
        public Scheduler(MessageBuffer messageBuffer, MessageBuffer floorOutBuffer, MessageBuffer elevatorOutBuffer) {
            this.messageBuffer = messageBuffer;
            this.floorOutBuffer = floorOutBuffer;
            this.elevatorOutBuffer = elevatorOutBuffer;
        }

    /**
     * Filler constructor do not use
     */
    private Scheduler(){
        this(new MessageBuffer(10, "Dummy"),
                new MessageBuffer(10, "FloorDummy"),
                new MessageBuffer(10, "ElevatorDummy"));
    }




    /**
     *
     * @return A HashMap containing the idle elevators.
     */

    public HashMap<String, ElevatorMessage> getIdleElevators() {
        return idleElevators;
    }

    /**
     *
     * @return A HashMap containing the working elevators.
     */
    public HashMap<String, ElevatorMessage> getWorkingElevators() {
        return workingElevators;
    }

    /**
     *
     * @return A HashMap containing the pending floor requests.
     */
    public HashMap<String, MessageInterface> getPendingFloorRequests() {
        return pendingFloorRequests;
    }

    /**
     *
     * @return A HashMap containing the pending elevator requests.
     */
    public HashMap<String, MessageInterface> getFloorRequestBuffer() {
        return floorRequestBuffer;
    }


    /**
     * Retrieves messages from the elevatorRequestBuffer, this is called after the messages have been removed from the
     * shared buffer. Only scheduler.Scheduler has access to this buffer.
     * Close requests once they are fulfilled. it is called after thread woken up from wait
     * and messages are in the buffer.
     * No params, no return, public for testing purposes
     */
    public void serveElevatorReqs(){
        String[] keys = elevatorRequestBuffer.keySet().toArray(new String[0]);
        for (String messageId : keys) {
            ElevatorMessage message = elevatorRequestBuffer.get(messageId);
            logger.info("Elevator Request: " + message);
            if (message.getType().equals(MessageTypes.ELEVATOR)){
                switch (message.getSignal()) {
                    case IDLE:
                        idleElevators.put(message.getSenderID(), message);
                        elevatorRequestBuffer.remove(messageId);
                        logger.info("Elevator " + message.getSenderID() + " is now idle");
                        break;
                    case WORKING:
                        //USES Sender ID as KEY
                        workingElevators.put(message.getSenderID(), message);
                        logger.info("Elevator " + message.getSenderID() + " is now working");
                        break;
                    case DONE:
                        logger.info("Elevator " + message.getSenderID() + " is now done");
                        workingElevators.remove(message.getSenderID());
                        if(message.getData() == null || !message.getData().containsKey("Servicing")){
                            throw new IllegalArgumentException("Invalid data: " + message.getData() + " for message: " + message);
                        }
                        //values associated with servicing is the origional message that was sent to the elevator
                        MessageInterface doneFMessage = (MessageInterface) message.getData().get("Servicing");
                        String completedFloorReqID = doneFMessage.getMessageId();
                        if(pendingFloorRequests.containsKey(completedFloorReqID)){
                            pendingFloorRequests.remove(completedFloorReqID);
                        }
                        floorOutBuffer.put(new MessageInterface[]{message});
                        idleElevators.put(message.getSenderID(), message);
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
        logger.info("SCHEDULER READING BUFFER");
        MessageInterface[] messages = messageBuffer.get();
        logger.info("SCHEDULER READ BUFFER");
        for (MessageInterface message : messages) {
            if (message.getType().equals(MessageTypes.ELEVATOR)) {

                try{
                    ElevatorMessage elevatorMessage = (ElevatorMessage) message;
                    elevatorRequestBuffer.put(elevatorMessage.getMessageId(), elevatorMessage);
                } catch (ClassCastException e){
                    logger.info("Invalid message type");

                }
            } else if (message.getType().equals(MessageTypes.FLOOR)) {

                floorRequestBuffer.put(message.getMessageId(), message);
            }
        }
        return 0;
    }



    /**
     * Assigns idle elevators to floor requests and adds the request to pending floor requests.
     * Pending floor requests are ones that are in the process of being serviced, they
     * will have a counterpart "working elevator"
     *
     * No params, no return, public for testing purposes
     */

    public void serveFloorRequests() {
            //go through idle elevators and assign them to floor requests, add the request to pending floor requests
            String[] floorRequestKeys = floorRequestBuffer.keySet().toArray(new String[0]);
            ArrayList<MessageInterface> elevatorOutMessagePayload = new ArrayList<>();
            for (String floorRequestId : floorRequestKeys) {
                if (!idleElevators.isEmpty() && !floorRequestBuffer.isEmpty()) {
                    String idleElevatorId = idleElevators.keySet().iterator().next();
                    pendingFloorRequests.put(floorRequestId, floorRequestBuffer.get(floorRequestId));
                    MessageInterface[] floorRequest = {pendingFloorRequests.get(floorRequestId)};
                    if(floorRequest[0] == null){
                        throw new IllegalArgumentException("Invalid floorRequest: " + floorRequest[0] + " for floorRequestId: " + floorRequestId);
                    }
                    elevatorOutMessagePayload.addAll(Arrays.stream(floorRequest).toList());
                    floorRequestBuffer.remove(floorRequestId);
                    idleElevators.remove(idleElevatorId);
                }
            }
            elevatorOutBuffer.put(elevatorOutMessagePayload.toArray(new MessageInterface[0]));
        }
    public void startSystem(){
        while (true){
            readBuffer();
            serveElevatorReqs();
            serveFloorRequests();
        }
    }

    public static void main(String[] args) {
        //TODO: Message buffers will no longer be shared objects, constructor needs to be reworked
        Scheduler s = new Scheduler(); // This is a dummy constructor for testing do not use this
        s.startSystem();
    }
}
