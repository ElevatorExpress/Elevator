import static org.junit.jupiter.api.Assertions.*;
import Messages.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

class SchedulerTest {

//    @org.junit.jupiter.api.Test
//    void serveElevatorReqs() {
//        MessageBuffer messageBuffer = new MessageBuffer(20);
//        MessageBuffer floorOutBuffer = new MessageBuffer(10);
//        MessageBuffer elevatorOutBuffer = new MessageBuffer(10);
//
//        Scheduler scheduler = new Scheduler(messageBuffer, floorOutBuffer, elevatorOutBuffer);
//
//        //Create an array of floor requests usring the floorMessageFactory
////        ArrayList<MessageInterface> floorRequests = new ArrayList<>();
////        for (int i = 0; i < 10; i++) {
////            MessageInterface floorRequest = FloorMessageFactory.createFloorMessage(UUID.randomUUID().toString(), null, Signal.WORK_REQ);
////
////            floorRequests.add(floorRequest);
////        }
//        MessageInterface[] elevatorRequests = new MessageInterface[10];
//        for (int i = 0; i < 10; i++) {
//            MessageInterface elevatorReq = ElevatorMessageFactory.createElevatorMessage(UUID.randomUUID().toString(), null, Signal.WORK_REQ);
//            elevatorRequests[i] = elevatorReq;
//        }
//        MessageInterface[] floorRequests = new MessageInterface[10];
//        for (int i = 0; i < 10; i++) {
//            MessageInterface floorRequest = FloorMessageFactory.createFloorMessage(UUID.randomUUID().toString(), null, Signal.WORK_REQ);
//            floorRequests[i] = floorRequest;
//        }
//
//        //Create an array of elevator requests using the elevatorMessageFactory
////        ArrayList<MessageInterface> elevatorRequests = new ArrayList<>();
////        for (int i = 0; i < 10; i++) {
////            MessageInterface elevatorReq = ElevatorMessageFactory.createElevatorMessage(UUID.randomUUID().toString(), null, Signal.WORK_REQ);
////            elevatorRequests.add(elevatorReq);
////        }
//
//
//        System.out.println("Floor Requests: " + floorRequests);
//        System.out.println("Elevator Requests: " + elevatorRequests);
//
//        //create a producer threads to add messages to each buffer
//        Thread floorProducerThread = new Thread(() -> {
//            try {
////                for (MessageInterface floorReq : floorRequests) {
////                    messageBuffer.put(new MessageInterface[]{floorReq});
////                }
//                messageBuffer.put(floorRequests);
//            } catch (Exception e) {
//                fail("Exception when adding messages to the buffer: " + e.getMessage());
//            }
//        });
//
//        Thread elevatorProducerThread = new Thread(() -> {
//            try {
////                for (MessageInterface elevatorReq : elevatorRequests) {
////                    messageBuffer.put(new MessageInterface[]{elevatorReq});//                }
//
//                messageBuffer.put(
//                        elevatorRequests
//                );
//            } catch (Exception e) {
//                fail("Exception when adding messages to the buffer: " + e.getMessage());
//            }
//        });
//
//        //Start scheduler thread
//        floorProducerThread.start();
//        scheduler.readBuffer();
//        elevatorProducerThread.start();
//        scheduler.serveElevatorReqs();
//        scheduler.readBuffer();
//        scheduler.serveFloorRequests();
//        System.out.println("Floor Out Buffer: " + floorOutBuffer.toString());
//    }

    @org.junit.jupiter.api.Test
    void serveElevator() {
        MessageBuffer messageBuffer = new MessageBuffer(20);
        MessageBuffer floorOutBuffer = new MessageBuffer(10);
        MessageBuffer elevatorOutBuffer = new MessageBuffer(10);

        Scheduler scheduler = new Scheduler(messageBuffer, floorOutBuffer, elevatorOutBuffer);

        //Create an array of floor requests usring the floorMessageFactory
//        ArrayList<MessageInterface> floorRequests = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            MessageInterface floorRequest = FloorMessageFactory.createFloorMessage(UUID.randomUUID().toString(), null, Signal.WORK_REQ);
//
//            floorRequests.add(floorRequest);
//        }
        ElevatorMessageFactory elevatorMessageFactory = new ElevatorMessageFactory();

        MessageInterface[] elevatorRequests = new MessageInterface[10];
        for (int i = 0; i < 10; i++) {
            MessageInterface elevatorReq = elevatorMessageFactory.createElevatorMessage(UUID.randomUUID().toString(), null, Signal.IDLE);
            elevatorRequests[i] = elevatorReq;
        }


        System.out.println("Elevator Requests: " + elevatorRequests);



        Thread elevatorProducerThread = new Thread(() -> {
            try {
                messageBuffer.put(
                        elevatorRequests
                );
            } catch (Exception e) {
                fail("Exception when adding messages to the buffer: " + e.getMessage());
            }
        });


        elevatorProducerThread.start();
        scheduler.readBuffer();
        scheduler.serveElevatorReqs();
        System.out.println("Floor Out Buffer: " + floorOutBuffer.toString());

        for (MessageInterface eMessage : elevatorRequests){
            String elevatorId = eMessage.getSenderID();
            assert(scheduler.getIdleElevators().containsKey(elevatorId));
            assert (scheduler.getIdleElevators().get(elevatorId).equals(eMessage));
        }

    }

