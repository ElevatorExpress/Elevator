package Messages;

import java.util.Map;
import java.util.UUID;

//T is the shape of the payload data

//S is the Signal type
public interface MessageInterface<T> {
    MessageTypes getType();

    Signal getSignal();

    Map<String, T> getData();
    String getSenderID();

    String getMessageId();
}