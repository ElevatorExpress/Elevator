import Messages.MessageInterface;

import java.util.HashMap;
import java.util.Map;

/**
 * Data structure to pass messages within the system
 * @author Connor Beleznay
 */
public class MessageBuffer {

    //Starts as empty
    private volatile boolean bufferEmpty = true;

    private final MessageInterface[] messageBuffer;

    public MessageBuffer(int size) {
        this.messageBuffer = new MessageInterface[size];
    }

    /**
     * Finds the length of the buffer and returns it
     * @return the length of this buffer
     */
    public synchronized int getBufferLength(){
        int count = 0;
        for (MessageInterface message : messageBuffer) {
            if (message != null) {
                count++;
            }else{
                break;
            }
        }
        return count;
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
    public synchronized MessageInterface[] get() {
        while (bufferEmpty) {
            try {
//                notifyAll();
                wait();
            } catch (InterruptedException e) {
                System.err.println("Producer ERROR: " + e.getMessage());
            }
        }
            MessageInterface[] messages = new MessageInterface[getBufferLength()];
            System.arraycopy(messageBuffer, 0, messages, 0, getBufferLength());
            // null the buffer
            for (int i = 0; i < messageBuffer.length; i++) {
                messageBuffer[i] = null;
            }
            bufferEmpty = true;
            notifyAll();
            return messages;

    }

    /**
     * Waits until buffer is available then fills it with messages
     * @param messages the messages being added to the buffer
     */
    public synchronized void put(MessageInterface[] messages) {

            while (!bufferEmpty) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.err.println("Producer ERROR: " + e.getMessage());
                }
            }
            //Make sure the buffer is the correct size
            if (messages.length > messageBuffer.length || messages.length + getBufferLength() > messageBuffer.length) {
                throw new IllegalArgumentException("Message buffer is too small");
            }

            System.arraycopy(messages, getBufferLength(), messageBuffer, 0, messages.length);
            bufferEmpty = false;
            notifyAll();
    }



}

