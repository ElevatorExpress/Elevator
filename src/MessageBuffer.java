import Messages.MessageInterface;

/**
 * Data structure to pass messages within the system
 * @author Connor Beleznay
 */
public class MessageBuffer {

    //Starts as empty
    private volatile boolean bufferEmpty = true;
    String bufferName;
    private volatile int bufferLength = 0;


    private final MessageInterface[] messageBuffer;

    public MessageBuffer(int size, String bufferName) {

        this.messageBuffer = new MessageInterface[size];
        this.bufferName = bufferName;
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
        //Loops until the buffer is not empty
        while (bufferLength < 1) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println("Producer ERROR: " + e.getMessage());
            }
        }
            //Grabs the messages from the buffer
            MessageInterface[] messages = new MessageInterface[bufferLength];
            System.arraycopy(messageBuffer, 0, messages, 0, bufferLength);
            // null the buffer
            for (int i = 0; i < bufferLength; i++) {
                messageBuffer[i] = null;
            }
            bufferEmpty = true;
            bufferLength = 0;
            notifyAll();
            return messages;

    }

    /**
     * Waits until buffer is available then fills it with messages
     * @param messages the messages being added to the buffer
     */
    public synchronized void put(MessageInterface[] messages) {
            while (bufferLength + messages.length > messageBuffer.length){
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.err.println("Producer ERROR: " + e.getMessage());
                }
            }
            //Make sure the buffer is the correct size
            if (messages.length > messageBuffer.length || messages.length + bufferLength > messageBuffer.length) {
                throw new IllegalArgumentException("Message buffer is too small ");
            }

        System.arraycopy(messages, 0, messageBuffer, bufferLength, messages.length);
        bufferLength += messages.length;
        bufferEmpty = false;
            notifyAll();
    }



}

