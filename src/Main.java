//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

//import java.util.concurrent.TimeUnit;

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

        Thread floorSystemThread = new Thread(floorSystem, "FloorSystem");
        Thread elevatorSubsystemThread = new Thread(elevatorSubsystem, "ElevatorSubsystem");
        Thread schedulerThread = new Thread(scheduler, "Scheduler");


        elevatorSubsystemThread.start();
        floorSystemThread.start();
        //TimeUnit.SECONDS.sleep(1);
        schedulerThread.start();
        System.out.println("Threads Started");


    }
}
