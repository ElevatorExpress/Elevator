import Messages.ElevatorMessageFactory;
import Messages.ElevatorSignal;
import Messages.MessageInterface;
import Messages.MessageTypes;

import java.util.*;

import static java.lang.Math.abs;

public class ElevatorSubsystem implements SubSystem<MessageInterface<String, ElevatorSignal>> {

    private Integer currentFloor = 1;
    private ElevatorSignal state = ElevatorSignal.IDLE;
    private MessageBuffer outboundBuffer, inboundBuffer;
    private HashMap<Integer, String> lamp = new HashMap<>(); // some elevator button
    private Object taskRequest;
    private String elevatorId = UUID.randomUUID().toString();

    public ElevatorSubsystem(MessageBuffer outboundBuffer, MessageBuffer inboundBuffer) {
        this.outboundBuffer = outboundBuffer;
        this.inboundBuffer = inboundBuffer;
        populateLamp();
    }

    private void populateLamp(){
        for (int i = 1; i < 23; i++) {
            lamp.put(i, "off");
        }
    }

    private void setLamp(Integer levelButton, String state) {
        lamp.put(levelButton, state);
    }
    private void goToSourceFloor() throws InterruptedException {
        String direction = "down";
        if (1 - currentFloor > 0) {direction = "up";}
        // Going to pick up passengers
        System.out.println("Going "+ "o.getDirection()" + " to floor:" + "o.getFloor()" + "to get passengers");
        Thread.sleep(100 *  12);

        // signal scheduler
        // get response to open doors
        System.out.println("Arrived to floor" + "o.getFloor()");
        currentFloor = 12;
        //signal
    }

    private void goToDestinationFloor() throws InterruptedException {
        String direction = "off";
        setLamp(4, "on");
        if (4 - currentFloor > 0) {direction = "up";}

        // Taking passengers to dest floor
        System.out.println("Going" + direction + "to destination floor:" + "o.getCarButton()");
        Thread.sleep(100 * abs(4-12));
        System.out.println("Arrived to floor" + "o.getCarButton()");
        currentFloor = 4;
    }

    private void signalScheduler() {
        HashMap<String, Object> workData = new HashMap<>();
        workData.put("FloorRequestId", taskRequest);
        MessageInterface[] m = {new ElevatorMessageFactory<>().createElevatorMessage(elevatorId, workData, state)};
        sendMessage(m);
    }

    // outbound -> task - shared between scheduler and elevator
    // get from outbound
    @Override
    public void receiveMessage() {
        taskRequest = inboundBuffer.get();
    }

    @Override
    public String[] sendMessage(MessageInterface[] message) {
        outboundBuffer.put(message);
        return new String[0];
    }

    @Override
    public void run(){
        MessageInterface[] m = new MessageInterface[1];
        try {
            signalScheduler();
            goToSourceFloor();
            goToDestinationFloor();
//            sendMessage();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
