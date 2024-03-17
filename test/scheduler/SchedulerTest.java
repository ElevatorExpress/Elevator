package scheduler;

import floor.FloorInfoReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.MessageHelper;
import util.Messages.MessageTypes;
import util.Messages.SerializableMessage;
import util.Messages.Signal;

import java.io.IOException;
import java.net.*;
import java.util.UUID;

class SchedulerTest {

    static Scheduler scheduler;
    static Thread schedulerStartSystemThread;

    @BeforeEach
    void setUpTestEnvironment() throws UnknownHostException {
        scheduler = new Scheduler(InetAddress.getLocalHost(), 8090, 8091, 8092);
        schedulerStartSystemThread = new Thread(scheduler::startSystem);
    }

    static void sendElevatorRequest(int id, Signal signal) throws IOException {
        DatagramSocket elevatorPortSendSocket = new DatagramSocket();
        SerializableMessage elevatorMessage = new SerializableMessage(
                "localhost",
                8091,
                signal,
                MessageTypes.ELEVATOR,
                id,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                null
        );
        MessageHelper.SendMessage(elevatorPortSendSocket, elevatorMessage, InetAddress.getLocalHost(), 8090);
        elevatorPortSendSocket.close();
    }

    private static void sendFloorReq() throws IOException {
        DatagramSocket floorPortSendSocket = new DatagramSocket();
        String uuid = UUID.randomUUID().toString();
        SerializableMessage floorMessage = new SerializableMessage(
                "localhost",
                8092,
                Signal.WORK_REQ,
                MessageTypes.FLOOR,
                1,
                uuid,
                uuid,
                new FloorInfoReader.Data("1", "1", "up", "1")
        );

        MessageHelper.SendMessage(floorPortSendSocket, floorMessage, InetAddress.getLocalHost(), 8090);
        floorPortSendSocket.close();
    }

    @Test
    void readBuffer() throws IOException, InterruptedException {
        schedulerStartSystemThread.start();

        // Message from floor port
        sendFloorReq();
        Thread.sleep(200);

        // Message from Elevator
        sendElevatorRequest(1, Signal.IDLE);
        scheduler.testStopBit = false;

        schedulerStartSystemThread.join(2000);
        Assertions.assertFalse(schedulerStartSystemThread.isAlive());
    }

    @Test
    void sendToElevator() throws IOException, InterruptedException {
        schedulerStartSystemThread.start();

        Thread elevatorCatcher = new Thread(() -> {
            byte[] buff = new byte[1024];
            try {
                DatagramSocket recv = new DatagramSocket(8091);
                DatagramPacket recvPacket = new DatagramPacket(buff, buff.length);
                SerializableMessage m = MessageHelper.ReceiveMessage(recv, buff, recvPacket);
                recv.close();
                Assertions.assertNotNull(m);
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        });
        elevatorCatcher.start();

        sendElevatorRequest(1, Signal.IDLE);
        Thread.sleep(200);
        sendFloorReq();
        scheduler.testStopBit = false;


        elevatorCatcher.join(2000);
        Assertions.assertFalse(elevatorCatcher.isAlive());

        schedulerStartSystemThread.join(2000);
        Assertions.assertFalse(schedulerStartSystemThread.isAlive());
    }

    @Test
    void sendToFloor() throws IOException, InterruptedException {
        schedulerStartSystemThread.start();

        Thread floorCatcher = new Thread(() -> {
            byte[] buff = new byte[1024];
            try {
                DatagramSocket recv = new DatagramSocket(8092);
                DatagramPacket recvPacket = new DatagramPacket(buff, buff.length);
                SerializableMessage m = MessageHelper.ReceiveMessage(recv, buff, recvPacket);
                recv.close();
                Assertions.assertNotNull(m);
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        });
        floorCatcher.start();

        Thread.sleep(200);
        scheduler.testStopBit = false;
        sendElevatorRequest(1, Signal.DONE);

        floorCatcher.join(2000);
        Assertions.assertFalse(floorCatcher.isAlive());

        schedulerStartSystemThread.join(2000);
        Assertions.assertFalse(schedulerStartSystemThread.isAlive());

    }

}