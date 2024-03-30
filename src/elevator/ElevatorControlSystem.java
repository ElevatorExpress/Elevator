package elevator;

import util.ElevatorStateUpdate;
import util.Messages.Signal;
import util.SubSystemSharedStateInterface;
import util.WorkAssignment;

import java.io.IOException;
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ElevatorControlSystem {

    // Some code to handle shared object

    private ArrayList<WorkAssignment> elevatorRequests;
    private ArrayList<ElevatorSubsystem> elevators;
    private Thread elevator2;
    private Thread elevator3;
    private SubSystemSharedStateInterface sharedState;
    private boolean notified = false;

    public ElevatorControlSystem() throws IOException, NotBoundException, InterruptedException {
        elevatorRequests = new ArrayList<>();
        elevators = new ArrayList<>();
        sharedState = (SubSystemSharedStateInterface) Naming.lookup("rmi://localhost/SharedSubSystemState");
        createElevators(2);

    }
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

    protected void updateScheduler() throws IOException, InterruptedException {
        HashMap<Integer, ElevatorStateUpdate> stateUpdate = new HashMap<>();
        boolean flag = false;
        for (ElevatorSubsystem elevator : elevators) {
            ElevatorStateUpdate elevatorStateUpdate = elevator.getElevatorInfo();
            stateUpdate.put(elevator.getElevatorId(), elevatorStateUpdate);
            sharedState.addElevatorState(elevator.getElevatorId(),elevatorStateUpdate);
            sharedState.setWorkAssignmentQueue(elevator.getElevatorId() , new ConcurrentLinkedDeque<>(elevatorStateUpdate.getWorkAssignments())); // elevatorStateUpdate.getWorkAssignments() -> 7 -> 4
            if (stateUpdate.get(elevator.getElevatorId()).getStateSignal() != Signal.IDLE) flag = true;
        }
        if (flag) notified = sharedState.ecsUpdate(stateUpdate); // this method is inside scheduler
    }

    private void runSystem() throws InterruptedException, IOException {
        if (notified) {
            AssignRequest();
        }
        // if unchanged, it'll keep notifying with same info
//        Thread.sleep(100);
//        notified = updateScheduler(); // at some point you have 5 done and 5 undone -> "update" with this info -> 100ms later:
    }

    public static void main(String[] args) throws IOException, NotBoundException, InterruptedException {
        ElevatorControlSystem elevatorController = new ElevatorControlSystem();
        while (true) {
            elevatorController.runSystem();
        }
    }
}
