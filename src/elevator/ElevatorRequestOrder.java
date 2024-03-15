package elevator;

import util.Messages.SerializableMessage;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ElevatorRequestOrder {

    private static final int MAX_REQUESTS = 5;
    public static SerializableMessage[] getRequest(ConcurrentLinkedQueue<SerializableMessage> messageBuffer) {
        String direction = messageBuffer.peek().data().get().direction();
        int startingFloor = Integer.parseInt(messageBuffer.peek().data().get().requestFloor());
        ArrayList<SerializableMessage> returnList = new ArrayList<>();
        returnList.add(messageBuffer.poll());

        for (SerializableMessage request : messageBuffer) {
            if(returnList.size() >= MAX_REQUESTS) {
                break;
            }
            if(direction.equals("up")) {
                if(request.data().get().direction().equals(direction)) {
                    int requestFloor = Integer.parseInt(request.data().get().requestFloor());
                    if(requestFloor >= startingFloor) {
                        returnList.add(messageBuffer.poll());
                    }
                }
            }
            else {
                if(request.data().get().direction().equals(direction)) {
                    int requestFloor = Integer.parseInt(request.data().get().requestFloor());
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
