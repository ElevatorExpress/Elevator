package elevator;

import elevator.ElevatorRequestTracker.RequestStatus;
import gui.ElevatorListener;
import util.Direction;
import util.ElevatorLogger;
import util.ElevatorStateUpdate;
import util.Messages.Signal;
import util.WorkAssignment;
import util.states.ElevatorIdle;
import util.states.ElevatorState;
import util.states.ElevatorWorking;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Class ElevatorSubsystem creates a subsystem thread for an elevator. The class will process requests sent by the scheduler
 * and go to the requested floors to pick up passengers. Once the passengers have been picked up, the elevator will deliver
 * passengers to the destination floor
 *
 * @author Yasir Sheikh
 */
public class ElevatorSubsystem extends Thread {
    private final ArrayList<ElevatorListener> elevatorListeners;
    //Elevators start at the bottom floor
    private Integer currentFloor = 1;
    private final ElevatorButtonPanel buttons;
    private final int elevatorId;
    private ElevatorState currentState;
    //Used to output information
    private final ElevatorLogger logger;
    private volatile ArrayList<ElevatorRequestTracker> trackRequest;
    //List of assignments for the elevator
    private final ArrayList<WorkAssignment> allWorkAssignments;
    //Current direction of the elevator
    private Direction universalDirection;
    private ElevatorStateUpdate elevatorInfo;
    //Our simulated building has 22 floors
    private final int MAX_LEVEL = 22;
    private int capacity = 0;
    private final ElevatorControlSystem ecs;
    private boolean stopBit = false;
    private final int MAX_PEOPLE = 5;
    private final int FLOOR_TRAVEL_TIME = 10000;
    private final int FLOOR_TRAVEL_FAULT_TIME = 20000;
    private final int OPEN_CLOSE_DOORS = 3000;
    private final int OPEN_CLOSE_DOORS_FAULT = 5000;
    private final int PASSENGER_DELAY = 5000;



    /**
     * Creates an elevator
     * @param elevatorId The elevator ID number
     * @param ecs the ElevatorControlSystem that manages the elevator
     */
    public ElevatorSubsystem(int elevatorId, ElevatorControlSystem ecs) {
        this.elevatorId = elevatorId;
        logger = new ElevatorLogger("Elevator-" + elevatorId, "\u001B[3"+ ( 3 + elevatorId) +"m");
        currentState = null;
        buttons = new ElevatorButtonPanel(MAX_LEVEL);
        //Direction is not picked yet
        universalDirection = Direction.ANY;
        trackRequest = new ArrayList<>();
        allWorkAssignments = new ArrayList<>();
        elevatorListeners = new ArrayList<>();
        this.ecs = ecs;
    }

