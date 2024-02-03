package Messages;

import java.util.Map;
import java.util.UUID;

public class FloorMessageFactory<T> {

    public FloorMessage<T> createFloorMessage(String floorID, Map<String, T> data, FloorSignal signal){
        return new FloorMessage<>(MessageTypes.FLOOR, floorID, data, signal, UUID.randomUUID().toString());
    }

}
