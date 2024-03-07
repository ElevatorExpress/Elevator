package elevator;

import util.Messages.ElevatorMessageFactory;
import util.Messages.MessageInterface;
import util.Messages.Signal;
import util.ElevatorLogger;
import util.MessageBuffer;
import util.SubSystem;
import util.states.ElevatorState;

import java.util.HashMap;
import java.util.UUID;

import static java.lang.Math.abs;

/**
 * Class elevator.ElevatorSubsystem creates a subsystem thread for an elevator. The class will process requests sent by the scheduler
 * and go to the requested floors to pick up passengers. Once the passengers have been picked up, the elevator will deliver
 * passengers to the destination floor
 *
 * @author Yasir Sheikh
 */
public class ElevatorSubsystem implements SubSystem<MessageInterface<String>> {

    private Integer currentFloor = 1;
    private final MessageBuffer outboundBuffer;
    private final MessageBuffer inboundBuffer;
    private final ElevatorButtonPanel buttons;
    private MessageInterface<String>[] floorRequestMessages;
    private final String elevatorId = UUID.randomUUID().toString();
    private ElevatorState currentState;
    private static final ElevatorLogger logger = new ElevatorLogger("elevator.ElevatorSubsystem");

    /**
     * Creates the Elevator subsystem and populates the lamps within the elevator car
     *
     * @param outboundBuffer The buffer to send signals from elevator to scheduler
     * @param inboundBuffer The buffer to receive request from the scheduler to the elevator
     */
    public ElevatorSubsystem(MessageBuffer outboundBuffer, MessageBuffer inboundBuffer) {
        this.outboundBuffer = outboundBuffer;
        this.inboundBuffer = inboundBuffer;
        currentState = null;
        buttons = new ElevatorButtonPanel(22);
    }

    private ElevatorSubsystem() {
        this(new MessageBuffer(10, "DummyOut"), new MessageBuffer(10, "DummyIn"));
    }

    /**
     * Goes to the floor where the request originated. Picks up passengers from the request floor.
     *
     * @param floorRequest The request which is being fulfilled
     * @throws InterruptedException
     */
    private void goToSourceFloor(MessageInterface<String> floorRequest) throws InterruptedException {
        //Gets the floor needed
        Integer sourceFloor = Integer.parseInt(floorRequest.getData().get("ServiceFloor"));
        //Picks the direction of travel
        String direction = getDirection(sourceFloor);
        logger.info(floorRequest.getData().get("Time") + " : Going " + direction + " to floor " + sourceFloor + " to get passengers");
        //The delay it takes to move floors
        travelDelay(sourceFloor);
        logger.info("Arrived at floor" + sourceFloor);
        currentFloor = sourceFloor;
    }

    /**
     * Sets the lamp to on when passenger clicks a floor. Goes to the destination floor chosen.
     *
     * @param floorRequest The request which is being fulfilled
     * @throws InterruptedException
     */
    private void goToDestinationFloor(MessageInterface<String> floorRequest) throws InterruptedException {
        //Grab the floor that needs to be traveled to
        int destFloor = Integer.parseInt(floorRequest.getData().get("Floor"));
        buttons.turnOnButton(destFloor);

        logger.info("Going" + floorRequest.getData().get("RequestDirection") + "to destination floor:" + destFloor);
        //Delay to travel between floors
        travelDelay(destFloor);
        logger.info("Arrived at floor:" + destFloor);

        //Once the elevator arrives update state information
        currentFloor = destFloor;
        buttons.turnOffButton(destFloor);
    }

    /**
     * The time to takes to go to a floor, open doors and close doors.
     *
     * @param floor Used for determining distance
     * @throws InterruptedException
     */
    private void travelDelay(Integer floor) throws InterruptedException {
        if (abs(floor - currentFloor) == 1) {
            // finding distance between floor and calculating time of travel + time of door open and close
            Thread.sleep((long) (6140 + (1000 * 12.58)));
        } else {
            // finding distance between floor and calculating time of travel + time of door open and close
            Thread.sleep((long) (1000 * ((abs(floor - currentFloor) * 4L / 2.53) + 12.58)));
        }
    }

    /**
     * Gets the direction of elevator movement based on arrivalFloor and currentFloor
     *
     * @param arrivalFloor The floor the elevator is moving to
     * @return A String for the determined direction
     */
    private String getDirection(Integer arrivalFloor) {
        if (arrivalFloor - currentFloor > 0) {return "up";}
        return "down";
    }

    /**
     * Sends the scheduler a message containing the request being fulfilled along with the state of the elevator
     *
     * @param state The next state of the elevator
     * @param floorRequest The request which the elevator is fulfilling/ fulfilled.
     */
    public void signalScheduler(Signal state, MessageInterface<String> floorRequest) {
        HashMap<String, MessageInterface<?>> workData = new HashMap<>();
        //If the current state of the elevator IDLE there is no request to be done
        workData.put("Servicing", floorRequest);
        if (state == Signal.IDLE) {
            workData = null;
        }
        //If the elevator is working tell the scheduler
        MessageInterface[] elevatorMessage = {new ElevatorMessageFactory<MessageInterface<?>>().createElevatorMessage(elevatorId, workData, state)};
        sendMessage(elevatorMessage);
    }

    /**
     * Gets the request from a shared buffer between the scheduler and elevator
     */
    @Override
    public void receiveMessage() {
        logger.info("Elevator receiving message from scheduler");
        floorRequestMessages = inboundBuffer.get(); // groups of request at a time
        logger.info("Elevator received message from scheduler");
    }

    /**
     * Sending a request to the shared buffer between the scheduler and elevator
     * @param message The message indicating the current info for the elevator
     * @return A String for the request from the floor.
     */
    @Override
    public String[] sendMessage(MessageInterface[] message) {
        logger.info("Elevator sending message to scheduler");
        outboundBuffer.put(message);
        logger.info("Elevator sent message to scheduler");
        return new String[0];
    }

    public void completeRequestEvent(){
        currentState = this.currentState.handleCompleteRequest();
    }

    public void receiveRequestEvent(){
        currentState = this.currentState.handleReceiveRequest();
    }

    /**
     * Creates the thread and performs the operations of the elevator
     */
    public void run(){
        signalScheduler(Signal.IDLE, null);
        currentState = ElevatorState.start(this);
        logger.info("Elevator is idle");
        try {
            while (true) {
                receiveMessage();
                for (MessageInterface<String> floorRequest : floorRequestMessages) {
                    if (floorRequest == null) {
                        throw new IllegalArgumentException("Invalid floorRequest: null");
                    }
                    logger.info("Elevator received request from scheduler: " + floorRequest);
                    signalScheduler(Signal.WORKING, floorRequest ); // assuming that get() always returns with request
                    receiveRequestEvent();

                    goToSourceFloor(floorRequest);
                    goToDestinationFloor(floorRequest);

                    signalScheduler(Signal.DONE, floorRequest);
                    logger.info("Elevator is done sending");

                    completeRequestEvent();
                    signalScheduler(Signal.IDLE, floorRequest);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        //TODO: Rework constructor, message buffers are no longer shared
        ElevatorSubsystem e = new ElevatorSubsystem(); // DO not use dummy constructor
        e.run();
    }
}