    /**
     * Changes floors
     */
    private void incrementFloor() {
        boolean majorDelay = false;
        boolean stateUpdated = false;
        boolean doorsOpen = false;
        int error = 0;
        ArrayList<ElevatorRequestTracker> dummyTrackRequest = new ArrayList<>(trackRequest);
        for (ElevatorRequestTracker ert : dummyTrackRequest) {
            //If the current request matches the direction of the elevator
            if (!isFull() && ert.getDirection() == universalDirection) {
                //If the elevator is at the floor it is expecting to pick up passengers
                if (ert.getSourceFloor() == currentFloor && ert.getStatus() == RequestStatus.PICKING) {
                    int errorBit = ert.getRequest().getErrorBit();
                    //If an error was injected
                    if (errorBit == 2) {
                        majorDelay = true;
                        //Remove the flagged request from the list
                        allWorkAssignments.remove(ert.getRequest());
                    } else if (errorBit == 1 && doorsOpen) {
                        error = errorBit;
                    }

                    notifyMovingSubscribers(ElevatorListener.Moving.STOPPED);
                    //Simulates the doors opening at a floor
                    if(!doorsOpen) {
                        verifyDoorDelay(errorBit);
                        doorsOpen = true;
                    }
                    //Simulate passenger exiting elevator
                    try {
                        Thread.sleep(PASSENGER_DELAY);
                    } catch (Exception ignored) {}
                    capacity++;
                    notifyUpdateCapacity();

                    //If there are no errors signal that the passengers were picked up
                    allWorkAssignments.forEach(workAssignment -> {
                        if (ert.getRequest() == workAssignment) {
                            workAssignment.setPickupComplete();
                            workAssignment.setSignal(Signal.WORKING);
                        }
                    });

                    //Send state update
                    setElevatorInfo(new ElevatorStateUpdate(elevatorId, currentFloor, universalDirection, allWorkAssignments, isFull()));
                    setElevatorInfoSignal(Signal.WORKING);
                    //Output what happened
                    logger.info("Picking up passengers from: " + ert.getSourceFloor() + ". Destination: " + ert.getDestFloor());
                    ert.setStatus(RequestStatus.DROPPING);
                    buttons.turnOnButton(ert.getDestFloor());
                    stateUpdated = true;
                }
                //If the elevator is at the floor it is expecting to drop off passengers
                else if (ert.getDestFloor() == currentFloor && ert.getStatus() == RequestStatus.DROPPING) {
                    //Check error bit of the request
                    int errorBit = ert.getRequest().getErrorBit();

                    notifyMovingSubscribers(ElevatorListener.Moving.STOPPED);
                    if (errorBit == 1 && doorsOpen) {
                        error = errorBit;
                    }

                    //Simulate opening the doors
                    if(!doorsOpen) {
                        verifyDoorDelay(errorBit);
                        doorsOpen = true;
                    }
                    //Simulate passenger exiting elevator
                    try {
                        Thread.sleep(PASSENGER_DELAY);
                    } catch (Exception ignored) {}
                    capacity--;
                    notifyUpdateCapacity();

                    //Output what happened
                    logger.info("Dropping passengers to floor " + ert.getDestFloor() + " from floor: " + ert.getSourceFloor());
                    //Mark request as complete
                    ert.setStatus(RequestStatus.DONE);
                    allWorkAssignments.forEach(workAssignment -> {
                        if (ert.getRequest() == workAssignment) {
                            workAssignment.setDropoffComplete();
                            workAssignment.setSignal(Signal.DONE);
                        }
                    });
                    //Signal that the elevator is done
                    setElevatorInfo(new ElevatorStateUpdate(elevatorId, currentFloor, universalDirection, allWorkAssignments, isFull()));
                    setElevatorInfoSignal(Signal.DONE);
                    //Remove the completed request
                    trackRequest.remove(ert);
                    stateUpdated = true;
                }
            }
        }
        //Simulates the doors opening at a floor
        if(doorsOpen) {
            verifyDoorDelay(error);
        }
        notifyMovingSubscribers(ElevatorListener.Moving.MOVING);

        //If the elevator reaches the top floor, swap its direction to down
        if (currentFloor == MAX_LEVEL && universalDirection == Direction.UP){
            universalDirection = Direction.DOWN;
            notifyDirectionSubscribers();
        }
        //If the elevator reaches the bottom floor, swap its direction to up
        else if (currentFloor == 0 && universalDirection == Direction.DOWN){
            universalDirection = Direction.UP;
            notifyDirectionSubscribers();
        }

        // Delay for travel time between each floor. Catches hard faults
        try {
            boolean finalMajorDelay = majorDelay;
            //Sets a timer depending on if there is an error
            Thread travelDelayThread = new Thread(() -> this.travelDelay(finalMajorDelay));
            //Start the timer
            travelDelayThread.start();
            //If the timer is running for more than 5s a hard fault occurred
            travelDelayThread.join(FLOOR_TRAVEL_FAULT_TIME);
            if (travelDelayThread.isAlive()){
                //Output what happened
                logger.info("HARD FAULT has occurred, elevator took too long to reach destination");
                //Signal elevator had a fault
                setElevatorInfo(new ElevatorStateUpdate(elevatorId, currentFloor, universalDirection, allWorkAssignments, isFull()));
                setElevatorInfoSignal(Signal.EMERG);
                notifyMovingSubscribers(ElevatorListener.Moving.EMERG);
                ecs.emergencyState(elevatorId, allWorkAssignments);
                //Clear out the requests of this elevator
                trackRequest.clear();
            } else if (!stateUpdated) {
                setElevatorInfo(new ElevatorStateUpdate(elevatorId, currentFloor, universalDirection, allWorkAssignments, isFull()));
                setElevatorInfoSignal(getElevatorInfo().getStateSignal());
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Simulates the delay on opening the doors of an elevator
     * @param errorBit 0 1 or 2 depending on which error is injected
     */
    private void verifyDoorDelay(int errorBit) {
        try {
            boolean completed = false;
            while (!completed) {
                int finalErrorBit = errorBit;
                //Changes the timer based on if there is an error injected
                Thread doorDelayThread = new Thread(() -> this.doorDelay(finalErrorBit == 1));
                //Start the timer and wait the delay
                doorDelayThread.start();
                doorDelayThread.join(OPEN_CLOSE_DOORS_FAULT);
                //If the thread is still active after the delay an error has occurred
                if (doorDelayThread.isAlive()) {
                    //Output what happened
                    logger.info("SOFT FAULT has occurred, retrying doors");
                    notifyMovingSubscribers(ElevatorListener.Moving.RETRY_DOOR);
                    //The doors will close on the next try
                    if (errorBit == 1) errorBit = 0;
                }
                //If the thread completes, the doors closed correctly
                else {
                    completed = true;
                }
            }
        } catch (InterruptedException ignored) {}
    }

    /**
     * Simulates the delay an elevator would need to travel to a floor
     */
    private void travelDelay(boolean hasError) {
        try {
            //Normal delay of each floor
            if (!hasError) { Thread.sleep(FLOOR_TRAVEL_TIME); }
            //If there is an error being injected take longer than the allowed time
            else { Thread.sleep((long) (FLOOR_TRAVEL_FAULT_TIME * 1.5)); }

            //If the elevator is going up
            if (universalDirection == Direction.UP) {
                //Increment floor
                currentFloor++;
            } else {
                //Decrement floor
                currentFloor--;
            }
            notifyFloorSubscribers();

        }
        catch (InterruptedException ignored) {}
    }

    /**
     * Simulates the delay of a door opening or closing
     * @param hasError if an error is getting injected
     */
    private void doorDelay(boolean hasError) {
        try {
            //If there is no error the doors will open for 3s
            if (!hasError) { Thread.sleep(OPEN_CLOSE_DOORS); }
            //If there is an error delay extra time
            else { Thread.sleep((long) (OPEN_CLOSE_DOORS_FAULT * 1.5)); }
        } catch (InterruptedException ignored){}
    }

    /**
     * Checks if the elevator is full
     * @return true if the elevator is full, false if not
     */
    private boolean isFull() {
        return this.capacity >= MAX_PEOPLE;
    }

    /**
     * Gets requests from the Scheduler and tracks them
     * @param newRequest the request that is going to be tracked
     */
    protected void addTrackedRequest(WorkAssignment newRequest) {
        allWorkAssignments.add(newRequest);
        //Sets the direction of the elevator if it does not have one
        if (universalDirection == Direction.ANY) universalDirection = newRequest.getDirection();
        //Grabs the lock for the trackRequest and add the request to it
        if (newRequest.getSignal() == Signal.WORK_REQ) {
            trackRequest.add(new ElevatorRequestTracker(RequestStatus.PICKING, newRequest));
        }
        //Output what happened
        logger.info("Elevator received request from scheduler. Signal: " + newRequest.getSignal() + ", Request: " + newRequest + ", Now Servicing " + trackRequest.size() + " request(s)");
    }

    /**
     * Attempts to get a request
     */
    public void receiveMessage() {
        while (trackRequest.isEmpty() && !stopBit) {}
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
    public synchronized ElevatorStateUpdate getElevatorInfo() { return elevatorInfo; }
    public synchronized void setElevatorInfo(ElevatorStateUpdate info) { elevatorInfo = info; }
    public synchronized void setElevatorInfoSignal(Signal s) {elevatorInfo.setStateSignal(s);}

    /**
     * Gets the elevatorId of this elevator
     * @return this elevatorId
     */
    public int getElevatorId() {
        return elevatorId;
    }

    /**
     * Stop bit to force stop elevator system
     */
    public void setStopBit(boolean stopBit) {
        this.stopBit = stopBit;
    }

    /**
     * Run
     */
    @Override
    public void run(){
        //Start the elevator
        currentState = ElevatorState.start(this);

        //Run forever
        while (!stopBit) {
            //If the elevator is idle
            if (currentState instanceof ElevatorIdle) {
                //Make new update event with corresponding values
                setElevatorInfo(new ElevatorStateUpdate(elevatorId, currentFloor,Direction.ANY, allWorkAssignments, isFull()));
                //Set elevator state
                setElevatorInfoSignal(Signal.IDLE);
                logger.info("Elevator is idle");
                //Receive requests from the scheduler
                receiveMessage();
                receiveRequestEvent();
            } else if (currentState instanceof ElevatorWorking) {
                //Make new update event with corresponding values
                setElevatorInfo(new ElevatorStateUpdate(elevatorId, currentFloor, universalDirection, allWorkAssignments, isFull()));
                //Set elevator state
                setElevatorInfoSignal(Signal.WORKING);
                notifyDirectionSubscribers();
                //Make the elevator move until there are no requests left
                while (!trackRequest.isEmpty()) {
                    incrementFloor();
                }
                //Signal that requests are all complete
                if (getElevatorInfo().getStateSignal() != Signal.EMERG) {
                    notifyMovingSubscribers(ElevatorListener.Moving.STOPPED);
                }
                completeRequestEvent();
            }
        }
    }

    /**
     * Notifies for Floor Event
     */
    public void notifyFloorSubscribers(){
        elevatorListeners.forEach(
                elevatorListener -> elevatorListener.updateCurrentFloor(this.elevatorId, this.currentFloor)
        );
    }

    /**
     * Notifies for Moving Event
     */
    public void notifyMovingSubscribers(ElevatorListener.Moving moving){
        elevatorListeners.forEach(
                elevatorListener -> elevatorListener.updateMovingState(this.elevatorId, moving)
        );
    }

    /**
     * Notifies for Direction Event
     */
    public void notifyDirectionSubscribers(){
        elevatorListeners.forEach(
                elevatorListener -> elevatorListener.updateDirection(this.elevatorId, this.universalDirection)
        );
    }

    /**
     * Notifies for Capacity Event
     */
    public void notifyUpdateCapacity(){
        elevatorListeners.forEach(
                elevatorListener -> elevatorListener.updateCapacity(this.elevatorId, capacity)
        );
    }

    /**
     * Subscribes an elevator listener to this elevator
     * @param elevatorListener The subscriber to this event
     */
    public void subscribe(ElevatorListener elevatorListener) {
        elevatorListeners.add(elevatorListener);
    }
}
