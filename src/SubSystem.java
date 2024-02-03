import Messages.MessageInterface;

import java.util.UUID;

public interface SubSystem <T> extends Runnable {

    //Java doc: This method receives a message from the subsystem and processes it
    void receiveMessage();

    //Java doc: This method sends a message to the subsystem and returns the response
    String[] sendMessage(T[] message);


}
