package elevator;

import util.Messages.SerializableMessage;

/**
 * Data class for a elevator request
 */
public class ElevatorRequestTracker {
    /**
     * The status of the request
     */
    public enum RequestStatus {
        UNSERVICED, SERVICING, DONE
    }
    private final int sourceFloor;
    private final int destFloor;
    private RequestStatus status;
    private final SerializableMessage request;
    private final String direction;

    /**
     * Creates an Elevator Request Tracker
     * @param status The current request status
     * @param request The request message
     */
    ElevatorRequestTracker(RequestStatus status, SerializableMessage request){
        this.sourceFloor = Integer.parseInt(request.data().serviceFloor());
        this.destFloor = Integer.parseInt(request.data().requestFloor());
        this.direction = request.data().direction();
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
    public SerializableMessage getRequest() {
        return request;
    }

    /**
     * @return The direction of the request
     */
    public String getDirection() {
        return direction;
    }

    /**
     * @return The target floor
     */
    public int getFloorByStatus() {
        if (status == RequestStatus.SERVICING) {
            return sourceFloor;
        }
        return destFloor;
    }

    /**
     * Sets the request status
     * @param status The new status
     */
    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
