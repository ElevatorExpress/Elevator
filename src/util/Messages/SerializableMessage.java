package util.Messages;

import floor.FloorInfoReader;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Optional;


/**
 *
 * @param senderAddr
 * @param senderPort
 * @param signal
 * @param type
 * @param senderID Int representing the sender's ID ie floor number
 * @param messageID UUID for the senders message
 * @param reqID UUID for the request being responded to.
 */
public record SerializableMessage(String senderAddr, int senderPort, Signal signal, MessageTypes type, int senderID, String messageID, Optional<String> reqID, Optional<FloorInfoReader.Data> data) implements Serializable {
}
