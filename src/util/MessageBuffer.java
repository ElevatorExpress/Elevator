package util;

import elevator.ElevatorRequestOrder;
import util.Messages.SerializableMessage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Data structure to pass messages within the system
 * @author Connor Beleznay
 */
public class MessageBuffer {

    String bufferName;

    private final DatagramSocket socket;
    private final InetSocketAddress address;
    private final int port;

    private final LinkedBlockingQueue<SerializableMessage> messageBuffer = new LinkedBlockingQueue<>();

    /**
     * Creates a message buffer
     * @param bufferName The name of this buffer
     * @param socket The socket that this buffer uses to listen messages and send messages
     * @param address The target address
     * @param port The target port
     */
    public MessageBuffer(String bufferName, DatagramSocket socket, InetSocketAddress address, int port) {
        this.bufferName = bufferName;
        this.socket = socket;
        this.address = address;
        this.port = port;
    }

    /**
     * Finds the length of the buffer and returns it
     * @return the length of this buffer
     */
    public synchronized int getBufferLength(){
        return messageBuffer.size();
    }

    /**
     * Creates a thread that will continuously listen to messages and fill this buffer
     */
    public void listenAndFillBuffer(){
        ElevatorLogger logger = new ElevatorLogger("ReaderThread");
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    byte[] buff = new byte[1024];
                    logger.info("Waiting for packet");
                    SerializableMessage message = MessageHelper.ReceiveMessage(socket, buff, new DatagramPacket(buff, buff.length));
                    messageBuffer.put(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

    /**
     * Returns true the buffer is empty false if not
     * @return if the buffer is empty
     */
    public boolean isBufferEmpty(){
        return messageBuffer.isEmpty();
    }

    /**
     * Gets the contents of the buffer and then clears the buffer
     * @return the messages inside the buffer
     */
    public SerializableMessage[] get() throws InterruptedException {
        //Loops until the buffer is not empty
            //Grabs the messages from the buffer
            SerializableMessage[] messages = new SerializableMessage[messageBuffer.size()];
            for (int i = 0; i < messages.length; i++) {
                messages[i] = messageBuffer.take();
            }
            return messages;
    }

//    public SerializableMessage[] getForElevators() {
//        return ElevatorRequestOrder.getRequest(messageBuffer);
//    }

    /**
     * Waits until buffer is available then fills it with messages
     * @param messages the messages being added to the buffer
     */
    public void put(ArrayList<SerializableMessage> messages) {
        MessageHelper.SendMessages(socket,  messages, address.getAddress(), port);
    }


}

