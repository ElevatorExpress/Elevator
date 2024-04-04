package gui;

import util.Direction;

public interface ElevatorListener {

    /**
     * Ran when the current floor is updated. Provides current floor.
     * @param id The elevator ID
     * @param floor The elevator's current floor
     */
    void updateCurrentFloor(int id, int floor);

    /**
     * Ran when the moving state is updated. Provides moving state
     * @param id The elevator ID
     * @param moving The elevator's current moving state
     */
    void updateMovingState(int id, Moving moving);
    /**
     * Ran when the direction is updated. Provides the direction
     * @param id The elevator ID
     * @param direction The elevator's direction
     */
    void updateDirection(int id, Direction direction);

    /**
     * Ran when the capacity is updated. Provides the capacity
     * @param id The elevator ID
     * @param capacity The elevator's current capacity
     */
    void updateCapacity(int id, int capacity);

    enum Moving {
        MOVING, STOPPED, EMERG
    }

}
