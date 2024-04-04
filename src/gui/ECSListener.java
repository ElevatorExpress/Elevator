package gui;

import util.ElevatorStateUpdate;

import java.util.Map;

public interface ECSListener {

    /**
     * Event is launched when an elevator is potentially updated
     * @param updateHashMap The updated elevator states associated with their IDs'
     */
    void updateElevators(Map<Integer, ElevatorStateUpdate> updateHashMap);

}
