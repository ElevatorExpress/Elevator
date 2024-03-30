package util;

import util.Messages.Signal;

import java.io.Serializable;

/**
 * Class for assigning request to elevators
 * @author Connor Beleznay
 */
public class WorkAssignment implements Serializable {
    //Pick up floor
    private final int serviceFloor;
    //Drop off floor
    private final int destinationFloor;
    private final String assignmentTimeStamp;
    private final String floorRequestId;
    private Signal signal;
    //Have passengers been picked up
    public boolean pickupComplete = false;
    //Have passengers been dropped off
    public boolean dropoffComplete = false;
    //Request direction
    private final Direction direction;
    private final String senderAddr;
    private final int senderPort;
    //Is there an error in the work order
    private final int errorBit;

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
        this.floorRequestId = floorRequestId;
        this.senderAddr = senderAddr;
        this.senderPort = senderPort;
        this.signal = signal;
        this.errorBit = errorBit;
    }

    /**
     * Gets the time of the request
     * @return The string representing the time of the request
     */
    public String getAssignmentTimeStamp() {
        return assignmentTimeStamp;
    }
    /**
     * Get the floor where the request is made
     * @return The number representing the floor
     */
    public int getServiceFloor() {
        return serviceFloor;
    }

    /**
     * Get the address for the sender of the request
     * @return The name of the host that sent the request
     */
    public String getSenderAddr() {
        return senderAddr;
    }


    /**
     * Get the port for sender of the request
     * @return The port used for communicating the request
     */
    public int getSenderPort() {
        return senderPort;
    }

    /**
     * Get the request id of the sender
     * @return The id of the request
     */
    public String getFloorRequestId() {
        return floorRequestId;
    }

    /**
     * Get the destination floor of the request
     * @return The number representing the destination floor
     */
    public int getDestinationFloor() {
        return destinationFloor;
    }

    /**
     * Get the direction of the request
     * @return The direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Get the error bit used for injecting faults
     * @return The bit representing hard or soft faults
     */
    public int getErrorBit() {
        return errorBit;
    }

    /**
     * Set the completion of picking up passengers
     */
    public void setPickupComplete() {
        pickupComplete = true;
    }

    /**
     * Set the completion of the dropping of passengers
     */
    public void setDropoffComplete() {
        dropoffComplete = true;
    }

    public boolean isPickupComplete() {
        return pickupComplete;
    }

    /**
     * Checks if a request drop off is completed
     * @return boolean showing the completion of drop off
     */
    public boolean isDropoffComplete() {
        return dropoffComplete;
    }

    /**
     * Converts this Work Assignment to a String
     * @return the string representation
     */
    public String toString() {
        return "[" + assignmentTimeStamp + "ms, Service Floor: " + serviceFloor + ", Destination Floor: " + destinationFloor + ", Direction: " + direction + "]";
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
        return serviceFloor == that.serviceFloor &&
                destinationFloor == that.destinationFloor &&
                assignmentTimeStamp.equals(that.assignmentTimeStamp) &&
                direction == that.direction;
    }
}
