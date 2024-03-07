package util;

import floor.FloorInfoReader;
import util.Messages.MessageTypes;
import util.Messages.SerializableMessage;
import util.Messages.Signal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Data structure to pass messages within the system
 * @author Connor Beleznay
 */
public class MessageBuffer {

    //Starts as empty
    private volatile boolean bufferEmpty = true;
    String bufferName;
    private volatile int bufferLength = 0;

    private DatagramSocket socket;
    private InetSocketAddress address;
    private int port;

    private final ConcurrentLinkedQueue<SerializableMessage> messageBuffer = new ConcurrentLinkedQueue<>();

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
        int count = 0;
        for (SerializableMessage message : messageBuffer) {
            if (message != null) {
                count++;
            }else{
                break;
            }
        }
        return count;
    }

    public void listenAndFillBuffer(){
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    SerializableMessage message = MessageHelper.RecieveMessage(socket, new byte[1024], new DatagramPacket(new byte[1024], 1024));
                    messageBuffer.add(message);
                    bufferEmpty = false;
                    bufferLength++;
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
        return bufferEmpty;
    }

    /**
     * Gets the contents of the buffer and then clears the buffer
     * @return the messages inside the buffer
     */
    //ToDo: I think this will work, I'm hoping that .toArray() will use output params.
    public SerializableMessage[] get() {
        //Loops until the buffer is not empty
            //Grabs the messages from the buffer

            SerializableMessage[] messages = new SerializableMessage[messageBuffer.size()];
            messageBuffer.toArray(messages);
//            System.arraycopy( messageBuffer.toArray(), 0, messages, 0, bufferLength);
            // null the buffer
            bufferEmpty = true;
            bufferLength = 0;
//            notifyAll();
            return messages;

    }

    /**
     * Waits until buffer is available then fills it with messages
     * @param messages the messages being added to the buffer
     */
    public void put(ArrayList<SerializableMessage> messages) {
        MessageHelper.SendMessages(socket,  messages, address.getAddress(), port);
    }


    /**
     *
     * @param id
     * @param workData
     * @param state
     * @param type
     */
    public void put(Signal signal, MessageTypes type, int senderId, String messageID, Optional<String> reqID, Optional<FloorInfoReader.Data> workData) throws IOException {
        ArrayList<SerializableMessage> messages = new ArrayList<>();

        SerializableMessage message = new SerializableMessage(address.getHostName(),port,signal,type, senderId, messageID, reqID, workData);
        MessageHelper.SendMessage(socket, message, address.getAddress(), port);
//        MessageHelper.SendMessage(socket, new SerializableMessage(address.getHostName(),port,signal,type, senderId, messageID, reqID, workData), address.getAddress(), port);
//        messages.add(new SerializableMessage(address.getHostName(),port,state,type, id, workData));
    }



}

