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

/**
 * Class for managing elevators
 * @author Yasir Sheikh
 */
public class ElevatorControlSystem {

    //List of elevator requests
    private final ArrayList<WorkAssignment> elevatorRequests;
    private final ArrayList<ElevatorSubsystem> elevators;
    private final SubSystemSharedStateInterface sharedState;
    private boolean notified = false;
    //If there is a fault on an elevator
    private boolean emergency;

    /**
     * Creates an Elevator Control System
     */
    public ElevatorControlSystem(int numElevators) throws IOException, NotBoundException, InterruptedException {
        //No fault by default
        emergency = false;
        elevatorRequests = new ArrayList<>();
        elevators = new ArrayList<>();
        //Grab state from shared object
        sharedState = (SubSystemSharedStateInterface) Naming.lookup("rmi://localhost/SharedSubSystemState");
        //Create individual elevators
        createElevators(numElevators);

    }

    /**
     * Creates the elevators
     * @param numElevators The amount of elevators to create
     */
    private void createElevators(int numElevators) throws IOException, InterruptedException {
        HashMap<Integer, ElevatorStateUpdate> stateUpdate = new HashMap<>();
        //For each elevator
        for (int i = 1; i <= numElevators; i++){
            //Add to elevator list
            elevators.add(new ElevatorSubsystem(i, this));
            //Start the elevator
            elevators.get(i - 1).start();
            elevators.get(i - 1).setName("Elevator" + i);
            //Create work list for elevator
            sharedState.setWorkAssignmentQueue(i, new ConcurrentLinkedDeque<>());
            //Add elevator state to update object
            stateUpdate.put(i , elevators.get(i - 1).getElevatorInfo());
            sharedState.addElevatorState(i, elevators.get(i - 1).getElevatorInfo());
        }
        //Update shared object
        notified = sharedState.ecsUpdate(stateUpdate);
    }

    /**
     * Assigns Requests to elevators
     */
    private void AssignRequest() throws RemoteException {
        //Each elevator looks into the request queue
        for (ElevatorSubsystem elevator : elevators) {
            //Checks each request in the WorkAssignment list that is tied to the current elevator ID
            for (WorkAssignment newRequest : sharedState.getWorkAssignments().get(elevator.getElevatorId())) {
                //If the request does not already exist
                if (!elevatorRequests.contains(newRequest)) {
                    //Give the request to the elevator
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
            //Gets info from elevator
            ElevatorStateUpdate elevatorStateUpdate = elevator.getElevatorInfo();
            //Fill the map with elevator id/state and request list pairs
            stateUpdate.put(elevator.getElevatorId(), elevatorStateUpdate);
            sharedState.addElevatorState(elevator.getElevatorId(),elevatorStateUpdate);
            sharedState.setWorkAssignmentQueue(elevator.getElevatorId() , new ConcurrentLinkedDeque<>(elevatorStateUpdate.getWorkAssignments()));
        }
        //RMI to the scheduler, it will process these requests and elevator information
        return notified = sharedState.ecsUpdate(stateUpdate);
    }

    /**
     * Runs the system once
     */
    private void runSystem() throws InterruptedException, IOException {
        if (!emergency){
            if (notified) {
                AssignRequest();
            }
            // Waits 100 ms before it tries to update the scheduler
            Thread.sleep(100);
            notified = updateScheduler();
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
        //Remove unserviced requests from the stopped elevator
        elevators.removeIf((elevatorSubsystem -> elevatorSubsystem.getElevatorId() == elevatorId));
        //Remove the stopped elevator from the scheduler
        sharedState.removeWorkElevator(elevatorId);
        //Remove the requests
        elevatorRequests.removeAll(requests);
        //Reallocates the requests that got removed
        emergency = sharedState.ecsEmergency(requests);
        AssignRequest();
    }


    public static void main(String[] args) throws IOException, NotBoundException, InterruptedException {
        ElevatorControlSystem elevatorController = new ElevatorControlSystem(3);
        //The 100 ms wait causes this method to execute repeated with a small delay
        while (true) {
            elevatorController.runSystem();
        }
    }

}
