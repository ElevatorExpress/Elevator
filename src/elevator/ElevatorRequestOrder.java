package elevator;

import util.Messages.SerializableMessage;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Creates helps parse a message to create a Request order
 */
public class ElevatorRequestOrder {

    private static final int MAX_REQUESTS = 5;

    /**
     * Parses SerializableMessages to create groupings of request orders
     * @param messageBuffer The message queue that contains the messages to be parsed
     * @return An array grouping of ordered messages
     */
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
