package Messages;

import java.util.Map;
import java.util.UUID;

//T is the shape of the payload data

//S is the Signal type
public interface MessageInterface<T> {
//<<<<<<< HEAD
//    String id = UUID.randomUUID().toString();
//    String getType();
//=======
    MessageTypes getType();
//>>>>>>> majorRefac

    SubSystemSignals getSignal();

    Map<String, T> getData();
//<<<<<<< HEAD
//    String getId();
//=======
    String getSenderID();
//>>>>>>> majorRefac

    String getMessageId();
}