package util;

import elevator.ElevatorRequestOrder;
import elevator.ElevatorRequestOrder.*;
import floor.FloorInfoReader;
import util.Messages.MessageTypes;
import util.Messages.SerializableMessage;
import util.Messages.Signal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Data structure to pass messages within the system
 * @author Connor Beleznay
 */
public class MessageBuffer {

    String bufferName;

    private DatagramSocket socket;
    private InetSocketAddress address;
    private int port;

    private final LinkedBlockingQueue<SerializableMessage> messageBuffer = new LinkedBlockingQueue<>();

    /**
     *
     * @param size
     * @param bufferName
     * @param socket
     * @param address
     * @param port
     */
    public MessageBuffer(int size, String bufferName, DatagramSocket socket, InetSocketAddress address, int port) {

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

    public void listenAndFillBuffer(){
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    byte[] buff = new byte[1024];
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
    //ToDo: I think this will work, I'm hoping that .toArray() will use output params.
    public SerializableMessage[] get() throws InterruptedException {
        //Loops until the buffer is not empty
            //Grabs the messages from the buffer
            SerializableMessage[] messages = new SerializableMessage[messageBuffer.size()];
            for (int i = 0; i < messages.length; i++) {
                messages[i] = messageBuffer.take();
            }
            return messages;
    }

    public SerializableMessage[] getForElevators() {
        return ElevatorRequestOrder.getRequest(messageBuffer);
    }

    /**
     * Waits until buffer is available then fills it with messages
     * @param messages the messages being added to the buffer
     */
    public void put(ArrayList<SerializableMessage> messages) {
        MessageHelper.SendMessages(socket,  messages, address.getAddress(), port);
    }


    /**
     * @param type
     * @param senderId
     * @param messageID
     * @param reqID
     * @param workData
     */
    public void put(Signal signal, MessageTypes type, int senderId, String messageID, String reqID, FloorInfoReader.Data workData) throws IOException {
        ArrayList<SerializableMessage> messages = new ArrayList<>();

        SerializableMessage message = new SerializableMessage(address.getHostName(),port,signal,type, senderId, messageID, reqID, workData);
        MessageHelper.SendMessage(socket, message, address.getAddress(), port);
//        MessageHelper.SendMessage(socket, new SerializableMessage(address.getHostName(),port,signal,type, senderId, messageID, reqID, workData), address.getAddress(), port);
//        messages.add(new SerializableMessage(address.getHostName(),port,state,type, id, workData));
    }



}

