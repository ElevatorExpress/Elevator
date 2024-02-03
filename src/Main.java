//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.util.concurrent.TimeUnit;

public class Main {
    public Main() {
    }

    public static void main(String[] args) throws InterruptedException {
//        NotScheduler threadSafeNotSchedulerShared = new NotScheduler(20);
//        MessageBuffer threadSafeResourceBuffer = new MessageBuffer(2);
////        threadSafeNotSchedulerShared.makeCoffee();
//        threadSafeNotSchedulerShared.run();
        MessageBuffer messageBuffer = new MessageBuffer(300);
        MessageBuffer floorOutBuffer = new MessageBuffer(100);
        MessageBuffer elevatorOutBuffer = new MessageBuffer(100);
        FloorSystem floorSystem = new FloorSystem(floorOutBuffer, messageBuffer);
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(messageBuffer,elevatorOutBuffer );
        Scheduler scheduler = new Scheduler(messageBuffer, floorOutBuffer, elevatorOutBuffer);

        Thread floorSystemThread = new Thread(floorSystem);
        Thread elevatorSubsystemThread = new Thread(elevatorSubsystem);
        Thread schedulerThread = new Thread(scheduler);


//        floorSystemThread.start();
        elevatorSubsystemThread.start();
        TimeUnit.SECONDS.sleep(1);
        schedulerThread.start();
        System.out.println("Threads Started");
        TimeUnit.SECONDS.sleep(5);
    }
}
