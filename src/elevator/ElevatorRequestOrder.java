package elevator;

import util.Direction;
import util.Messages.SerializableMessage;
import util.WorkAssignment;

import java.util.ArrayList;
import java.util.Collections;
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

    public static synchronized ArrayList<WorkAssignment> getRequest(ArrayList<WorkAssignment> requests ) {
        if (requests.isEmpty()) return new ArrayList<>();

        Direction direction = requests.get(0).getDirection();
        ArrayList<WorkAssignment> groupedRequests = new ArrayList<>();
        ArrayList<WorkAssignment> temp = new ArrayList<>(requests);
        for (WorkAssignment request : temp ) {
            if(groupedRequests.size() >= MAX_REQUESTS) {
                break;
            }
            if (request.getDirection() == direction) {
                groupedRequests.add(request);
                requests.remove(request);
            }
        }
        return groupedRequests;
    }
}
