import Messages.*;

import javax.print.attribute.HashDocAttributeSet;
import java.util.HashMap;
import java.util.ArrayList;

public class Scheduler implements Runnable {
    private MessageBuffer messageBuffer;
    private MessageBuffer floorOutBuffer;
    private MessageBuffer elevatorOutBuffer;

//    private HashMap<String, SubSystem<MessageInterface>> floorSubscribers;
//
//    private HashMap<String, SubSystem<MessageInterface>> elevatorSubscribers;

    HashMap<String, ElevatorMessage> elevatorRequestBuffer = new HashMap<>();
    private HashMap<String, MessageInterface> floorRequestBuffer = new HashMap<>();

    //elevator id: ElevatorMessage
    private HashMap<String, ElevatorMessage> idleElevators = new HashMap<>();

    //elevator id: ElevatorMessage
    private HashMap<String, ElevatorMessage> workingElevators = new HashMap<>();

    //elevator id: ElevatorMessage
    private HashMap<String, MessageInterface> pendingFloorRequests = new HashMap<>();

    private HashMap<String, MessageInterface> pendingElevatorRequests = new HashMap<>();






    public Scheduler(MessageBuffer messageBuffer, MessageBuffer floorOutBuffer, MessageBuffer elevatorOutBuffer) {
        this.messageBuffer = messageBuffer;
//        this.floorSubscribers = floorSubscribers;
//        this.elevatorSubscribers = elevatorSubscribers;
        this.floorOutBuffer = floorOutBuffer;
        this.elevatorOutBuffer = elevatorOutBuffer;
        //generate array of floor requests using MessageFactory
        ArrayList<MessageInterface> floorRequests = new ArrayList<>();



        
        
    }

//    public int addFloorSubscriber(String id, Floor floor) {
//        floorSubscribers.put(id, floor);
//        return floorSubscribers.size();
//    }

//    public int addElevatorSubscriber(String id, Object elevator) {
//        elevatorSubscribers.put(id, elevator);
//        return elevatorSubscribers.size();
//    }

//    public int removeFloorSubscriber(String id) {
//        floorSubscribers.remove(id);
//        return floorSubscribers.size();
//    }
//
//    public int removeElevatorSubscriber(String id) {
//        elevatorSubscribers.remove(id);
//        return elevatorSubscribers.size();
//    }

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
                        //Get current floor and pass it here
                        idleElevators.put(message.getSenderID(), message);
                        elevatorRequestBuffer.remove(messageId);
                        break;
                    case WORKING:
                        workingElevators.put(message.getSenderID(), message);
                        elevatorRequestBuffer.remove(messageId);
                        MessageInterface origionalFloorRequest = (MessageInterface) pendingElevatorRequests.get(message.getSenderID());
                        break;
                    case DONE:
                        //Get completed info
                        MessageInterface completed = workingElevators.get(message.getSenderID());
                        workingElevators.remove(message.getSenderID());
                        //get floor requestid from completed data
                        //send completed message to floor
                        if(completed.getData() == null || !completed.getData().containsKey("FloorRequestId")){
                            throw new IllegalArgumentException("Invalid data");
                        }
                        if(pendingFloorRequests.containsKey(completed.getData().get("Servicing"))){
                            //send completed message to floor
                            // floorSubscribers
                            //         .get(pendingFloorRequests
                            //                 .get(completed.getData().get("FloorRequestId"))
                            //                 .getSenderID())
                            //         .receiveMessage(new ElevatorMessage[]{completed});
            //
                            MessageInterface origionalFloorRequest = (MessageInterface) completed.getData().get("Servicing");
                            String origionalFloorResuestId = origionalFloorRequest.getMessageId();
                            String origionalFloorId = origionalFloorRequest.getSenderID();

                            floorOutBuffer.put(new MessageInterface[]{completed});
                            pendingFloorRequests.remove(origionalFloorResuestId);
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
        for (String floorRequestId : floorReqKeys) {
            if (!idleElevators.isEmpty() && !floorRequestBuffer.isEmpty()) {
                //Get the first avialable elevator
                String idleElevatorId = idleElevators.keySet().iterator().next();
//                pendingFloorRequests.put(floorRequestId, floorRequestBuffer.get(floorRequestId));
//                floorRequestBuffer.remove(floorRequestId);
                //This will likely be an asynchronous call, for threads we'll probably have to wake up the elevator
//                elevatorSubscribers.get(idleElevatorId).receiveMessage(new MessageInterface[]{floorRequestBuffer.get(floorRequestId)});
//                workingElevators.put(idleElevatorId, idleElevators.get(idleElevatorId));
                idleElevators.remove(idleElevatorId);
                pendingElevatorRequests.put(idleElevatorId, floorRequestBuffer.get(floorRequestId));
                elevatorOutBuffer.put(new MessageInterface[]{floorRequestBuffer.get(floorRequestId)});
                floorRequestBuffer.remove(floorRequestId);

                // Send it
            }
        }



    }
    public void readBuffer(){
        MessageInterface[] messages = messageBuffer.get();
        for (MessageInterface message : messages) {
            if (message.getType().equals(MessageTypes.ELEVATOR)) {
                //get message and cast to ElevatorMessage
                try{
                    ElevatorMessage elevatorMessage = (ElevatorMessage) message;
                    elevatorRequestBuffer.put(elevatorMessage.getMessageId(), elevatorMessage);
                } catch (ClassCastException e){
                    System.err.println("Invalid message type");
//                    elevatorRequestBuffer.put(message.getSenderID(), message);
                }
            } else if (message.getType().equals(MessageTypes.FLOOR)) {

                floorRequestBuffer.put(message.getMessageId(), message);
            }
        }
//        serveElevatorReqs();
    }




    public void serveFloorRequests() {
            //go through idle elevators and assign them to floor requests, add the request to pending floor requests
            for (String floorRequestId : floorRequestBuffer.keySet()) {
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





    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        //Add a killSwitch
        while (true) {
            readBuffer();
            serveElevatorReqs();
            serveFloorRequests();
        }
    }
}
