package util;

import util.Messages.Signal;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

public class WorkAssignment implements Serializable {
    private int serviceFloor;
    private int destinationFloor;

    private String assignmentTimeStamp; //comes in with the

    private String assignmentId;

    private String floorRequestId;
    private Signal signal;
    public boolean pickupComplete = false;
    public boolean dropoffComplete = false;


    private Direction direction;
    private String senderAddr;
    private int senderPort;
    private int errorBit;




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



    public String toString() {
        return assignmentTimeStamp + "ms: Service Floor: " + serviceFloor + " Destination Floor: " + destinationFloor + " Direction: " + direction;
    }

    public void setSignal(Signal signal) {
        this.signal = signal;
    }

    public Signal getSignal() {
        return signal;
    }

    @Override
    public boolean equals(Object o) {
        WorkAssignment that = (WorkAssignment) o;
        return serviceFloor == that.serviceFloor && destinationFloor == that.destinationFloor && assignmentTimeStamp.equals(that.assignmentTimeStamp) && direction == that.direction;
    }
}
