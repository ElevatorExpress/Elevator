package util.Messages;

import floor.FloorInfoReader;

import java.io.Serializable;


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
public record SerializableMessage(String senderAddr, int senderPort, Signal signal, MessageTypes type, int senderID, String messageID, String reqID, FloorInfoReader.Data data) implements Serializable {
}
