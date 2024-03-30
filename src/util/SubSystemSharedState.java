package util;

import scheduler.Scheduler;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SubSystemSharedState extends UnicastRemoteObject implements SubSystemSharedStateInterface {

    private HashMap <Integer, ElevatorStateUpdate> elevatorStates;


    // Placeholder this needsd to change
    private HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments = new HashMap<>();

    private ArrayList<WorkAssignment> newWorkAssignmentBuffer = new ArrayList<>();
    Scheduler scheduler;

    /**
     * Notifies shared object. ECS had an update
     * @param stateUpdate The elevators with their updates
     * @return True if the shared object was notified
     */
    // If this method returns true then there has been updated floor requests. The ECS should reconsider.
    public boolean ecsUpdate(HashMap<Integer, ElevatorStateUpdate> stateUpdate) throws InterruptedException, IOException {
        elevatorStates = stateUpdate;
        return scheduler.handleECSUpdate();
    }

    /**
     * Notifies shared object. ECS had an emergency
     * @param workRequests To be re allocated
     * @return false if succeeded
     */
    public boolean ecsEmergency(ArrayList<WorkAssignment> workRequests){
        return scheduler.handleECSEmergency(workRequests);
    }


    /**
     * Sets the scheduler
     * @param scheduler The scheduler
     */
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
    public SubSystemSharedState() throws RemoteException {
        super();
        elevatorStates = new HashMap<>();
    }

    /**
     * Flushes The assignment buffer
     * @return The new assignment buffer
     */
    public ArrayList<WorkAssignment> flushNewWorkAssignmentBuffer() {
        // Will clearing the buffer also clear temp?
        ArrayList<WorkAssignment> temp = newWorkAssignmentBuffer;
        newWorkAssignmentBuffer.clear();
        return temp;
    }

    /**
     * Sets new Work Assignments
     * @param workAssignments The new work assignments
     */
    public void setNewWorkAssignmentsBuffer(ArrayList<WorkAssignment> workAssignments) {
        newWorkAssignmentBuffer = workAssignments;
    }

    /**
     * Gets new work assignment
     * @return The new work assignments
     */
    public ArrayList<WorkAssignment> getNewWorkAssignmentsBuffer() {
        return newWorkAssignmentBuffer;
    }


    /**
     * Gets the elevator states
     * @return Mapping of elevator ID's and their states
     */
    public HashMap<Integer, ElevatorStateUpdate> getElevatorStates() {
        return elevatorStates;
    }

    /**
     * Sets the elevators states map
     * @param elevatorStates The state map
     */
    public void setElevatorStates(HashMap<Integer, ElevatorStateUpdate> elevatorStates) {
        this.elevatorStates = elevatorStates;
    }

    /**
     * Set the work assignments
     * @param workAssignments The work assignment
     */
    public void setWorkAssignments(HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments) {
        this.workAssignments = workAssignments;
    }

    /**
     * Get the work assignment
     * @return The work assignment
     */
    public HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> getWorkAssignments() {
        return workAssignments;
    }


    /**
     * Adds an elevator state
     * @param elevatorId The elevator
     * @param elevatorState The elevator state
     */
    public void addElevatorState(int elevatorId, ElevatorStateUpdate elevatorState) {
        elevatorStates.put(elevatorId, elevatorState);
    }

    /**
     * Remove an elevator state
     * @param elevatorId The elevator
     */
    public void removeElevatorState(int elevatorId) {
        elevatorStates.remove(elevatorId);
    }

    /**
     * Remove a work elevator
     * @param elevatorId the elevator
     */
    public void removeWorkElevator(int elevatorId) {
        workAssignments.remove(elevatorId);
        removeElevatorState(elevatorId);
    }

    /**
     * Adds a new work assignment
     * @param workAssignment The new work assignment
     */
    public void addNewWorkAssignment(WorkAssignment workAssignment) {
        newWorkAssignmentBuffer.add(workAssignment);
    }

    /**
     * Add a work assignment
     * @param elevatorId The work assignment
     * @param workAssignment The work assignment
     */
    public void addWorkAssignment(int elevatorId, WorkAssignment workAssignment) {
        workAssignments.get(elevatorId).add(workAssignment);
    }

    /**
     * Remove a work assignment
     * @param elevatorId The elevator
     * @param workAssignment The work assignment to remove
     */
    public void removeWorkAssignment(int elevatorId, WorkAssignment workAssignment) {
        workAssignments.get(elevatorId).remove(workAssignment);
    }

    /**
     * Sets a work assignments to an elevator
     * @param elevatorId  The elevator
     * @param workAssignments The work assignments
     */
    public void setWorkAssignmentQueue(int elevatorId, ConcurrentLinkedDeque<WorkAssignment> workAssignments) {
        this.workAssignments.put(elevatorId, workAssignments);
    }

    /**
     * Gets the work assignments assigned to an elevator
     * @param elevatorId The elevator
     * @return The assigned work assignments
     */
    public ConcurrentLinkedDeque<WorkAssignment> getWorkAssignmentQueue(int elevatorId) {
        return workAssignments.get(elevatorId);
    }

    /**
     * Clears a work assignment queue
     * @param elevatorId The elevator
     */
    public void clearWorkAssignmentQueue(int elevatorId) {
        workAssignments.get(elevatorId).clear();
    }

    /**
     * Sets all work assignments
     * @param workAssignments the work assignments
     */
    public void setAllWorkAssignments(HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> workAssignments) {
        this.workAssignments = workAssignments;
    }

    /**
     * Clears all work assignments queues
     */
    public void clearAllWorkAssignments() {
        for (int elevatorId : workAssignments.keySet()) {
            workAssignments.get(elevatorId).clear();
        }
    }

    /**
     * Clears all elevator states
     */
    public void clearAllElevatorStates() {
        elevatorStates.clear();
    }

    /**
     * Cleats all work assignments and elevator states
     */
    public void clearAll() {
        clearAllWorkAssignments();
        clearAllElevatorStates();
    }

    public HashMap<Integer, ConcurrentLinkedDeque<WorkAssignment>> getAllWorkAssignments() {
        return workAssignments;
    }

}
