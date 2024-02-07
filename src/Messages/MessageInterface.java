package Messages;

import java.util.Map;
import java.util.UUID;

/**
 * Interface providing methods that are common to all message types.
 * More details about the message protocol used can be found
 * <a href="https://github.com/ElevatorExpress/Elevator/wiki/Message-Protocol">here</a>.
 * @param <T> Generic type representing data to be placed inside a data map
 */
public interface MessageInterface<T> {
    /**
     * @return The type of the message
     */
    MessageTypes getType();

    /**
     * @return The new state information inside the message
     */
    Signal getSignal();

    /**
     * @return The data contained inside the message
     */
    Map<String, T> getData();

    /**
     * @return The unique ID of the sends of this message
     */
    String getSenderID();

    /**
     * @return The unique ID of this message
     */
    String getMessageId();
}