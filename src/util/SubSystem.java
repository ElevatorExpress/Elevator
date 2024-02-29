package util;

public interface SubSystem <T> {

    /**
     *  This method receives a message from the subsystem and processes it
     */
    void receiveMessage();

    /**
     * This method sends a message to the subsystem and returns the response
     * @param message Message to be sent
     */
    String[] sendMessage(T[] message);


}
