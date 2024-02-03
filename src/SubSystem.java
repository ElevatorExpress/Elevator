import Messages.MessageInterface;

import java.util.UUID;

public interface SubSystem <T> extends Runnable {

    //Java doc: This method receives a message from the subsystem and processes it
//<<<<<<< HEAD
//    String receiveMessage(T[] message);
//=======
    void receiveMessage();
//>>>>>>> majorRefac
//
    //Java doc: This method sends a message to the subsystem and returns the response
    String[] sendMessage(T[] message);


}
