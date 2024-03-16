package elevator;

import util.Messages.SerializableMessage;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ElevatorRequestOrder {

    private static final int MAX_REQUESTS = 5;
    public static synchronized SerializableMessage[] getRequest(LinkedBlockingQueue<SerializableMessage> messageBuffer) {
        if (messageBuffer.peek() == null) return new SerializableMessage[0];

        String direction = messageBuffer.peek().data().direction();
        ArrayList<SerializableMessage> groupedRequests = new ArrayList<>();

        for (SerializableMessage request : messageBuffer) {
            if(groupedRequests.size() >= MAX_REQUESTS) {
                break;
            }
            if (request.data().direction().equals(direction)) {
                groupedRequests.add(request);
                messageBuffer.remove(request);
            }
        }
        return groupedRequests.toArray(new SerializableMessage[0]);
    }
}
