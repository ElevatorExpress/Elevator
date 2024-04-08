package scheduler;

import scheduler.strategies.AllocationStrategy;
import scheduler.strategies.LoadBalancedStrategy;
import util.*;
import util.Messages.MessageTypes;
import util.Messages.SerializableMessage;
import util.Messages.Signal;
import util.states.SchedulerScheduling;
import util.states.SchedulerState;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 * The scheduler.Scheduler class is responsible for managing elevator and floor requests,
 * assigning idle elevators to floor requests, and handling elevator status updates.
 */
public class Scheduler {
    private DatagramSocket outSocket;
    private SchedulerState currentState;
    private SubSystemSharedState sharedState;

    private static final ElevatorLogger logger = new ElevatorLogger("Scheduler");

    private int floorSubSystemPort;
    volatile boolean testStopBit;
    private MessageBuffer floorMessageBuffer;
    private AllocationStrategy allocationStrategy;
    private Set<String> requestSet;


    /**
     * Creates a Scheduler
     * @param schedulerAddr The Internet address of the scheduler
     * @param schedulerPort The port that the scheduler listens to
     * @param floorSubSystemPort The port that the floor system listens to
     * @param allocationStrategy the object that will do the allocation
     * @param sharedState object that holds the states of the elevators
     */
    public Scheduler(InetAddress schedulerAddr, int schedulerPort, int floorSubSystemPort, AllocationStrategy allocationStrategy, SubSystemSharedState sharedState) {
        try {
            //Set up port to receive and send
            this.floorSubSystemPort = floorSubSystemPort;
            DatagramSocket inSocket = new DatagramSocket(schedulerPort, schedulerAddr);
            InetSocketAddress inetSocketAddress = new InetSocketAddress(schedulerAddr, schedulerPort);
            outSocket = new DatagramSocket();
            floorMessageBuffer = new MessageBuffer("FloorMessageBuffer", inSocket, inetSocketAddress, schedulerPort);
            testStopBit = true;
            this.sharedState = sharedState;
            LocateRegistry.createRegistry(1099);
            Naming.bind("SharedSubSystemState", sharedState);
            requestSet = new HashSet<>();
            this.allocationStrategy = allocationStrategy;
            //Sets this object as the scheduler of the sharedState
            this.sharedState.setScheduler(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentState = null; // Started with startSystem()
    }

    /**
     * Handles Elevator Control System Updates
     * @return true if updated, false if not
     */
    public boolean handleECSUpdate() throws InterruptedException, IOException {
        boolean updated = false;
        //Check the message buffer for updates. if there's none return false
        if (currentState instanceof SchedulerScheduling) {
            if (!floorMessageBuffer.isBufferEmpty()) {
                updated = true;
                //If there is a message in the buffer construct an array of work assignments and submit them to allocate, then to the shared state
                SerializableMessage[] floorReqs = floorMessageBuffer.get();
                doneReading();
                for (SerializableMessage floorReq : floorReqs) {
                    String reqId = floorReq.reqID();
                    int serviceFloor = floorReq.senderID();
                    int destinationFloor = Integer.parseInt(floorReq.data().requestFloor());
                    String assignmentTimeStamp = floorReq.data().time();
                    Direction direction = Objects.equals(floorReq.data().direction(), "up") ? Direction.UP : Direction.DOWN;
                    Signal signal = floorReq.signal();
                    String floorSenderAddr = floorReq.senderAddr();
                    int floorSenderPort = floorReq.senderPort();
                    int errorBit = Integer.parseInt(floorReq.data().error());

                    WorkAssignment workAssignment = new WorkAssignment(serviceFloor, destinationFloor, assignmentTimeStamp, direction, reqId, floorSenderAddr, floorSenderPort, signal, errorBit);

                    //Update the shared object with new work assignments
                    logger.info("Received Request: " + workAssignment);
                    allocationStrategy.allocate(workAssignment);
                }
                //Signal that the Scheduler is done reading
                doneServing();
            }

            //Check for completed assignments, remove them from the assigned work buffer and respond to the floor system
            if (sharedState.getWorkAssignments() == null) {

                doneServing();
                return updated;
            }
            for (int assignmentKey : sharedState.getWorkAssignments().keySet()) {
                if (sharedState.getWorkAssignments().get(assignmentKey).isEmpty()) {
                    continue;
                }
                ConcurrentLinkedDeque<WorkAssignment> workAssignments = sharedState.getWorkAssignments().get(assignmentKey);
                for (WorkAssignment workAssignment : workAssignments) {
                    //If the current request is done
                    if (workAssignment.isPickupComplete() && workAssignment.isDropoffComplete() && requestSet.add(workAssignment.getFloorRequestId())) {
                        //Remove the assignment
                        sharedState.getWorkAssignments().get(assignmentKey).remove();
                        logger.info("Sending Request Completion:" + workAssignment);
                        //Constructs and sends a DONE message
                        SerializableMessage message = new SerializableMessage(
                                workAssignment.getSenderAddr(),
                                workAssignment.getSenderPort(),
                                Signal.DONE,
                                MessageTypes.FLOOR,
                                assignmentKey,
                                workAssignment.getFloorRequestId(),
                                workAssignment.getFloorRequestId(),
                                null);
                        MessageHelper.SendMessage(outSocket, message, InetAddress.getLocalHost(), floorSubSystemPort); //scheduler sends completed assignment to floor
                    }
                }
            }
            doneServing();
        }
        return updated;
    }

    /**
     * gets the shared state object
     * @return The shared object
     */
    public SubSystemSharedState getSharedState() {
        return sharedState;
    }

    /**
     * Done reading event
     */
    private void doneReading() {
        currentState = currentState.handleDoneReadingRequest();
    }

    /**
     * Gets the message buffer
     * @return The message buffer
     */
    public MessageBuffer getMessageBuffer() {
        return floorMessageBuffer;
    }
    /**
     * Done serving event
     */
    private void doneServing() {
        currentState = currentState.handleDoneServing();
    }

    /**
     * Handle an ECS emergency, reallocates uncompleted work requests
     * @param workRequests The work requests to reallocate
     * @return False if succeeded
     */
    public boolean handleECSEmergency(ArrayList<WorkAssignment> workRequests) throws RemoteException {
        for (WorkAssignment workRequest : workRequests) {
            allocationStrategy.allocate(workRequest);
        }
        return false;
    }

    /**
     * Starts the system
     */
    public void startSystem() {
        //TODO: Graceful shutdown
        currentState = SchedulerState.start(this);
        floorMessageBuffer.listenAndFillBuffer();
    }

    public static void main(String[] args) throws RemoteException, UnknownHostException {
        SubSystemSharedState sharedState = new SubSystemSharedState();
        AllocationStrategy allocationStrategy1 = new LoadBalancedStrategy(sharedState);
        Scheduler s = new Scheduler(InetAddress.getLocalHost(), 8080, 8082, allocationStrategy1, sharedState);
        s.startSystem();
    }

}

