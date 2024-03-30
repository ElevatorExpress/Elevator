//package scheduler;
//
//import org.junit.runner.Runner;
//import util.SubSystemSharedState;
//
//import java.rmi.AlreadyBoundException;
//import java.rmi.RemoteException;
//import java.rmi.registry.LocateRegistry;
//import java.rmi.registry.Registry;
//
//public class SchedulerV2 implements Runnable {
//    private SubSystemSharedState sharedState;
//    Registry registry;
//
//
//    public SchedulerV2(SubSystemSharedState sharedState) throws RemoteException, AlreadyBoundException {
//        this.sharedState = sharedState;
//        registry = LocateRegistry.getRegistry();
//        registry.bind("SharedSubSystemState", sharedState);
//    }
//
//
//    public void schedule(){
//        // Grab the state, then make assignents.
//
//    }
//    public void run () {
//        while (true) {
//            // do something
//
//        }
//    }
//}

package scheduler;

import scheduler.strategies.AllocationStrategy;
import scheduler.strategies.LoadBalancedStrategy;
import util.*;
import util.Messages.MessageTypes;
import util.Messages.SerializableMessage;
import util.Messages.Signal;
import util.states.SchedulerState;

import java.io.IOException;
import java.net.*;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 * The scheduler.Scheduler class is responsible for managing elevator and floor requests,
 * assigning idle elevators to floor requests, and handling elevator status updates.
 * It implements the Runnable interface to allow it to be executed in a separate thread.
 * At hte moment it is at risk of circular wait, and needs to be refactored to use semaphores.
 */
public class Scheduler {
    private DatagramSocket inSocket;
    private DatagramSocket outSocket;
    private SchedulerState currentState;
    private SubSystemSharedState sharedState;

    private
    Registry registry;

    // Key is Message ID, Value is the message
    //Requests are taken from the shared buffer and parsed into the appropriate buffer, these are requests that have yet
    //to be serviced

    private static final ElevatorLogger logger = new ElevatorLogger("Scheduler");

    private HashMap<String, WorkAssignment> assignedWork;

    private int elevatorSubSystemPort;
    private int floorSubSystemPort;
    volatile boolean testStopBit;
    private MessageBuffer floorMessageBuffer;
    private AllocationStrategy allocationStrategy;

    private ConcurrentLinkedDeque<WorkAssignment> pendingWorkAssignments;
    private Set<String> requestSet;


    /**
     *  Creates a Scheduler
     * @param schedulerAddr The Internet address of the scheduler
     * @param schedulerPort The port that the scheduler listens to
     * @param floorSubSystemPort The port that the floor system listens to
     */
    public Scheduler(InetAddress schedulerAddr, int schedulerPort, int floorSubSystemPort, AllocationStrategy allocationStrategy, SubSystemSharedState sharedState) {
        try {
            this.floorSubSystemPort = floorSubSystemPort;
//            this.elevatorSubSystemPort = elevatorSubSystemPort;
            inSocket = new DatagramSocket(schedulerPort, schedulerAddr);
            InetSocketAddress inetSocketAddress = new InetSocketAddress(schedulerAddr, schedulerPort);
            outSocket = new DatagramSocket();
            floorMessageBuffer = new MessageBuffer("FloorMessageBuffer", inSocket, inetSocketAddress, schedulerPort);
            testStopBit = true;
            this.sharedState = sharedState;
            LocateRegistry.createRegistry(1099);
            Naming.bind("SharedSubSystemState", sharedState);
            assignedWork = new HashMap<>();
            requestSet = new HashSet<>();
            this.allocationStrategy = allocationStrategy;
            this.sharedState.setScheduler(this);
//            floorMessageBuffer.listenAndFillBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentState = null; // Started with startSystem()
    }


    //ToDo: Does the FCS expect something in the data payload? currently it is null on response.
    public boolean handleECSUpdate() throws InterruptedException, IOException {
        boolean updated = false;
        //Check the message buffer for updates. if theres none return false
        if(!floorMessageBuffer.isBufferEmpty()) {
            updated = true;
            //If there is a message in the buffer construct an array of work assignemnts and submit them to allocate, then to the shared state
            SerializableMessage[] floorReqs = floorMessageBuffer.get();
//        ArrayList<WorkAssignment> newWorkAssignments = new ArrayList<>();
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

                WorkAssignment wa = new WorkAssignment(serviceFloor, destinationFloor, assignmentTimeStamp, direction, reqId, floorSenderAddr, floorSenderPort, signal, errorBit);

//            newWorkAssignments.add(wa);
                //Update the shared object with new work assignments
                logger.info("New Floor Request: " + wa);
                allocationStrategy.allocate(wa);
                sharedState.addNewWorkAssignment(wa);
            }
        }

        //iterate through update, check for completed assignments, remove them from the assigned work buffer and respond
        //to the floor system
        //
        if(sharedState.getWorkAssignments() == null) return updated;
        for (int assignmentKey : sharedState.getWorkAssignments().keySet()) {
            if (sharedState.getWorkAssignments().get(assignmentKey).isEmpty()) return updated;
            ConcurrentLinkedDeque<WorkAssignment> wa = sharedState.getWorkAssignments().get(assignmentKey);
            for (WorkAssignment workAssignment : wa) {
                assert wa != null;
                if (workAssignment.isPickupComplete() && workAssignment.isDropoffComplete() && requestSet.add(workAssignment.getFloorRequestId())) {
                    sharedState.getWorkAssignments().get(assignmentKey).remove();
                    String floorAddr = workAssignment.getSenderAddr();
                    int floorPort = workAssignment.getSenderPort();
                    logger.info(wa + " drop off: " + workAssignment.dropoffComplete);
                    SerializableMessage message = new SerializableMessage(
                            floorAddr,
                            floorPort,
                            Signal.DONE,
                            MessageTypes.FLOOR,
                            workAssignment.getServiceFloor(),
                            workAssignment.getFloorRequestId(),
                            workAssignment.getFloorRequestId(),
                            null);
                    MessageHelper.SendMessage(outSocket, message, InetAddress.getLocalHost(), floorSubSystemPort); //scheduler reads it
                }
            }

        }
        return updated;
    }



    /**
     * Starts the system
     */
    public void startSystem() {
        //TODO: Graceful shutdown
        //ToDo: complete SchedulerV2, port changes over to scheduler class so this method can be called.
//        currentState = SchedulerState.start(this);
//        readBuffer();
        floorMessageBuffer.listenAndFillBuffer();
        //From what I've read, the RMI should be running in the background and manage the thread lifecycle.

        // Normal function of this class should not  reach this point. This is for testing only
//        try {
//            Thread.sleep(200);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        inSocket.close();
//        outSocket.close();
    }


    public static void main(String[] args) throws IOException {
        SubSystemSharedState sharedState = new SubSystemSharedState();
        AllocationStrategy allocationStrategy1 = new LoadBalancedStrategy(sharedState);
        Scheduler s = new Scheduler(InetAddress.getLocalHost(),8080, 8082,allocationStrategy1,sharedState);

        s.startSystem();
    }
}

