package util;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public interface SubSystemSharedStateInterface extends Remote {


    HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> getWorkAssignments() throws RemoteException;
    boolean ecsUpdate(HashMap<Integer, ElevatorStateUpdate> stateUpdate) throws InterruptedException, IOException;
    boolean ecsEmergency(ArrayList<WorkAssignment> workRequests) throws RemoteException;
    void addElevatorState(int elevatorId, ElevatorStateUpdate elevatorState) throws RemoteException;
    void addWorkAssignment(int elevatorId, WorkAssignment workAssignment) throws RemoteException;
    void setWorkAssignmentQueue(int elevatorId, ConcurrentLinkedDeque<WorkAssignment> workAssignments) throws RemoteException;
    void removeWorkElevator(int elevatorId) throws RemoteException;
    ArrayList<WorkAssignment> flushNewWorkAssignmentBuffer() throws RemoteException;
    HashMap<Integer, ElevatorStateUpdate> getElevatorStates() throws RemoteException;
}
