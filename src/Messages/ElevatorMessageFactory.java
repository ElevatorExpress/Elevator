package Messages;

import java.util.Map;
import java.util.UUID;

// if sending a message to the server, the data MUST include FloorMessage fulfilled. AT MINIMUM!

public class ElevatorMessageFactory<T> {

//<<<<<<< HEAD
//    public static ElevatorMessage createElevatorMessage(String elevatorID, Map<String, MessageInterface> data, SubSystemSignals signal){
//        return new ElevatorMessage(MessageTypes.ELEVATOR, elevatorID, data, signal, UUID.randomUUID().toString());
//=======
    public ElevatorMessage<T> createElevatorMessage(String elevatorID, Map<String, T> data, Signal signal){
        return new ElevatorMessage<>(MessageTypes.ELEVATOR, elevatorID, data, signal, UUID.randomUUID().toString());
//>>>>>>> majorRefac
    }

}