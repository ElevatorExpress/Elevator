package Messages;

import java.util.Map;
import java.util.UUID;

//T is the shape of the payload data

//S is the Signal type
public interface MessageInterface<T> {

//>>>>>>> majorRefac
    MessageTypes getType();
//>>>>>>> majorRefac

//<<<<<<< HEAD
//    SubSystemSignals getSignal();
//=======
    Signal getSignal();
//>>>>>>> majorRefac

    Map<String, T> getData();
//<<<<<<< HEAD
//    String getId();
//=======
    String getSenderID();
//>>>>>>> majorRefac

    String getMessageId();
}