    @org.junit.jupiter.api.Test
    void serveFloorRequests() {
        FloorMessageFactory floorMessageFactory = new FloorMessageFactory();
        MessageBuffer messageBuffer = new MessageBuffer(20);
        MessageBuffer floorOutBuffer = new MessageBuffer(10);
        MessageBuffer elevatorOutBuffer = new MessageBuffer(10);
        Scheduler scheduler = new Scheduler(messageBuffer, floorOutBuffer, elevatorOutBuffer);
        MessageInterface[] floorRequests = new MessageInterface[10];
        for (int i = 0; i < 10; i++) {
            MessageInterface floorReqs = floorMessageFactory.createFloorMessage(UUID.randomUUID().toString(), null, Signal.WORK_REQ);
            floorRequests[i] = floorReqs;
        }
        System.out.println("Elevator Requests: " + floorRequests);
        Thread floorProducerThread = new Thread(() -> {
            try {
                messageBuffer.put(
                        floorRequests
                );
            } catch (Exception e) {
                fail("Exception when adding messages to the buffer: " + e.getMessage());
            }
        });
        floorProducerThread.start();
        scheduler.readBuffer();
        scheduler.serveFloorRequests();
        for (MessageInterface fMessage : floorRequests){
            String floorId = fMessage.getSenderID();
            assert(scheduler.getFloorRequestBuffer().containsKey(floorId));
            assert (scheduler.getFloorRequestBuffer().get(floorId).equals(fMessage));
        }
    }

    @org.junit.jupiter.api.Test
    void testSchedulerAssignsWorkToElevators(){

        FloorMessageFactory floorMessageFactory = new FloorMessageFactory();
        MessageBuffer messageBuffer = new MessageBuffer(20);
        MessageBuffer floorOutBuffer = new MessageBuffer(10);
        MessageBuffer elevatorOutBuffer = new MessageBuffer(10);
        Scheduler scheduler = new Scheduler(messageBuffer, floorOutBuffer, elevatorOutBuffer);


        ElevatorMessageFactory elevatorMessageFactory = new ElevatorMessageFactory();

        MessageInterface[] elevatorRequests = new MessageInterface[10];
        for (int i = 0; i < 10; i++) {
            MessageInterface elevatorReq = elevatorMessageFactory.createElevatorMessage(UUID.randomUUID().toString(), null, Signal.IDLE);
            elevatorRequests[i] = elevatorReq;
        }


        System.out.println("Elevator Requests: " + elevatorRequests);



        Thread elevatorProducerThread = new Thread(() -> {
            try {
                messageBuffer.put(
                        elevatorRequests
                );
            } catch (Exception e) {
                fail("Exception when adding messages to the buffer: " + e.getMessage());
            }
        });


        MessageInterface[] floorRequests = new MessageInterface[10];
        for (int i = 0; i < 10; i++) {
            MessageInterface floorReqs = floorMessageFactory.createFloorMessage(UUID.randomUUID().toString(), null, Signal.WORK_REQ);
            floorRequests[i] = floorReqs;
        }


        System.out.println("Elevator Requests: " + floorRequests);
        Thread floorProducerThread = new Thread(() -> {
            try {
                messageBuffer.put(
                        floorRequests
                );
            } catch (Exception e) {
                fail("Exception when adding messages to the buffer: " + e.getMessage());
            }
        });

        floorProducerThread.start();
        scheduler.readBuffer();
        scheduler.serveFloorRequests();
        elevatorProducerThread.start();
        scheduler.readBuffer();
        scheduler.serveElevatorReqs();
        scheduler.serveFloorRequests();
//        scheduler.schedule();
//

        for (MessageInterface fMessage : floorRequests){
            String floorId = fMessage.getSenderID();
//            assert(scheduler.getFloorRequestBuffer().containsKey(floorId));
//            assert (scheduler.getFloorRequestBuffer().get(floorId).equals(fMessage));
        }
    }



}