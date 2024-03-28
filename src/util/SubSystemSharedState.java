package util;

import scheduler.Scheduler;
import scheduler.SchedulerV2;
import util.states.ElevatorState;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SubSystemSharedState extends UnicastRemoteObject {

    private HashMap <Integer, ConcurrentLinkedDeque<ElevatorState>> elevatorStates;

    private HashMap<Integer, ConcurrentLinkedDeque<ElevatorState>> floorRequests;

    private HashMap<Integer, ConcurrentLinkedDeque<int>> workAssignments;
    SchedulerV2 scheduler;

    public SubSystemSharedState(SchedulerV2 sched) throws RemoteException {
        super();
        elevatorStates = new HashMap<>();
        scheduler = sched;
    }



    //interger return value is a place holder.
    public ConcurrentLinkedDeque<int> getWorkAssignment(int elevatorId) {
        return workAssignments.get(elevatorId);
    }

    public void addWorkAssignment(int elevatorId, int floor) {
        workAssignments.get(elevatorId).add(floor);
    }

    public void setWorkAssignments(HashMap<Integer, ConcurrentLinkedDeque<int>> workAssignments) {
        this.workAssignments = workAssignments;
    }




    public void registerElevator(int elevatorId) {
        elevatorStates.put(elevatorId, new ConcurrentLinkedDeque<>());
    }
    public void registerFloor(int floor) {
        floorRequests.put(floor, new ConcurrentLinkedDeque<>());
    }
    public void setFloors(HashMap<Integer, ConcurrentLinkedDeque<ElevatorState>> floorRequests) {
        this.floorRequests = floorRequests;
    }
    public void setElevators (HashMap<Integer, ConcurrentLinkedDeque<ElevatorState>> elevatorStates) {
        this.elevatorStates = elevatorStates;
    }

    public HashMap<Integer, ConcurrentLinkedDeque<ElevatorState>> getFloorRequests() {
        return floorRequests;
    }

    public void addFloorRequest(int floor, ElevatorState state) {
        floorRequests.get(floor).add(state);
    }
    public void setFloorRequests(HashMap<Integer, ConcurrentLinkedDeque<ElevatorState>> floorRequests) {
        this.floorRequests = floorRequests;
    }

    public void addState(int elevatorId, ElevatorState state) {
        elevatorStates.get(elevatorId).add(state);
    }

    public ElevatorState peekState(int elevatorId) {
        return elevatorStates.get(elevatorId).peek();
    }

    public void setState(HashMap<Integer, ConcurrentLinkedDeque<ElevatorState>> elevatorStates) {
        this.elevatorStates = elevatorStates;
    }

    public HashMap<Integer, ConcurrentLinkedDeque<ElevatorState>> getElevatorStates() {
        return elevatorStates;
    }

    public ElevatorState popElevatorState(int elevatorId) {
        ElevatorState state = peekState(elevatorId);
        elevatorStates.get(elevatorId).pop();
        return state;
    }










}
