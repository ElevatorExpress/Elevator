package util;

import scheduler.Scheduler;
import scheduler.SchedulerV2;
import util.states.ElevatorState;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SubSystemSharedState extends UnicastRemoteObject {

    private HashMap <Integer, ElevatorStateUpdate> elevatorStates;


    // Placeholder this needsd to change
    private HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments;

    private ArrayList<WorkAssignment> newWorkAssignmentBuffer;
    SchedulerV2 scheduler;








    // If this method returns true then there has been updated floor requests. The ECS should reconsider.
    public boolean ecsUpdate(HashMap<Integer, ElevatorStateUpdate> stateUpdate) throws InterruptedException, IOException {
        elevatorStates = stateUpdate;
        return scheduler.handleECSUpdate();
    }

    public SubSystemSharedState(SchedulerV2 sched) throws RemoteException {
        super();
        elevatorStates = new HashMap<>();
        scheduler = sched;
    }

    public void addWorkAssignmentBuffer(WorkAssignment workAssignment) {
        newWorkAssignmentBuffer.add(workAssignment);
    }

    public ArrayList<WorkAssignment> flushNewWorkAssignmentBuffer() {
        // Will clearing the buffer also clear temp?
        ArrayList<WorkAssignment> temp = newWorkAssignmentBuffer;
        newWorkAssignmentBuffer.clear();
        return temp;
    }

    public void setNewWorkAssignmentsBuffer(ArrayList<WorkAssignment> workAssignments) {
        newWorkAssignmentBuffer = workAssignments;
    }

    public ArrayList<WorkAssignment> getWorkAssignmentsBuffer() {
        return newWorkAssignmentBuffer;
    }


    public HashMap<Integer, ElevatorStateUpdate> getElevatorStates() {
        return elevatorStates;
    }

    //interger return value is a place holder.




    public void setElevatorStates(HashMap<Integer, ElevatorStateUpdate> elevatorStates) {
        this.elevatorStates = elevatorStates;
    }

    public void setWorkAssignments(HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments) {
        this.workAssignments = workAssignments;
    }

    public HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> getWorkAssignments() {
        return workAssignments;
    }

    public void addElevatorState(int elevatorId, ElevatorStateUpdate elevatorState) {
        elevatorStates.put(elevatorId, elevatorState);
    }

    public void removeElevatorState(int elevatorId) {
        elevatorStates.remove(elevatorId);
    }

    public void addNewWorkAssignment(WorkAssignment workAssignment) {
        newWorkAssignmentBuffer.add(workAssignment);
    }

    public void addWorkAssignment(int elevatorId, WorkAssignment workAssignment) {
        workAssignments.get(elevatorId).add(workAssignment);
    }

    public void removeWorkAssignment(int elevatorId, WorkAssignment workAssignment) {
        workAssignments.get(elevatorId).remove(workAssignment);
    }

    public void setWorkAssignmentQueue(int elevatorId, ConcurrentLinkedDeque<WorkAssignment> workAssignments) {
        this.workAssignments.put(elevatorId, workAssignments);
    }

    public ConcurrentLinkedDeque<WorkAssignment> getWorkAssignmentQueue(int elevatorId) {
        return workAssignments.get(elevatorId);
    }

    public void clearWorkAssignmentQueue(int elevatorId) {
        workAssignments.get(elevatorId).clear();
    }

    public void setAllWorkAssignments(HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments) {
        this.workAssignments = workAssignments;
    }

    public void clearAllWorkAssignments() {
        for (int elevatorId : workAssignments.keySet()) {
            workAssignments.get(elevatorId).clear();
        }
    }

    public void clearAllElevatorStates() {
        elevatorStates.clear();
    }

    public void clearAll() {
        clearAllWorkAssignments();
        clearAllElevatorStates();
    }

    public HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> getAllWorkAssignments() {
        return workAssignments;
    }

}
