package util;

import util.Messages.Signal;

import java.io.Serializable;
import java.util.ArrayList;

public class ElevatorStateUpdate implements Serializable {
    private final int elevatorId;
    private final int curFloor;
    private int destinationFloor;
    private Direction direction;
    private ArrayList<WorkAssignment> workAssignments;
    private Signal stateSignal;

    /**
     * Creates an elevator state update
     * @param elevatorId The ID of the elevator
     * @param floor The floor it is currently at
     * @param direction The direction it is traveling
     * @param workAssignments It's work assignments
     */
    public ElevatorStateUpdate(int elevatorId, int floor, Direction direction, ArrayList<WorkAssignment> workAssignments)  {
        this.elevatorId = elevatorId;
        this.curFloor = floor;
        this.direction = direction;
        this.workAssignments = workAssignments;
    }

    /**
     * Gets the current floor of the elevator
     * @return The number for the current floor
     */
    public int getFloor() {
        return curFloor;
    }

    /**
     * Gets the work assignments for a specific elevator
     * @return The list of work assignments
     */
    public ArrayList<WorkAssignment> getWorkAssignments() {
        return workAssignments;
    }

    /**
     * Gets the state signal for the update
     * @return The signal of the elevator's current state
     */
    public Signal getStateSignal() {
        return stateSignal;
    }

    /**
     * Sets the state of the current update
     * @param signal The signal representing the most recent state
     */
    public void setStateSignal(Signal signal) {
        stateSignal = signal;
    }

    /**
     * Sets the current destination floor of the elevator
     * @param destinationFloor The number representing the elevator's current target
     */
    public void setDestinationFloor(int destinationFloor) {
        this.destinationFloor = destinationFloor;
    }

    /**
     * Getsthe current destination of the elevator
     * @return The number representing the elevator's current target
     */
    public int getDestinationFloor() {
        return destinationFloor;
    }

    /**
     * Sets the direction the elevator is moving in
     * @param direction The direction of the elevator
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    /**
     * Sets the current works assignments of the elevator
     * @param workAssignments The
     */
    public void setWorkAssignments(ArrayList<WorkAssignment> workAssignments) {
        this.workAssignments = workAssignments;
    }

    /**
     * Gets the current direction that the elevator is moving in
     * @return The direction of the elevator
     */
    public Direction getDirection() {
        return direction;
    }
}
