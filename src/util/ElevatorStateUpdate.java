package util;

import util.Messages.Signal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Scanner;

public class ElevatorStateUpdate implements Serializable {
    private final int elevatorId;
    private ArrayList<Integer> floorStopQueue = new ArrayList<>();
    private ArrayList<Integer> upFloorStopQueue = new ArrayList<>();
    private ArrayList<Integer> downFloorStopQueue = new ArrayList<>();
    private final int curFloor;
    private int destinationFloor;
    private Direction direction;
    private ArrayList<WorkAssignment> workAssignments;
    private Signal stateSignal;
    private boolean isFull = false;

    /**
     * Creates an elevator state update
     * @param elevatorId The ID of the elevator
     * @param floor The floor it is currently at
     * @param direction The direction it is traveling
     * @param workAssignments It's work assignments
     */
    public ElevatorStateUpdate(int elevatorId, int floor, Direction direction, ArrayList<WorkAssignment> workAssignments, boolean isFull)  {
        this.elevatorId = elevatorId;
        this.curFloor = floor;
        this.direction = direction;
        this.workAssignments = workAssignments;
        this.isFull = isFull;
    }
    public void setFloorUpStopQueue(ArrayList<Integer> upFloorStopQueue) {
        this.upFloorStopQueue = upFloorStopQueue;
    }
    public void setFloorDownStopQueue(ArrayList<Integer> downFloorStopQueue) {
        this.downFloorStopQueue = downFloorStopQueue;
    }
    public ArrayList<Integer> getUpFloorStopQueue() {
        return upFloorStopQueue;
    }
    public ArrayList<Integer> getDownFloorStopQueue() {
        return downFloorStopQueue;
    }

    public ArrayList<Integer> getFloorStopQueue() {
        return floorStopQueue;
    }
    public void setFloorStopQueue(ArrayList<Integer> floorStopQueue) {
        this.floorStopQueue = floorStopQueue;
    }

    public void removeFloorStop(int floor) {
        floorStopQueue.remove(floor);
    }
    public void addFloorStop(int floor) {
        floorStopQueue.add(floor);
    }

    public void clearFloorStopQueue() {
        floorStopQueue.clear();
    }


    public static int getElevatorCount() throws FileNotFoundException {
        File elevatorInfo = new File("./elevators.info");
        Scanner eScanner = new Scanner(elevatorInfo);

        int numElevators = 1;
        if (eScanner.hasNext()){
            String s = eScanner.next();
            numElevators = Integer.parseInt(s);
        }
        return numElevators;
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
     * Gets the current destination of the elevator
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

    public boolean isFull() {
        return isFull;
    }
}
