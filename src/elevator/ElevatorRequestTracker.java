package elevator;

import util.Direction;
import util.Messages.MessageTypes;
import util.Messages.SerializableMessage;
import util.Messages.Signal;
import util.WorkAssignment;

/**
 * Data class for a elevator request
 */
public class ElevatorRequestTracker {

    /**
     * The status of the request
     */
    public enum RequestStatus {
        UNSERVICED, PICKING, DROPPING, DONE
    }
    private final int sourceFloor;
    private final int destFloor;
    private RequestStatus status;
    private final WorkAssignment request;
    private final Direction direction;

    /**
     * Creates an Elevator Request Tracker
     * @param status The current request status
     * @param request The request message
     */
    ElevatorRequestTracker(RequestStatus status, WorkAssignment request){
        this.sourceFloor = request.getServiceFloor();
        this.destFloor = request.getDestinationFloor();
        this.direction = request.getDirection();
        this.status = status;
        this.request = request;
    }

    /**
     * @return The destination floor
     */
    public int getDestFloor() {
        return destFloor;
    }

    /**
     * @return The source floor
     */
    public int getSourceFloor() {
        return sourceFloor;
    }

    /**
     * @return The status
     */
    public RequestStatus getStatus() {
        return status;
    }

    /**
     * @return The original request
     */
    public WorkAssignment getRequest() {
        return request;
    }

    /**
     * @return The direction of the request
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * @return The target floor
     */
    public int getFloorByStatus() {
        if (status == RequestStatus.PICKING) {
            return sourceFloor;
        }
        return destFloor;
    }

    /**
     * Sets the request status
     *
     */
    public void setStatus() {
        switch (status) {
            case UNSERVICED -> {
                setStatus(RequestStatus.PICKING);
            }
        }
    }

    public boolean equals(WorkAssignment obj) {
        if (!(sourceFloor == obj.getServiceFloor())) return false;
        if (!(destFloor == obj.getDestinationFloor())) return false;
        return direction == obj.getDirection();
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }
    public Signal getSignal() {
        return request.getSignal();
    }
}
