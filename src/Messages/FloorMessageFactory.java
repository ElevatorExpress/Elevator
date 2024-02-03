package Messages;

import java.util.Map;
import java.util.UUID;

public class FloorMessageFactory {

    public static FloorMessage createFloorMessage(String floorID, Map<String, MessageInterface> data, SubSystemSignals signal){
        return new FloorMessage(MessageTypes.FLOOR, floorID, data, signal, UUID.randomUUID().toString());
    }

}
