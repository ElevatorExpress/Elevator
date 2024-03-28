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
import util.*;
import util.Messages.MessageTypes;
import util.Messages.SerializableMessage;
import util.Messages.Signal;
import util.states.SchedulerState;

import java.io.IOException;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 * The scheduler.Scheduler class is responsible for managing elevator and floor requests,
 * assigning idle elevators to floor requests, and handling elevator status updates.
 * It implements the Runnable interface to allow it to be executed in a separate thread.
 * At hte moment it is at risk of circular wait, and needs to be refactored to use semaphores.
 */
public class SchedulerV2 {
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


    /**
     *  Creates a Scheduler
     * @param schedulerAddr The Internet address of the scheduler
     * @param schedulerPort The port that the scheduler listens to
     * @param floorSubSystemPort The port that the floor system listens to
     */
    public SchedulerV2(InetAddress schedulerAddr, int schedulerPort, int floorSubSystemPort, AllocationStrategy allocationStrategy, SubSystemSharedState sharedState) {
        try {
            this.floorSubSystemPort = floorSubSystemPort;
//            this.elevatorSubSystemPort = elevatorSubSystemPort;
            inSocket = new DatagramSocket(schedulerPort, schedulerAddr);
            InetSocketAddress inetSocketAddress = new InetSocketAddress(schedulerAddr, schedulerPort);
            outSocket = new DatagramSocket();
            floorMessageBuffer = new MessageBuffer("FloorMessageBuffer", inSocket, inetSocketAddress, schedulerPort);
            testStopBit = true;
            this.sharedState = sharedState;
            registry = LocateRegistry.getRegistry();
            registry.bind("SharedSubSystemState", sharedState);
            assignedWork = new HashMap<>();
            this.allocationStrategy = allocationStrategy;
//            floorMessageBuffer.listenAndFillBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentState = null; // Started with startSystem()
    }


    //ToDo: Does the FCS expect something in the data payload? currently it is null on response.
    public boolean handleECSUpdate() throws InterruptedException, IOException {

        //Check the message buffer for updates. if theres none return false
        if(floorMessageBuffer.isBufferEmpty()){
            return false;
        }
        //If there is a message in the buffer construct an array of work assignemnts and submit them to allocate, then to the shared state
        SerializableMessage[] floorReqs = floorMessageBuffer.get();
        ArrayList<WorkAssignment> newWorkAssignments = new ArrayList<>();
        for (SerializableMessage floorReq : floorReqs) {
            String reqId = floorReq.reqID();
            int serviceFloor = floorReq.senderID();
            int destinationFloor = Integer.parseInt(floorReq.data().requestFloor());
            String assignmentTimeStamp = floorReq.data().time();
            Direction direction = Objects.equals(floorReq.data().direction(), "UP") ? Direction.UP : Direction.DOWN;
            String floorSenderAddr = floorReq.senderAddr();
            int floorSenderPort = floorReq.senderPort();

            WorkAssignment wa = new WorkAssignment(serviceFloor, destinationFloor, assignmentTimeStamp, direction, reqId, floorSenderAddr, floorSenderPort);

            newWorkAssignments.add(wa);
            assignedWork.put(reqId, wa);
            //Update the shared object with new work assignments
            sharedState.addNewWorkAssignment(wa);
        }

        //iterate through update, check for completed assignments, remove them from the assigned work buffer and respond
        //to the floor system
        for (int assignmentKey : sharedState.getWorkAssignments().keySet()) {
            WorkAssignment wa = sharedState.getWorkAssignments().get(assignmentKey).peek();
            assert wa != null;
            if (wa.isPickupComplete() && wa.isDropoffComplete()) {
                WorkAssignment completedAssignment = assignedWork.remove(wa.getFloorRequestId());

                String floorAddr = completedAssignment.getSenderAddr();
                //ToDo: POTENTIAL BUG! getbyname may not be correct method
                InetAddress floorAddress = InetAddress.getByName(floorAddr);
                int floorPort = completedAssignment.getSenderPort();
                SerializableMessage message = new SerializableMessage(floorAddr, floorPort, Signal.DONE, MessageTypes.FLOOR, wa.getServiceFloor(), wa.getFloorRequestId(), wa.getFloorRequestId(), null);
                MessageHelper.SendMessage(outSocket, message, floorAddress, floorSubSystemPort);
            }else {

                String floorAddr = wa.getSenderAddr();
                //ToDo: POTENTIAL BUG! getbyname may not be correct method
                InetAddress floorAddress = InetAddress.getByName(floorAddr);
                int floorPort = wa.getSenderPort();
                SerializableMessage message = new SerializableMessage(floorAddr, floorPort, Signal.WORKING, MessageTypes.FLOOR, wa.getServiceFloor(), wa.getFloorRequestId(), wa.getFloorRequestId(), null);

                MessageHelper.SendMessage(outSocket, message, floorAddress, floorSubSystemPort);
            }
        }


        return true;

    }



    /**
     * Starts the system
     */
    public void startSystem() {
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
        Scheduler s = new Scheduler(InetAddress.getLocalHost(),8080, 8081,8082);
        s.startSystem();
    }
}

