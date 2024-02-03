import Messages.*;

import javax.print.attribute.HashDocAttributeSet;
import java.util.HashMap;
import java.util.ArrayList;

public class Scheduler implements Runnable {
    private MessageBuffer messageBuffer;
    private MessageBuffer floorOutBuffer;
    private MessageBuffer elevatorOutBuffer;

    HashMap<String, ElevatorMessage> elevatorRequestBuffer = new HashMap<>();
    private HashMap<String, MessageInterface> floorRequestBuffer = new HashMap<>();


    private HashMap<String, ElevatorMessage> idleElevators = new HashMap<>();

    private HashMap<String, ElevatorMessage> workingElevators = new HashMap<>();

    private HashMap<String, MessageInterface> pendingFloorRequests = new HashMap<>();

    private HashMap<String, MessageInterface> pendingElevatorRequests = new HashMap<>();






    public Scheduler(MessageBuffer messageBuffer, MessageBuffer floorOutBuffer, MessageBuffer elevatorOutBuffer) {
        this.messageBuffer = messageBuffer;

        this.floorOutBuffer = floorOutBuffer;
        this.elevatorOutBuffer = elevatorOutBuffer;

        ArrayList<MessageInterface> floorRequests = new ArrayList<>();



        
        
    }


    public HashMap<String, ElevatorMessage> getIdleElevators() {
        return idleElevators;
    }
    public HashMap<String, ElevatorMessage> getWorkingElevators() {
        return workingElevators;
    }
    public HashMap<String, MessageInterface> getPendingFloorRequests() {
        return pendingFloorRequests;
    }
    public HashMap<String, MessageInterface> getFloorRequestBuffer() {
        return floorRequestBuffer;
    }
    public void serveElevatorReqs(){
        String[] keys = elevatorRequestBuffer.keySet().toArray(new String[0]);
        for (String messageId : keys) {
            ElevatorMessage message = elevatorRequestBuffer.get(messageId);
            if (message.getType().equals(MessageTypes.ELEVATOR)){

                switch (message.getSignal()) {
                    case IDLE:

                        idleElevators.put(message.getSenderID(), message);
                        elevatorRequestBuffer.remove(messageId);
                        break;
                    case WORKING:
                        workingElevators.put(message.getSenderID(), message);
                        elevatorRequestBuffer.remove(messageId);
                        MessageInterface origionalFloorRequest = (MessageInterface) pendingElevatorRequests.get(message.getSenderID());
                        pendingElevatorRequests.remove(message.getSenderID());
                        pendingFloorRequests.put(origionalFloorRequest.getMessageId(), message);

                        break;
                    case DONE:
                        MessageInterface completed = workingElevators.get(message.getSenderID());
                        workingElevators.remove(message.getSenderID());

                        if(completed.getData() == null || !completed.getData().containsKey("FloorRequestId")){
                            throw new IllegalArgumentException("Invalid data");
                        }
                        if(pendingFloorRequests.containsKey(completed.getData().get("Servicing"))){

                            MessageInterface servicedFloorReq = (MessageInterface) completed.getData().get("Servicing");
                            String servicedFloorReqId = servicedFloorReq.getMessageId();
                            String servicedFloorFloorId = servicedFloorReq.getSenderID();

                            floorOutBuffer.put(new MessageInterface[]{completed});
                            pendingFloorRequests.remove(servicedFloorReqId);
                        }

                        idleElevators.put(message.getSenderID(), message);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid signal");
                }
            }




        }


    }

    public void schedule(){
        //go through idle elevators and assign them to floor requests.
        //send work request to elevator

        String[] floorReqKeys = floorRequestBuffer.keySet().toArray(new String[0]);
        String[] idleElevatorKeys = idleElevators.keySet().toArray(new String[0]);

        ArrayList<MessageInterface> elevatorOutMessagePayload = new ArrayList<>();
        for (String floorRequestId : floorReqKeys) {
            if (!idleElevators.isEmpty() && !floorRequestBuffer.isEmpty()) {

                String idleElevatorId = idleElevators.keySet().iterator().next();

                idleElevators.remove(idleElevatorId);
                pendingElevatorRequests.put(idleElevatorId, floorRequestBuffer.get(floorRequestId));

                elevatorOutMessagePayload.add(floorRequestBuffer.get(floorRequestId));
                floorRequestBuffer.remove(floorRequestId);

            }
        }
        elevatorOutBuffer.put(elevatorOutMessagePayload.toArray(new MessageInterface[0]));



    }
    public void readBuffer(){
        MessageInterface[] messages = messageBuffer.get();
        for (MessageInterface message : messages) {
            if (message.getType().equals(MessageTypes.ELEVATOR)) {

                try{
                    ElevatorMessage elevatorMessage = (ElevatorMessage) message;
                    elevatorRequestBuffer.put(elevatorMessage.getMessageId(), elevatorMessage);
                } catch (ClassCastException e){
                    System.err.println("Invalid message type");

                }
            } else if (message.getType().equals(MessageTypes.FLOOR)) {

                floorRequestBuffer.put(message.getMessageId(), message);
            }
        }

    }




    public void serveFloorRequests() {
            //go through idle elevators and assign them to floor requests, add the request to pending floor requests
            String[] floorRequestKeys = floorRequestBuffer.keySet().toArray(new String[0]);
            for (String floorRequestId : floorRequestKeys) {
                if (!idleElevators.isEmpty() && !floorRequestBuffer.isEmpty()) {
                    //Get the first avialable elevator
                    String idleElevatorId = idleElevators.keySet().iterator().next();

                    pendingFloorRequests.put(floorRequestId, floorRequestBuffer.get(floorRequestId));
                    floorRequestBuffer.remove(floorRequestId);

                    //This will likely be an asynchronous call, for threads we'll probably have to wake up the elevator
//                    elevatorSubscribers.get(idleElevatorId).receiveMessage(new MessageInterface[]{floorRequestBuffer.get(floorRequestId)});
                    elevatorOutBuffer.put(new MessageInterface[]{floorRequestBuffer.get(floorRequestId)});
                    workingElevators.put(idleElevatorId, idleElevators.get(idleElevatorId));
                    idleElevators.remove(idleElevatorId);

                    // Send it
                }
            }
        }



    public void startSystem(){

        readBuffer();
        serveElevatorReqs();
        serveFloorRequests();
        schedule();
    }

    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        //Add a killSwitch
        while (true) {
            startSystem();
        }
    }
}
