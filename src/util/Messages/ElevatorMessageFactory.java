package util.Messages;

import java.util.Map;
import java.util.UUID;

/**
 * Factory for creating ElevatorMessages
 * @param <T> Generic type representing data to be placed inside a data map of the message
 */
@Deprecated(forRemoval = true, since = "March 3 2024")
public class ElevatorMessageFactory<T> {

    /**
     * Creates an ElevatorMessage
     * @param elevatorID The unique ID of the message sender
     * @param data  The data map that will be sent
     * @param signal The new state of that the sender of the message
     * @return An ElevatorMessage
     */
    public ElevatorMessage<T> createElevatorMessage(String elevatorID, Map<String, T> data, Signal signal){
        return new ElevatorMessage<>(MessageTypes.ELEVATOR, elevatorID, data, signal, UUID.randomUUID().toString());
    }

}