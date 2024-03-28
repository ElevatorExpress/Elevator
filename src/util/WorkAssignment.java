package util;

public class WorkAssignment {
    private int serviceFloor;
    private int destinationFloor;

    private String assignmentTimeStamp; //comes in with the

    private String assignmentId;

    private String floorRequestId;

    public boolean pickupComplete = false;
    public boolean dropoffComplete = false;


    private Direction direction;




    public WorkAssignment(int serviceFloor, int destinationFloor, String assignmentTimeStamp, Direction direction, String floorRequestId) {
        this.serviceFloor = serviceFloor;
        this.destinationFloor = destinationFloor;
        this.assignmentTimeStamp = assignmentTimeStamp;
        this.direction = direction;
        this.assignmentId = assignmentTimeStamp + serviceFloor + destinationFloor;
        this.floorRequestId = floorRequestId;
    }

    public int getServiceFloor() {
        return serviceFloor;
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
        return "Service Floor: " + serviceFloor + " Destination Floor: " + destinationFloor + " Direction: " + direction;
    }





}
