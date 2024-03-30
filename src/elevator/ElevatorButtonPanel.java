package elevator;

import java.util.HashMap;

/**
 * Class that represents the panel of elevator buttons in an elevator
 * @author Joshua Braddon
 */
public class ElevatorButtonPanel {
    //The mapping will be floor -> on/off
    private HashMap<Integer, String> buttons = new HashMap<>();

    /**
     * Creates the panel with the given number of floors
     * @param size number of floors
     */
    public ElevatorButtonPanel(int size) {
        for(int i = 0; i < size; i++) {
            buttons.put(i + 1, "off");
        }
    }

    /**
     * Turns on the button at a given floor
     * @param floor the given floor
     */
    public void turnOnButton(int floor) {
        buttons.put(floor, "on");
    }
    /**
     * Turns off the button at a given floor
     * @param floor the given floor
     */
    public void turnOffButton(int floor) {
        buttons.put(floor, "off");
    }
}
