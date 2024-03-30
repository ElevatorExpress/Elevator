package util;

import util.Messages.Signal;

import java.io.Serializable;
import java.util.ArrayList;

public class ElevatorStateUpdate implements Serializable {
    private int elevatorId;
    private int curFloor;
    private int destinationFloor;
    private Direction direction;
    private ArrayList<WorkAssignment> workAssignments;
    private Signal stateSignal;

    /**
     * Creates an elevator state update
     * @param elevatorId The ID of the elevator
     * @param floor The floor it is currently at
     * @param direction The direction it is traveling
     * @param workAssignments It's work assignmetns
     */
    public ElevatorStateUpdate(int elevatorId, int floor, Direction direction, ArrayList<WorkAssignment> workAssignments)  {
        this.elevatorId = elevatorId;
        this.curFloor = floor;
        this.direction = direction;
        this.workAssignments = workAssignments;
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public int getFloor() {
        return curFloor;
    }

    public ArrayList<WorkAssignment> getWorkAssignments() {
        return workAssignments;
    }

    public Signal getStateSignal() {
        return stateSignal;
    }

    public void setStateSignal(Signal signal) {
        stateSignal = signal;
    }

    public void setDestinationFloor(int destinationFloor) {
        this.destinationFloor = destinationFloor;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setWorkAssignments(ArrayList<WorkAssignment> workAssignments) {
        this.workAssignments = workAssignments;
    }

    public Direction getDirection() {
        return direction;
    }
}
