package util.Messages;

import java.io.Serializable;
import java.util.HashMap;


public record SerializableMessage(String senderAddr, int senderPort, Signal signal, MessageTypes type, String senderID, HashMap<String, SerializableMessage> data) implements Serializable {
}
