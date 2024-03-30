package elevator;

import util.*;
import util.Messages.MessageTypes;
import util.Messages.SerializableMessage;
import util.Messages.Signal;
import util.states.ElevatorIdle;
import util.states.ElevatorState;
import util.states.ElevatorWorking;
import elevator.ElevatorRequestTracker.*;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

import static java.lang.Math.abs;
import static java.lang.Math.log;

/**
 * Class ElevatorSubsystem creates a subsystem thread for an elevator. The class will process requests sent by the scheduler
 * and go to the requested floors to pick up passengers. Once the passengers have been picked up, the elevator will deliver
 * passengers to the destination floor
 *
 * @author Yasir Sheikh
 */
public class ElevatorSubsystem extends Thread {

    private Integer currentFloor = 1;
    private final ElevatorButtonPanel buttons;
    private final int elevatorId ;
    private ElevatorState currentState;
    private final ElevatorLogger logger;
    private volatile ArrayList<ElevatorRequestTracker> trackRequest = new ArrayList<>();
    private ArrayList<WorkAssignment> wa = new ArrayList<>();
    private Direction universalDirection;
    private String msgID;
    private ElevatorStateUpdate elevatorInfo;
    private final int MAX_LEVEL = 22;
    private ElevatorControlSystem  ecs;


    /**
     * Creates an elevator
     *
     * @param elevatorId The elevator ID number
     */
    public ElevatorSubsystem(int elevatorId, ElevatorControlSystem ecs) {
        this.elevatorId = elevatorId;
        logger = new ElevatorLogger("Elevator-" + elevatorId, "\u001B[3"+ elevatorId +"m");
        currentState = null;
        buttons = new ElevatorButtonPanel(MAX_LEVEL);
        universalDirection = Direction.ANY;
        this.ecs = ecs;
    }

    private void incrementFloor() {
        //find if current floor and direction matches a request
        ArrayList<ElevatorRequestTracker> dummyTrackRequest = new ArrayList<>(trackRequest);
        for (ElevatorRequestTracker ert : dummyTrackRequest) {
            if (ert.getDirection() == universalDirection) {
                if (ert.getSourceFloor() == currentFloor && ert.getStatus() == RequestStatus.PICKING) {
                    wa.forEach(workAssignment -> {if (ert.getRequest() == workAssignment) {workAssignment.setPickupComplete();}});
                    elevatorInfo = new ElevatorStateUpdate(elevatorId, currentFloor, universalDirection, wa);
                    logger.info("Arrived at floor " + ert.getSourceFloor() + " to pick up passengers");
                    ert.setStatus(RequestStatus.DROPPING);
                    buttons.turnOnButton(ert.getDestFloor());
                }
                else if (ert.getDestFloor() == currentFloor && ert.getStatus() == RequestStatus.DROPPING) {
                    logger.info("Dropping passengers to floor " + ert.getDestFloor() + " from floor: " + ert.getSourceFloor());
                    ert.setStatus(RequestStatus.DONE);
                    wa.forEach(workAssignment -> {if (ert.getRequest() == workAssignment) {workAssignment.setDropoffComplete();}});
                    elevatorInfo = new ElevatorStateUpdate(elevatorId, currentFloor, universalDirection, new ArrayList<>(wa));
                    elevatorInfo.setStateSignal(Signal.DONE);
                    trackRequest.remove(ert);
                }
            }
        }

        if (currentFloor == MAX_LEVEL && universalDirection == Direction.UP) universalDirection = Direction.DOWN;
        else if (currentFloor == 0 && universalDirection == Direction.DOWN) universalDirection = Direction.UP;

        travelDelay(); // go up one floor
    }

    /**
     * Sends this elevator to a floor
     * @param ert The elevator request
     */
//    private void goToFloor(ElevatorRequestTracker ert) {
//        int floor = ert.getFloorByStatus();
//        Direction direction = getDirection(floor);
//
//
//        if (ert.getStatus() == RequestStatus.PICKING) {
//            if (currentFloor != floor) {
//                logger.info( ert.getRequest().getAssignmentTimeStamp() + ": Going " + direction + " to floor: " + floor);
//                travelDelay(floor, direction);
////                for (ElevatorRequestTracker newErt : trackRequest) {
////                    if (newErt.getStatus() == RequestStatus.UNSERVICED) return;
////                }
//                logger.info("Arrived at floor " + floor + " to pick up passengers");
//            } else {
//                logger.info(ert.getRequest().getAssignmentTimeStamp() + " Picking up passengers from floor: " + floor);
//            }
//            buttons.turnOnButton(ert.getDestFloor());
//            ert.setStatus(RequestStatus.DROPPING);
//            wa.forEach(workAssignment -> {if (ert.getRequest() == workAssignment) {workAssignment.setPickupComplete();}});
//        } else {
//            if (currentFloor != floor) {
//                logger.info("Going " + direction + " to floor: " + floor);
//                travelDelay(floor, direction);
//                for (ElevatorRequestTracker newErt : trackRequest) {
//                    if (newErt.getStatus() == RequestStatus.UNSERVICED) return;
//                }
//                logger.info(  "Arrived at floor " + floor + " to drop passengers from floor: " + ert.getSourceFloor());
//            } else {
//                logger.info("Dropping passengers to floor " + floor + " from floor: " + ert.getSourceFloor());
//            }
//            buttons.turnOffButton(floor);
//            ert.setStatus(RequestStatus.DONE);
//            wa.forEach(workAssignment -> {if (ert.getRequest() == workAssignment) {workAssignment.setDropoffComplete();}});
//        }
//    }

