package Messages;

import java.util.Map;
import java.util.UUID;

/**
 * Factory for creating FloorMessages
 * @param <T> Generic type representing data to be placed inside a data map of the message
 */
public class FloorMessageFactory <T>{
    /**
     * Creates a FloorMessage
     * @param floorID The unique ID of the message sender
     * @param data  The data map that will be sent
     * @param signal The new state of that the sender of the message
     * @return A FloorMessage
     */
    public FloorMessage<T> createFloorMessage(String floorID, Map<String, T> data, Signal signal){
        return new FloorMessage<>(MessageTypes.FLOOR, floorID, data, signal, UUID.randomUUID().toString());
    }

}
