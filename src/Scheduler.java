import Messages.ElevatorMessage;
import Messages.MessageInterface;

import java.util.HashMap;

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
  






    public Scheduler(MessageBuffer messageBuffer, MessageBuffer floorOutBuffer, MessageBuffer elevatorOutBuffer, HashMap<String, SubSystem<MessageInterface>> floorSubscribers, HashMap<String,SubSystem<MessageInterface>> elevatorSubscribers) {
        this.messageBuffer = messageBuffer;
//        this.floorSubscribers = floorSubscribers;
//        this.elevatorSubscribers = elevatorSubscribers;
        this.floorOutBuffer = floorOutBuffer;
        this.elevatorOutBuffer = elevatorOutBuffer;
        
    
        
        
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



    public void serveElevatorReqs(){

        for (String messageId : elevatorRequestBuffer.keySet()) {
            ElevatorMessage message = elevatorRequestBuffer.get(messageId);
            if (message.getType().equalsIgnoreCase("Elevator")){
                switch (message.getSignal().toString()) {
                    case "IDLE":
                        //Get current floor and pass it here
                        idleElevators.put(message.getId(), message);
                        break;
                    case "WORKING":
                        workingElevators.put(message.getId(), message);
                        break;
                    case "DONE":
                        //Get completed info
                        MessageInterface completed = workingElevators.get(message.getId());
                        //get floor requestid from completed data
                        //send completed message to floor
                        if(completed.getData() == null || !completed.getData().containsKey("FloorRequestId")){
                            throw new IllegalArgumentException("Invalid data");
                        }
                        if(pendingFloorRequests.containsKey(completed.getData().get("FloorRequestId"))){
                            //send completed message to floor
                            // floorSubscribers
                            //         .get(pendingFloorRequests
                            //                 .get(completed.getData().get("FloorRequestId"))
                            //                 .getId())
                            //         .receiveMessage(new ElevatorMessage[]{completed});
            //              
//
                            floorOutBuffer.put(new MessageInterface[]{completed});
                            pendingFloorRequests.remove(completed.getData().get("FloorRequestId"));
                        }

                        idleElevators.put(message.getId(), message);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid signal");
                }
            }




        }
    }
    public void readBuffer(){
        MessageInterface[] messages = messageBuffer.get();
        for (MessageInterface message : messages) {
            if (message.getType().equals("ElevatorRequest")) {
                //get message and cast to ElevatorMessage
                try{
                    ElevatorMessage elevatorMessage = (ElevatorMessage) message;
                    elevatorRequestBuffer.put(elevatorMessage.getMessageId(), elevatorMessage);
                } catch (ClassCastException e){
                    System.err.println("Invalid message type");
//                    elevatorRequestBuffer.put(message.getId(), message);
                }
            } else if (message.getType().equals("FloorRequest")) {

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
