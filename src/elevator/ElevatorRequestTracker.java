package elevator;

import util.Direction;
import util.Messages.Signal;
import util.WorkAssignment;

/**
 * Data class for a elevator request
 * @author Yasir Sheikh
 */
public class ElevatorRequestTracker {

    /**
     * The status of the request
     */
    public enum RequestStatus {
        UNSERVICED, PICKING, DROPPING, DONE
    }

    //Floor that the elevator is going to pick up people from
    private final int sourceFloor;
    //Floor that the elevator is going to drop people off at
    private final int destFloor;
    //Current stats us the request
    private RequestStatus status;
    //The request being tracked
    private final WorkAssignment request;
    //Direction of request
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
     * Sets the request status
     */
    public void setStatus() {
        if (status == RequestStatus.UNSERVICED) {
            setStatus(RequestStatus.PICKING);
        }
    }

    /**
     * Sets the status of this tracker
     * @param status the status being set
     */
    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    /**
     * Get signal of current request
     * @return the signal
     */
    public Signal getSignal() {
        return request.getSignal();
    }

}
