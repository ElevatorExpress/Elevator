package Messages;

import java.util.Map;
import java.util.UUID;

public interface MessageInterface<T, S> {
    String id = UUID.randomUUID().toString();
    String getType();

    S getSignal();

    Map<String, T> getData();
    String getId();

    String getMessageId();
}