import Messages.*;
import java.util.*;
import static java.lang.Math.abs;

/**
 * Class ElevatorSubsystem creates a subsystem thread for an elevator. The class will process requests sent by the scheduler
 * and go to the requested floors to pick up passengers. Once the passengers have been picked up, the elevator will deliver
 * passengers to the destination floor
 *
 * @author Yasir Sheikh
 */
public class ElevatorSubsystem implements SubSystem<MessageInterface<String>> {

    private Integer currentFloor = 1;
    private MessageBuffer outboundBuffer, inboundBuffer;
    private HashMap<Integer, String> lamp = new HashMap<>();
    private MessageInterface<String>[] floorRequestMessages;
    private String elevatorId = UUID.randomUUID().toString();

    /**
     * Creates the Elevator subsystem and populates the lamps within the elevator car
     *
     * @param outboundBuffer The buffer to send signals from elevator to scheduler
     * @param inboundBuffer The buffer to receive request from the scheduler to the elevator
     */
    public ElevatorSubsystem(MessageBuffer outboundBuffer, MessageBuffer inboundBuffer) {
        this.outboundBuffer = outboundBuffer;
        this.inboundBuffer = inboundBuffer;
        populateLamp();
    }

    /**
     * Goes to the floor where the request originated. Picks up passengers from the request floor.
     *
     * @param floorRequest The request which is being fulfilled
     * @throws InterruptedException
     */
    private void goToSourceFloor(MessageInterface<String> floorRequest) throws InterruptedException {
        Integer sourceFloor = Integer.parseInt(floorRequest.getData().get("ServiceFloor"));
        String direction = getDirection(sourceFloor);

        System.out.println(floorRequest.getData().get("Time") + " : Going " + direction + " to floor " + sourceFloor + " to get passengers");
        travelDelay(sourceFloor);
        System.out.println("Arrived at floor" + sourceFloor);
        currentFloor = sourceFloor;
    }

    /**
     * Sets the lamp to on when passenger clicks a floor. Goes to the destination floor chosen.
     *
     * @param floorRequest The request which is being fulfilled
     * @throws InterruptedException
     */
    private void goToDestinationFloor(MessageInterface<String> floorRequest) throws InterruptedException {
        Integer destFloor = Integer.parseInt(floorRequest.getData().get("Floor"));
        setLamp(destFloor, "on");

        System.out.println("Elevator car button:" + destFloor + "is" + lamp.get(destFloor));
        System.out.println("Going" + floorRequest.getData().get("RequestDirection") + "to destination floor:" + destFloor);
        travelDelay(destFloor);
        System.out.println("Arrived at floor:" + destFloor);

        currentFloor = destFloor;
        setLamp(destFloor, "off");
        signalScheduler(Signal.DONE, floorRequest);
        System.out.println("Elevator is done sending");
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
     * Fills all the car buttons with corresponding values and states (off).
     */
    private void populateLamp() {
        for (int i = 1; i < 23; i++) {
            lamp.put(i, "off");
        }
    }

    /**
     * Turns the lamp on/off for the destination button pressed.
     *
     * @param levelButton The floor number pressed
     * @param state The next state of the lamp
     */
    private void setLamp(Integer levelButton, String state) {
        lamp.put(levelButton, state);
    }

    /**
     * Sends the scheduler a message containing the request being fulfilled along with the state of the elevator
     *
     * @param state The next state of the elevator
     * @param floorRequest The request which the elevator is fulfilling/ fulfilled.
     */
    private void signalScheduler(Signal state, MessageInterface<String> floorRequest) {
        HashMap<String, MessageInterface<?>> workData = new HashMap<>();
        workData.put("Servicing", floorRequest);
        if (state == Signal.IDLE) {
            workData = null;
        }

        MessageInterface[] elevatorMessage = {new ElevatorMessageFactory<MessageInterface<?>>().createElevatorMessage(elevatorId, workData, state)};
//        System.out.println("Elevator Signalling message to scheduler");
        sendMessage(elevatorMessage);
    }

    /**
     * Gets the request from a shared buffer between the scheduler and elevator
     */
    @Override
    public void receiveMessage() {
        System.out.println("Elevator receiving message from scheduler");
        floorRequestMessages = inboundBuffer.get(); // groups of request at a time
        System.out.println("Elevator received message from scheduler");
    }

    /**
     * Sending a request to the shared buffer between the scheduler and elevator
     * @param message The message indicating the current info for the elevator
     * @return A String for the request from the floor.
     */
    @Override
    public String[] sendMessage(MessageInterface[] message) {
        System.out.println("Elevator sending message to scheduler");
        outboundBuffer.put(message);
        System.out.println("Elevator sent message to scheduler");
        return new String[0];
    }

    /**
     * Creates the thread and performs the operations of the elevator
     */
    @Override
    public void run(){
        signalScheduler(Signal.IDLE, null);
        System.out.println("Elevator is idle");
        try {
            while (true) {
                receiveMessage();
                for (MessageInterface<String> floorRequest : floorRequestMessages) {
                    if (floorRequest == null) {
                        throw new IllegalArgumentException("Invalid floorRequest: " + floorRequest);
                    }
                    System.out.println("Elevator received request from scheduler: " + floorRequest);
                    signalScheduler(Signal.WORKING, floorRequest ); // assuming that get() always returns with request
                    goToSourceFloor(floorRequest);
                    goToDestinationFloor(floorRequest);
                    signalScheduler(Signal.IDLE, floorRequest);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
