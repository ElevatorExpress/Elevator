package elevator;

import util.Direction;
import util.Messages.SerializableMessage;
import util.WorkAssignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Creates helps parse a message to create a Request order
 * @author Yasir Sheikh
 */
public class ElevatorRequestOrder {

    //Each elevator will take 5 requests maximum
    private static final int MAX_REQUESTS = 5;

    /**
     * Parses SerializableMessages to create groupings of request orders
     * @param messageBuffer The message queue that contains the messages to be parsed
     * @return An array grouping of ordered messages
     */
    public static synchronized SerializableMessage[] getRequest(LinkedBlockingQueue<SerializableMessage> messageBuffer) {
        //If there are no messages return an empty one
        if (messageBuffer.peek() == null) return new SerializableMessage[0];

        //Get direction of first request
        String direction = messageBuffer.peek().data().direction();
        ArrayList<SerializableMessage> groupedRequests = new ArrayList<>();

        for (SerializableMessage request : messageBuffer) {
            //If there are ever more than 5 requests
            if(groupedRequests.size() >= MAX_REQUESTS) {
                break;
            }
            //If another request is in the same direction
            if (request.data().direction().equals(direction)) {
                //Add the request to this block
                groupedRequests.add(request);
                //Remove from the request list
                messageBuffer.remove(request);
            }
        }
        //Return a block of requests
        return groupedRequests.toArray(new SerializableMessage[0]);
    }

    /**
     * Parses a list of requests to group and send to elevators
     * @param requests the requests to be picked from
     * @return An array grouping of ordered messages
     */
    public static synchronized ArrayList<WorkAssignment> getRequest(ArrayList<WorkAssignment> requests ) {
        //If there are no messages return an empty one
        if (requests.isEmpty()) return new ArrayList<>();

        //Get direction from the first request
        Direction direction = requests.get(0).getDirection();
        ArrayList<WorkAssignment> groupedRequests = new ArrayList<>();
        ArrayList<WorkAssignment> temp = new ArrayList<>(requests);
        for (WorkAssignment request : temp ) {
            //If there are ever more than 5 requests
            if(groupedRequests.size() >= MAX_REQUESTS) {
                break;
            }
            //If another request is in the same direction
            if (request.getDirection() == direction) {
                //Add the request to this block
                groupedRequests.add(request);
                //Remove from the request list
                requests.remove(request);
            }
        }
        //The grouped block of requests
        return groupedRequests;
    }
}
