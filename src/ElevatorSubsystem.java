import Messages.*;

import java.util.*;
import static java.lang.Math.abs;

/**
 * Class ElevatorSubsystem
 *
 */
public class ElevatorSubsystem implements SubSystem<MessageInterface<String, ElevatorSignal>> {

    private Integer currentFloor = 1;
    private MessageBuffer outboundBuffer, inboundBuffer;
    private HashMap<Integer, String> lamp = new HashMap<>();
    private MessageInterface<String, FloorSignal>[] floorRequestMessages;
    private String elevatorId = UUID.randomUUID().toString();

    public ElevatorSubsystem(MessageBuffer outboundBuffer, MessageBuffer inboundBuffer) {
        this.outboundBuffer = outboundBuffer;
        this.inboundBuffer = inboundBuffer;
        populateLamp();
    }

    private void goToSourceFloor(MessageInterface<String, FloorSignal> floorRequest) throws InterruptedException {
        Integer sourceFloor = Integer.parseInt(floorRequest.getData().get("ServiceFloor"));
        String direction = getDirection(sourceFloor);

        System.out.println(floorRequest.getData().get("Time") + ": Going " + direction + " to floor " + sourceFloor + " to get passengers");
        travelDelay(sourceFloor);
        System.out.println("Arrived at floor" + sourceFloor);
        currentFloor = sourceFloor;
    }

    private void goToDestinationFloor(MessageInterface<String, FloorSignal> floorRequest) throws InterruptedException {
        Integer destFloor = Integer.parseInt(floorRequest.getData().get("Floor"));
        setLamp(destFloor, "on");

        System.out.println("Elevator car button:" + destFloor + "is" + lamp.get(destFloor));
        System.out.println("Going" + floorRequest.getData().get("RequestDirection") + "to destination floor:" + destFloor);
        travelDelay(destFloor);
        System.out.println("Arrived at floor:" + destFloor);

        currentFloor = destFloor;
        setLamp(destFloor, "off");
        signalScheduler(ElevatorSignal.DONE, floorRequest);
    }

    private void travelDelay(Integer floor) throws InterruptedException {
        if (abs(floor - currentFloor) == 1) {
            // finding distance between floor and calculating time of travel + time of door open and close
            Thread.sleep((long) (6140 + (1000 * 12.58)));
        } else {
            // finding distance between floor and calculating time of travel + time of door open and close
            Thread.sleep((long) (1000 * ((abs(floor - currentFloor) * 4L / 2.53) + 12.58)));
        }
    }

    private String getDirection(Integer arrivalFloor) {
        if (arrivalFloor - currentFloor > 0) {return "up";}
        return "down";
    }

    private void populateLamp() {
        for (int i = 1; i < 23; i++) {
            lamp.put(i, "off");
        }
    }

    private void setLamp(Integer levelButton, String state) {
        lamp.put(levelButton, state);
    }

    private void signalScheduler(ElevatorSignal state, MessageInterface<String, FloorSignal> floorRequest) {
        HashMap<String, MessageInterface<?, ?>> workData = new HashMap<>();
        workData.put("Servicing", floorRequest);
        if (state == ElevatorSignal.IDLE) {
            workData = null;
        }

        MessageInterface[] elevatorMessage = {new ElevatorMessageFactory<MessageInterface<?, ?>>().createElevatorMessage(elevatorId, workData, state)};
        sendMessage(elevatorMessage);
    }

    @Override
    public void receiveMessage() {
        floorRequestMessages = inboundBuffer.get(); // groups of request at a time
    }

    @Override
    public String[] sendMessage(MessageInterface[] message) {
        outboundBuffer.put(message);
        return new String[0];
    }

    @Override
    public void run(){
        signalScheduler(ElevatorSignal.IDLE, null);
        try {
            while (true) {
                receiveMessage();
                for (MessageInterface<String, FloorSignal> floorRequest : floorRequestMessages) {
                    signalScheduler(ElevatorSignal.WORKING, floorRequest ); // assuming that get() always returns with request
                    goToSourceFloor(floorRequest);
                    goToDestinationFloor(floorRequest);
                    signalScheduler(ElevatorSignal.IDLE, floorRequest);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
