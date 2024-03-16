package elevator;

import util.Messages.SerializableMessage;

public class ElevatorRequestTracker {

    public enum RequestStatus {
        UNSERVICED, SERVICING, DONE
    }
    private final int sourceFloor;
    private final int destFloor;
    private RequestStatus status;
    private final SerializableMessage request;
    private String direction;

    ElevatorRequestTracker(RequestStatus status, SerializableMessage request){
        this.sourceFloor = Integer.parseInt(request.data().serviceFloor());
        this.destFloor = Integer.parseInt(request.data().requestFloor());
        this.direction = request.data().direction();
        this.status = status;
        this.request = request;

    }

    public int getDestFloor() {
        return destFloor;
    }

    public int getSourceFloor() {
        return sourceFloor;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public SerializableMessage getRequest() {
        return request;
    }

    public String getDirection() {
        return direction;
    }

    public int getFloorByStatus() {
        if (status.equals("Servicing")) {
            return sourceFloor;
        }
        return destFloor;
    }
    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
