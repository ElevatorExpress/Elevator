package Messages;

import java.util.Map;
import java.util.UUID;

//T is the shape of the payload data

//S is the Signal type
public interface MessageInterface<T, S> {
    String id = UUID.randomUUID().toString();
    String getType();

    S getSignal();

    Map<String, T> getData();
    String getId();

    String getMessageId();
}