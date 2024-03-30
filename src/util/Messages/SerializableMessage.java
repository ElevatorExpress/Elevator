package util.Messages;

import floor.FloorInfoReader;

import java.io.Serializable;


/**
 * Record containing all necessary data that is shared between systems.
 * @param senderAddr The address of the sender. eg. "localhost"
 * @param senderPort The port that the sender listens to.
 * @param signal The Signal of the message
 * @param type The message type
 * @param senderID Int representing the sender's ID ie floor number
 * @param messageID UUID for the senders message
 * @param reqID UUID for the request being responded to.
 * @param data Data from the request
 */
public record SerializableMessage(String senderAddr, int senderPort, Signal signal, MessageTypes type, int senderID, String messageID, String reqID, FloorInfoReader.Data data) implements Serializable {
}
