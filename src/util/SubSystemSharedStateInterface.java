package util;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Interface that supplies methods to communicate between subsystems
 */
public interface SubSystemSharedStateInterface extends Remote {


    HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> getWorkAssignments() throws RemoteException;
    boolean ecsUpdate(HashMap<Integer, ElevatorStateUpdate> stateUpdate) throws InterruptedException, IOException;
    boolean ecsEmergency(ArrayList<WorkAssignment> workRequests) throws RemoteException;
    void addElevatorState(int elevatorId, ElevatorStateUpdate elevatorState) throws RemoteException;
    void addWorkAssignment(int elevatorId, WorkAssignment workAssignment) throws RemoteException;
    void setWorkAssignmentQueue(int elevatorId, ConcurrentLinkedDeque<WorkAssignment> workAssignments) throws RemoteException;
    void removeWorkElevator(int elevatorId) throws RemoteException;

    ArrayList<Integer> getElevatorStopQueue(int elevatorId) throws RemoteException;
    void setElevatorStopQueues(int elevatorId, ArrayList<Integer> stopQueue) throws RemoteException;
    HashMap<Integer, ArrayList<Integer>> getElevatorStopQueues() throws RemoteException;
    void setElevatorStopQueue(int elevatorId, ArrayList<Integer> stopQueue) throws RemoteException;
    void setAllElevatorStopQueues(HashMap<Integer, ArrayList<Integer>> stopQueues) throws RemoteException;
    HashMap<Integer, ElevatorStateUpdate> getElevatorStates() throws RemoteException;

    void setElevatorUpStopQueue(int retId, ArrayList<Integer> upfloorStops) throws RemoteException;

    void setElevatorDownStopQueue(int retId, ArrayList<Integer> downFloorStops) throws RemoteException;

    ArrayList<Integer> getElevatorUpStopQueue(int elevatorId) throws RemoteException;
    ArrayList<Integer> getElevatorDownStopQueue(int elevatorId) throws RemoteException;
}
