/**
 * Crestes all the threads for the elevator and gets them running
 */
public class Main {
    public Main() {
    }

    public static void main(String[] args) {
        MessageBuffer messageBuffer = new MessageBuffer(30, " TO SCHED BUFFER");
        MessageBuffer floorOutBuffer = new MessageBuffer(30, " TO FLOOR BUFFER ");
        MessageBuffer elevatorOutBuffer = new MessageBuffer(30, " TO ELEVATOR BUFFER ");
        FloorSystem floorSystem = new FloorSystem(floorOutBuffer, messageBuffer);
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(messageBuffer,elevatorOutBuffer );
        Scheduler scheduler = new Scheduler(messageBuffer, floorOutBuffer, elevatorOutBuffer);

        //Create the threads
        Thread floorSystemThread = new Thread(floorSystem, "FloorSystem");
        Thread elevatorSubsystemThread = new Thread(elevatorSubsystem, "ElevatorSubsystem");
        Thread schedulerThread = new Thread(scheduler, "Scheduler");

        //Start the threads
        elevatorSubsystemThread.start();
        floorSystemThread.start();
        schedulerThread.start();
        System.out.println("Threads Started");
    }
}
