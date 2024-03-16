package elevator;

import util.Messages.SerializableMessage;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ElevatorRequestOrder {

    private static final int MAX_REQUESTS = 5;
    public static synchronized SerializableMessage[] getRequest(ConcurrentLinkedQueue<SerializableMessage> messageBuffer) {
        System.out.println("WAITING");
        while (messageBuffer.isEmpty()) {}
        System.out.println("DONE WAITING");
        String direction = messageBuffer.peek().data().direction();
        int startingFloor = Integer.parseInt(messageBuffer.peek().data().requestFloor());
        ArrayList<SerializableMessage> returnList = new ArrayList<>();
        returnList.add(messageBuffer.poll());

        for (SerializableMessage request : messageBuffer) {
            if(returnList.size() >= MAX_REQUESTS) {
                break;
            }
            if(direction.equals("up")) {
                if(request.data().direction().equals(direction)) {
                    int requestFloor = Integer.parseInt(request.data().requestFloor());
                    if(requestFloor >= startingFloor) {
                        returnList.add(messageBuffer.poll());
                    }
                }
            }
            else {
                if(request.data().direction().equals(direction)) {
                    int requestFloor = Integer.parseInt(request.data().requestFloor());
                    if(requestFloor <= startingFloor) {
                        returnList.add(messageBuffer.poll());
                    }
                }
            }
        }
        SerializableMessage[] returnListArray = new SerializableMessage[returnList.size()];
        returnList.toArray(returnListArray);
        return returnListArray;
    }
}
