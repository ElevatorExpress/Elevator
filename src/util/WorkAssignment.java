package util;

import util.Messages.Signal;

import java.io.Serializable;

/**
 * Class for assigning request to elevators
 * @author Connor Beleznay
 */
public class WorkAssignment implements Serializable {
    //Pick up floor
    private int serviceFloor;
    //Drop off floor
    private int destinationFloor;

    private String assignmentTimeStamp;

    private String assignmentId;

    private String floorRequestId;
    private Signal signal;
    //Have passengers been picked up
    public boolean pickupComplete = false;
    //Have passengers been dropped off
    public boolean dropoffComplete = false;
    //Request direction
    private Direction direction;
    private String senderAddr;
    private int senderPort;
    //Is there an error in the work order
    private int errorBit;


    /**
     * Creates a work assignment
     * @param serviceFloor The floor to service
     * @param destinationFloor The floor to reach
     * @param assignmentTimeStamp The time stamp
     * @param direction The direction of the request
     * @param floorRequestId The original request ID
     * @param senderAddr The sender's IP address
     * @param senderPort The sender's port
     * @param signal The signal of the message
     * @param errorBit The test error bit status
     */
    public WorkAssignment(int serviceFloor, int destinationFloor, String assignmentTimeStamp, Direction direction, String floorRequestId, String senderAddr, int senderPort, Signal signal, int errorBit) {
        this.serviceFloor = serviceFloor;
        this.destinationFloor = destinationFloor;
        this.assignmentTimeStamp = assignmentTimeStamp;
        this.direction = direction;
        this.assignmentId = assignmentTimeStamp + serviceFloor + destinationFloor;
        this.floorRequestId = floorRequestId;
        this.senderAddr = senderAddr;
        this.senderPort = senderPort;
        this.signal = signal;
        this.errorBit = errorBit;
    }

    public int getServiceFloor() {
        return serviceFloor;
    }
    public String getSenderAddr() {
        return senderAddr;
    }
    public int getSenderPort() {
        return senderPort;
    }
    public String getFloorRequestId() {
        return floorRequestId;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    public String getAssignmentTimeStamp() {
        return assignmentTimeStamp;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getErrorBit() {
        return errorBit;
    }

    public void setPickupComplete() {
        pickupComplete = true;
    }

    public void setDropoffComplete() {
        dropoffComplete = true;
    }

    public boolean isPickupComplete() {
        return pickupComplete;
    }

    public boolean isDropoffComplete() {
        return dropoffComplete;
    }

    /**
     * Converts this Work Assignment to a String
     * @return the string representation
     */
    public String toString() {
        return assignmentTimeStamp + "ms: Service Floor: " + serviceFloor + " Destination Floor: " + destinationFloor + " Direction: " + direction;
    }

    /**
     * Sets the signal of the Work Assignment
     * @param signal the new signal
     */
    public void setSignal(Signal signal) {
        this.signal = signal;
    }

    /**
     * Gets the signal
     * @return signal
     */
    public Signal getSignal() {
        return signal;
    }

    /**
     * Checks if 2 Work Assignments are equal
     * @param o object being compared to
     * @return if 2 Work Assignments are equal
     */
    @Override
    public boolean equals(Object o) {
        WorkAssignment that = (WorkAssignment) o;
        //2 Work Assignments are equal when their service floor, destination floor, timestamp and direction are the same
        return serviceFloor == that.serviceFloor && destinationFloor == that.destinationFloor && assignmentTimeStamp.equals(that.assignmentTimeStamp) && direction == that.direction;
    }
}
