import Messages.MessageInterface;

import java.util.HashMap;
import java.util.Map;

public class MessageBuffer {

    private volatile boolean bufferEmpty = true;


    private final MessageInterface[] messageBuffer;

//    private final Map<MessageInterface,ResourceType> availableResources = new HashMap<ResourceType,ResourceType>();

    public MessageBuffer(int size) {
        this.messageBuffer = new MessageInterface[size];
    }

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
    public boolean isBufferEmpty(){
        return bufferEmpty;
    }

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
            bufferEmpty = true;
            notifyAll();
            return messages;

    }

    public synchronized void put(MessageInterface[] messages) {

            while (!bufferEmpty) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.err.println("Producer ERROR: " + e.getMessage());
                }
            }
            //There needs to be a check for length
            if (messages.length > messageBuffer.length || messages.length + getBufferLength() > messageBuffer.length) {
                throw new IllegalArgumentException("Message buffer is too small");
            }

            System.arraycopy(messages, getBufferLength(), messageBuffer, 0, messages.length);
            bufferEmpty = false;
            notifyAll();
    }



}

