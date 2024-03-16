package elevator;

import util.Messages.MessageTypes;
import util.Messages.SerializableMessage;
import util.Messages.Signal;
import util.ElevatorLogger;
import util.MessageBuffer;
import util.states.ElevatorIdle;
import util.states.ElevatorState;
import util.states.ElevatorWorking;
import elevator.ElevatorRequestTracker.*;

import java.net.*;
import java.util.*;

import static java.lang.Math.abs;

/**
 * Class ElevatorSubsystem creates a subsystem thread for an elevator. The class will process requests sent by the scheduler
 * and go to the requested floors to pick up passengers. Once the passengers have been picked up, the elevator will deliver
 * passengers to the destination floor
 *
 * @author Yasir Sheikh
 */
public class ElevatorSubsystem implements Runnable {

    private Integer currentFloor = 1;
    private final MessageBuffer queue;
    private final ElevatorButtonPanel buttons;
    private final int elevatorId ;
    private ElevatorState currentState;
    private final ElevatorLogger logger;
    private final List<ElevatorRequestTracker> trackRequest = new ArrayList<>();
    private String msgID;


    /**
     * Creates an elevator
     * @param queue The messageBuffer that is used by this system
     * @param elevatorId The elevator ID number
     */
    public ElevatorSubsystem(MessageBuffer queue, int elevatorId) {
        this.queue = queue;
        this.elevatorId = elevatorId;
        logger = new ElevatorLogger("Elevator-" + elevatorId);
        currentState = null;
        buttons = new ElevatorButtonPanel(22);
    }

    /**
     * Sends this elevator to a floor
     * @param ert The elevator request
     */
    private void goToFloor(ElevatorRequestTracker ert) {
        int floor = ert.getFloorByStatus();
        String direction = getDirection(floor);

        if (ert.getStatus() == RequestStatus.SERVICING) {
            if (currentFloor != floor) {
                logger.info(ert.getRequest().data().time() + ": Going " + direction + " to floor: " + floor);
                travelDelay(floor);
                logger.info("Arrived at floor " + floor + " to pick up passengers");
            } else {
                logger.info(ert.getRequest().data().time() + " Picking up passengers from floor: " + floor );
            }

            buttons.turnOnButton( ert.getDestFloor());
        } else {
            if (currentFloor != floor) {
                logger.info("Going " + direction + " to floor: " + floor);
                travelDelay(floor);
                logger.info("Arrived at floor " + floor + " to drop passengers from floor: " + ert.getSourceFloor());
            } else {
                logger.info("Dropping passengers to floor " + floor + " from floor: " + ert.getSourceFloor());
            }

            buttons.turnOffButton(floor);
            trackRequest.remove(ert);
        }
        currentFloor = floor;
    }

    /**
     * Simulates the delay an elevator would need to reach a specific floor
     * @param floor The floor to go to
     */
    private void travelDelay(Integer floor) {
        try {
            if (abs(floor - currentFloor) == 1) {
                Thread.sleep((long) (6140 + (1000 * 12.58)));
            } else {
                Thread.sleep((long) (1000 * ((abs(floor - currentFloor) * 4L / 2.53) + 12.58)));
            }
        } catch (InterruptedException ie) {
            System.exit(1);
        }
    }

    /**
     * Gets the direction of elevator movement based on arrivalFloor and currentFloor
     *
     * @param arrivalFloor The floor the elevator is moving to
     * @return A String for the determined direction
     */
    private String getDirection(int arrivalFloor) {
        if (arrivalFloor - currentFloor > 0) {return "up";}
        return "down";
    }

    public boolean handleRequestDirection(int minMax, int floor, String direction) {
        if (direction.equals("up")) {
            return (minMax == 0 || minMax > floor);
        }
        return (minMax == 0 ||  floor > minMax);
    }

