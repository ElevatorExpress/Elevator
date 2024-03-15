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


    /**
     *
     * @param queue
     * @param elevatorId
     */
    public ElevatorSubsystem(MessageBuffer queue, int elevatorId) {
        this.queue = queue;
        this.elevatorId = elevatorId;
        logger = new ElevatorLogger("elevator.ElevatorSubsystem" + elevatorId);
        currentState = null;
        buttons = new ElevatorButtonPanel(22);
    }

    private void goToFloor(ElevatorRequestTracker ert) {
        int floor = ert.getFloorByStatus();
        String direction = getDirection(floor);

        if (ert.getStatus() == RequestStatus.SERVICING) {
            if (currentFloor != floor) {
                logger.info(ert.getRequest().data().get().time() + ": Going " + direction + " to floor: " + floor);
                travelDelay(floor);
                logger.info("Arrived at floor " + floor + " to pick up passengers");
            } else {
                logger.info(ert.getRequest().data().get().time() + " Picking up passengers from floor: " + floor );
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
     *
     * @param floor
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
        if (direction.equals("up")) return (minMax == 0 || minMax > floor);
        return (minMax == 0 ||  floor > minMax);
    }
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
     *
     * @param state
     * @param floorRequest
     */
    public void sendMessage(Signal state, SerializableMessage floorRequest) {
        try {
            SerializableMessage sm = (state == Signal.IDLE)?
                    (new SerializableMessage(InetAddress.getLocalHost().getHostAddress(), 8081, state, MessageTypes.ELEVATOR, elevatorId, UUID.randomUUID().toString(), null, null)) :
                    (new SerializableMessage(InetAddress.getLocalHost().getHostAddress(), 8081, state, MessageTypes.ELEVATOR, elevatorId, UUID.randomUUID().toString(), floorRequest.reqID(), floorRequest.data()));

            logger.info("Elevator is sending message to scheduler");
            queue.put((ArrayList<SerializableMessage>) List.of(sm));
            logger.info("Elevator sent message to scheduler");
        } catch (UnknownHostException ue) {
            System.exit(1);
        }
    }

    /**
     *
     */
    public void receiveMessage() {
        logger.info("Elevator receiving message from scheduler");
        SerializableMessage[] floorRequestMessages = queue.getForElevators(); // groups of request at a time
        Arrays.stream(floorRequestMessages).map(sm -> new ElevatorRequestTracker(RequestStatus.UNSERVICED, sm)).forEach(trackRequest::add);
        logger.info("Elevator received message from scheduler");
    }


    /**
     *
     */
    public void completeRequestEvent(){
        currentState = currentState.handleCompleteRequest();
    }

    /**
     *
     */
    public void receiveRequestEvent(){
        currentState = currentState.handleReceiveRequest();
    }


    /**
     *
     */
    public void run(){
        currentState = ElevatorState.start(this);
        while (true) {
            if (currentState instanceof ElevatorIdle) {
                sendMessage(Signal.IDLE, null);
                logger.info("Elevator is idle");
                receiveMessage();
                receiveRequestEvent();
            } else if (currentState instanceof ElevatorWorking) {
                while (!trackRequest.isEmpty()) {
                    ElevatorRequestTracker trackedFloorRequest = serviceNextRequest();

                    if (trackedFloorRequest.getStatus() == RequestStatus.SERVICING) {
                        logger.info("Elevator received request from scheduler: " + trackedFloorRequest.getRequest());
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
        MessageBuffer queue = new MessageBuffer(10, "ElevatorQueue", new DatagramSocket(8081), new InetSocketAddress(InetAddress.getLocalHost(), 8080), 8080);
        queue.listenAndFillBuffer();
        Thread elevator1 = new Thread(new ElevatorSubsystem(queue, 1), "Elevator1");
        Thread elevator2 = new Thread(new ElevatorSubsystem(queue, 2), "Elevator2");

        elevator1.start();
        elevator2.start();






    }
}
