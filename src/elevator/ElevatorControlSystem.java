package elevator;

import util.ElevatorStateUpdate;
import util.SubSystemSharedStateInterface;
import util.WorkAssignment;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ElevatorControlSystem {

    // Some code to handle shared object

    private final ArrayList<WorkAssignment> elevatorRequests;
    private final ArrayList<ElevatorSubsystem> elevators;
    private final SubSystemSharedStateInterface sharedState;
    private boolean notified = false;
    private boolean emergency;

    /**
     * Creates an Elevator Control System
     */
    public ElevatorControlSystem() throws IOException, NotBoundException, InterruptedException {
        emergency = false;
        elevatorRequests = new ArrayList<>();
        elevators = new ArrayList<>();
        sharedState = (SubSystemSharedStateInterface) Naming.lookup("rmi://localhost/SharedSubSystemState");
        createElevators(3);

    }

    /**
     * Creates the elevators
     * @param numElevators The amount of elevators to create
     */
    private void createElevators(int numElevators) throws IOException, InterruptedException {
        HashMap<Integer, ElevatorStateUpdate> stateUpdate = new HashMap<>();
        for (int i = 1; i <= numElevators; i++){
            elevators.add(new ElevatorSubsystem(i, this));
            elevators.get(i - 1).start();
            elevators.get(i - 1).setName("Elevator" + i);
            sharedState.setWorkAssignmentQueue(i, new ConcurrentLinkedDeque<>());
            stateUpdate.put(i , elevators.get(i - 1).getElevatorInfo());
            sharedState.addElevatorState(i, elevators.get(i - 1).getElevatorInfo());
        }
        notified = sharedState.ecsUpdate(stateUpdate);
    }

    /**
     * Assigns Requests to elevators
     */
    private void AssignRequest() throws RemoteException {
        for (ElevatorSubsystem elevator : elevators) {
            for (WorkAssignment newRequest : sharedState.getWorkAssignments().get(elevator.getElevatorId())) {
                if (!elevatorRequests.contains(newRequest)) {
                    elevator.addTrackedRequest(newRequest);
                    elevatorRequests.add(newRequest);
                }
            }
        }
    }

    /**
     * Updates the schedulers with the elevator's states
     * @return If the scheduler has been notified
     */
    protected synchronized boolean updateScheduler() throws IOException, InterruptedException {
        HashMap<Integer, ElevatorStateUpdate> stateUpdate = new HashMap<>();
        for (ElevatorSubsystem elevator : elevators) {
            ElevatorStateUpdate elevatorStateUpdate = elevator.getElevatorInfo();
            stateUpdate.put(elevator.getElevatorId(), elevatorStateUpdate);
            sharedState.addElevatorState(elevator.getElevatorId(),elevatorStateUpdate);
//            System.out.println(elevator.getElevatorId() + " " + elevatorStateUpdate.getWorkAssignments());
//            if (stateUpdate.get(elevator.getElevatorId()).getStateSignal() != Signal.IDLE)
            sharedState.setWorkAssignmentQueue(elevator.getElevatorId() , new ConcurrentLinkedDeque<>(elevatorStateUpdate.getWorkAssignments())); // elevatorStateUpdate.getWorkAssignments() -> 7 -> 4
//            else sharedState.setWorkAssignmentQueue(elevator.getElevatorId() , new ConcurrentLinkedDeque<>());
        }
//        System.out.println();
        return notified = sharedState.ecsUpdate(stateUpdate); // this method is inside scheduler
    }

    /**
     * Runs the system once
     */
    private void runSystem() throws InterruptedException, IOException {
        if (!emergency){
            if (notified) {
                AssignRequest();
            }
            // if unchanged, it'll keep notifying with same inf
            Thread.sleep(100);
            notified = updateScheduler(); // at some point you have 5 done and 5 undone -> "update" with this info -> 100ms later:
        }
    }

    /**
     * Declares an elevator broken. Will reassign given requests
     * @param elevatorId The broken elevator
     * @param requests The requests to reassign
     */
    public synchronized void emergencyState(int elevatorId, ArrayList<WorkAssignment> requests) throws RemoteException {
        emergency = true;
        notified = false;
        elevators.removeIf((elevatorSubsystem -> elevatorSubsystem.getElevatorId() == elevatorId));
        sharedState.removeWorkElevator(elevatorId);
        elevatorRequests.removeAll(requests);
        emergency = sharedState.ecsEmergency(requests);
        AssignRequest();
    }


    public static void main(String[] args) throws IOException, NotBoundException, InterruptedException {
        ElevatorControlSystem elevatorController = new ElevatorControlSystem();
        while (true) {
            elevatorController.runSystem();
        }
    }

}