    /**
     * Gets the next request to service
     * @return The next request
     */
    public ElevatorRequestTracker serviceNextRequest() {
        int minMax = 0;
        ElevatorRequestTracker nextRequest = null;
        for (ElevatorRequestTracker ert : trackRequest) {
            switch (ert.getStatus()) {
                case UNSERVICED -> {
                    if (handleRequestDirection(minMax, ert.getSourceFloor(), ert.getDirection())){
                        minMax = ert.getSourceFloor();
                        nextRequest = ert;
                    }
                }
                case SERVICING -> {
                    if (handleRequestDirection(minMax, ert.getDestFloor(), ert.getDirection())){
                        minMax = ert.getDestFloor();
                        nextRequest = ert;
                    }
                }
            }
        }
        if (nextRequest.getStatus() == RequestStatus.SERVICING) {
            nextRequest.setStatus(RequestStatus.DONE);
        } else {
            nextRequest.setStatus(RequestStatus.SERVICING);
        }
        return nextRequest;
    }

    /**
     * Sends a message to the scheduler
     * @param state The state to send
     * @param floorRequest The original request from a floor
     */
    public void sendMessage(Signal state, SerializableMessage floorRequest) {
        try {
            SerializableMessage sm = (state == Signal.IDLE)?
                    (new SerializableMessage(InetAddress.getLocalHost().getHostAddress(), 8081, state, MessageTypes.ELEVATOR, elevatorId, msgID, null, null)) :
                    (new SerializableMessage(InetAddress.getLocalHost().getHostAddress(), 8081, state, MessageTypes.ELEVATOR, elevatorId, msgID, floorRequest.reqID(), floorRequest.data()));

            logger.info("Elevator is sending message to scheduler");
            queue.put(new ArrayList<>(List.of(sm)));
            logger.info("Elevator sent message to scheduler. Signal: " + sm.signal() + " Data: " + sm.data());
        } catch (UnknownHostException ue) {
            System.exit(1);
        }
    }

    /**
     * Attempts to get a request
     */
    public void receiveMessage() {
        logger.info("Elevator receiving message from scheduler");
        SerializableMessage[] floorRequestMessages;
        do {
            floorRequestMessages = queue.getForElevators(); // groups of request at a time
        } while (floorRequestMessages.length == 0);

        Arrays.stream(floorRequestMessages).map(sm -> new ElevatorRequestTracker(RequestStatus.UNSERVICED, sm)).forEach(trackRequest::add);
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


    /**
     * Run
     */
    public void run(){
        currentState = ElevatorState.start(this);

        while (true) {
            if (currentState instanceof ElevatorIdle) {
                msgID = UUID.randomUUID().toString();
                sendMessage(Signal.IDLE, null);
                logger.info("Elevator is idle");
                receiveMessage();
                receiveRequestEvent();
            } else if (currentState instanceof ElevatorWorking) {
                while (!trackRequest.isEmpty()) {
                    ElevatorRequestTracker trackedFloorRequest = serviceNextRequest();
                    if (trackedFloorRequest.getStatus() == RequestStatus.SERVICING) {
                        logger.info("Elevator received request from scheduler. Signal: " + trackedFloorRequest.getRequest().signal() + " Data: " + trackedFloorRequest.getRequest().data());
                        sendMessage(Signal.WORKING, trackedFloorRequest.getRequest());
                        goToFloor(trackedFloorRequest);
                    } else {
                        goToFloor(trackedFloorRequest);
                        sendMessage(Signal.DONE, trackedFloorRequest.getRequest());
                        logger.info("Elevator is done sending");
                        completeRequestEvent();
                    }
                }
            }

        }
    }


    public static void main(String[] args) throws SocketException, UnknownHostException {
        MessageBuffer queue = new MessageBuffer("ElevatorQueue", new DatagramSocket(8081), new InetSocketAddress(InetAddress.getLocalHost(), 8080), 8080);
        queue.listenAndFillBuffer();
        Thread elevator1 = new Thread(new ElevatorSubsystem(queue, 1), "Elevator1");
        Thread elevator2 = new Thread(new ElevatorSubsystem(queue, 2), "Elevator2");
        Thread elevator3 = new Thread(new ElevatorSubsystem(queue, 3), "Elevator3");

        elevator1.start();
        elevator2.start();
        elevator3.start();

    }
}