    /**
     * Simulates the delay an elevator would need to reach a specific floor
     *
     */
    private void travelDelay() {
        try {
            Thread.sleep((long) (1000 * ((4L / 2.53))));
            if (universalDirection == Direction.UP) {
                    currentFloor++;
                } else {
                    currentFloor--;
                }
                sendMessage();

//            if (abs(floor - currentFloor) == 1) {
//                Thread.sleep((long) (6140 + (1000 * 12.58))); // do we care for sleep time?
//            } else {
//                Thread.sleep((long) (1000 * ((4L / 2.53))));
//                if (direction == Direction.UP) {
//                    currentFloor++;
//                } else {
//                    currentFloor--;
//                }
//            }
        } catch (InterruptedException ie) {
            System.exit(1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the direction of elevator movement based on arrivalFloor and currentFloor
     *
     * @param arrivalFloor The floor the elevator is moving to
     * @return A String for the determined direction
     */
    private Direction getDirection(int arrivalFloor) {
        if (arrivalFloor - currentFloor > 0) {return Direction.UP;}
        return Direction.DOWN;
    }

    /**
     * Determines if the lowest or highest floor is desired based on direction.
     *
     * @param minMax The current min or max value representing the lowest or highest floor
     * @param floor The floor used for comparison
     * @return A boolean representing if the floor is lower or higher than the current min or max
     */
    public boolean handleRequestDirection(int minMax, int floor) {
        if (universalDirection == Direction.UP) {
            if (floor > currentFloor){
                return (minMax == 0 || minMax > floor);
            }
        }
        if (floor < currentFloor) {
            return (minMax == 0 ||  floor > minMax);
        }
        return false;
    }

    /**
     * Gets the next request to service
     * @return The next request
     */
    public ElevatorRequestTracker serviceNextRequest() {
        int minMax = 0;
        ElevatorRequestTracker nextRequest = null;
        ArrayList<ElevatorRequestTracker> dummyRequestList = new ArrayList<>(trackRequest);
        for (ElevatorRequestTracker ert : dummyRequestList) {
            switch (ert.getStatus()) {
                case UNSERVICED, PICKING -> {
                    if (handleRequestDirection(minMax, ert.getSourceFloor())) {
                        minMax = ert.getSourceFloor();
                        nextRequest = ert;

                    }
                }
                case DROPPING-> {
                    if (handleRequestDirection(minMax, ert.getDestFloor())){
                        minMax = ert.getDestFloor();
                        nextRequest = ert;
                    }
                }
            }
        }

        assert nextRequest != null;
        nextRequest.setStatus();
        return nextRequest;
    }

    /**
     * Sends a message to the scheduler
     *
     */
    public synchronized void sendMessage() throws IOException, InterruptedException {
       ecs.updateScheduler();
    }


    protected void addTrackedRequest(ConcurrentLinkedDeque<WorkAssignment> newRequests) {
        for (WorkAssignment request : newRequests) {
            trackRequest.add(new ElevatorRequestTracker(RequestStatus.UNSERVICED, request));
        }
        if (universalDirection == Direction.ANY) universalDirection = trackRequest.get(0).getDirection();
    }

    protected void addTrackedRequest(WorkAssignment newRequest) {
        wa.add(newRequest);
        if (universalDirection == Direction.ANY) universalDirection = newRequest.getDirection();
        logger.info("Elevator received request from scheduler. Signal: " + newRequest.getSignal() + ", Request: " + newRequest);
        synchronized (trackRequest) {
            trackRequest.add(new ElevatorRequestTracker(RequestStatus.PICKING, newRequest));
        }
    }

    /**
     * Attempts to get a request
     */
    public void receiveMessage() {
        while (trackRequest.isEmpty()) {}
    }


    /**
     * Triggers a complete request event
     */
    public void completeRequestEvent(){
        currentState = currentState.handleCompleteRequest();
    }

    /**
     * Triggers a receive request event
     */
    public void receiveRequestEvent(){
        currentState = currentState.handleReceiveRequest();
    }
    public ElevatorStateUpdate getElevatorInfo() {
        return elevatorInfo;
    }

    public int getElevatorId() {
        return elevatorId;
    }


    /**
     * Run
     */
    @Override
    public void run(){
        currentState = ElevatorState.start(this);

        while (true) {
            if (currentState instanceof ElevatorIdle) {
                elevatorInfo = new ElevatorStateUpdate(elevatorId, currentFloor,Direction.ANY, null);
                elevatorInfo.setStateSignal(Signal.IDLE);
                logger.info("Elevator is idle");
                receiveMessage();
                receiveRequestEvent();
            } else if (currentState instanceof ElevatorWorking) {
                elevatorInfo = new ElevatorStateUpdate(elevatorId, currentFloor, universalDirection, wa);
                elevatorInfo.setStateSignal(Signal.WORKING);
                while (!trackRequest.isEmpty()) {
                    incrementFloor();
                }
                completeRequestEvent();
            }
        }
    }



}
