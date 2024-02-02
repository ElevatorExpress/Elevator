import Messages.ElevatorMessage;
import Messages.MessageInterface;

import java.util.HashMap;

public class Scheduler implements Runnable {
    private MessageBuffer messageBuffer;

    private HashMap<String,Floor> floorSubscribers;

    private HashMap<String,Object> elevatorSubscribers;

    private HashMap<String, ElevatorMessage> elevatorRequestBuffer;
    private HashMap<String, MessageInterface> floorRequestBuffer;

    //elevator id: ElevatorMessage
    private HashMap<String, ElevatorMessage> idleElevators;

    //elevator id: ElevatorMessage
    private HashMap<String, ElevatorMessage> workingElevators;

    //elevator id: ElevatorMessage
    private HashMap<String, ElevatorMessage> pendingFloorRequests;






    public Scheduler(MessageBuffer messageBuffer, HashMap<String,Floor> floorSubscribers, HashMap<String,Object> elevatorSubscribers) {
        this.messageBuffer = messageBuffer;
        this.floorSubscribers = floorSubscribers;
        this.elevatorSubscribers = elevatorSubscribers;
    }

    public int addFloorSubscriber(String id, Floor floor) {
        floorSubscribers.put(id, floor);
        return floorSubscribers.size();
    }

    public int addElevatorSubscriber(String id, Object elevator) {
        elevatorSubscribers.put(id, elevator);
        return elevatorSubscribers.size();
    }

    public int removeFloorSubscriber(String id) {
        floorSubscribers.remove(id);
        return floorSubscribers.size();
    }

    public int removeElevatorSubscriber(String id) {
        elevatorSubscribers.remove(id);
        return elevatorSubscribers.size();
    }


    private void readBuffer(){
        MessageInterface[] messages = messageBuffer.get();
        for (MessageInterface message : messages) {
            if (message.getType().equals("ElevatorRequest")) {
                //get message and cast to ElevatorMessage
                try{
                    ElevatorMessage elevatorMessage = (ElevatorMessage) message;
                    elevatorRequestBuffer.put(elevatorMessage.getId(), elevatorMessage);
                } catch (ClassCastException e){
                    System.err.println("Invalid message type");
//                    elevatorRequestBuffer.put(message.getId(), message);
                }
            } else if (message.getType().equals("FloorRequest")) {

                floorRequestBuffer.put(message.getId(), message);
            }
        }
    }



    public void serve() {
        while(true){
            readBuffer();
            for (String messageId : elevatorRequestBuffer.keySet()) {
                ElevatorMessage message = elevatorRequestBuffer.get(messageId);


                if (message.getType().equalsIgnoreCase("Elevator")){
                    switch (message.getSignal().toString()) {
                        case "IDLE":
                            //Get current floor and pass it here
                            idleElevators.put(message.getMessageId(), message);
                            break;
                        case "WORKING":
                            workingElevators.put(message.getId(), message);
                            break;
                        case "DONE":
                            //Get completed info
                            ElevatorMessage completed = workingElevators.get(message.getId());
                            //get floor requestid from completed data
                            //send completed message to floor
                            if(completed.getData() == null || !completed.getData().containsKey("FloorRequestId")){
                                throw new IllegalArgumentException("Invalid data");
                            }
                            if(pendingFloorRequests.containsKey(completed.getData().get("FloorRequestId"))){
                                //send completed message to floor
                                floorSubscribers
                                        .get(pendingFloorRequests
                                            .get(completed.getData().get("FloorRequestId"))
                                            .getId())
                                        .receiveMessage(new ElevatorMessage[]{completed});
                            }

                            idleElevators.put(message.getId(), message);
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid signal");
                    }
                }
            }

            // Process requests

        }

    }


    /**
     * Runs this operation.
     */
    @Override
    public void run() {

    }
}
