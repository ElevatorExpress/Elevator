import floor.FloorInfoReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import util.Messages.MessageTypes;
import util.Messages.SerializableMessage;
import util.Messages.Signal;
import elevator.ElevatorSubsystem;
import org.junit.jupiter.api.Test;
import util.MessageBuffer;

import java.io.IOException;
import java.net.*;
import java.util.UUID;

import static util.MessageHelper.ReceiveMessage;
import static util.MessageHelper.SendMessage;

class ElevatorSubsystemTest {

    static ElevatorSubsystem elevator;

    @BeforeAll
    static void createElevatorSystem() {
        MessageBuffer queue;
        try {
            queue = new MessageBuffer(
                    "ElevatorTest",
                    new DatagramSocket(8081),
                    new InetSocketAddress(InetAddress.getLocalHost(), 8080),
                    8080
            );
            queue.listenAndFillBuffer();
            elevator = new ElevatorSubsystem(queue, 1);
        } catch (SocketException | UnknownHostException  ignored) {}

    }

    @Test
    void sendMessage(){
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        }catch (UnknownHostException ignored){}
        FloorInfoReader.Data data = new FloorInfoReader.Data("4:12", "2", "up", "9");

        assert addr != null;
        SerializableMessage sm =  new SerializableMessage(addr.getHostAddress(), 8081, Signal.DONE, MessageTypes.ELEVATOR, 1, null, UUID.randomUUID().toString(), data);

        Thread t = new Thread(() -> {
            try {
                byte[] messageArray = new byte[1024];
                DatagramSocket receiveSocket = new DatagramSocket(8080);
                DatagramPacket receivePacket = new DatagramPacket(messageArray, messageArray.length);
                SerializableMessage msg = ReceiveMessage(receiveSocket, messageArray, receivePacket);
                Assertions.assertEquals(sm, msg);
                receiveSocket.close();
            } catch (Exception ignored){}
        });

        t.start();
        elevator.sendMessage(Signal.DONE, sm);
        try {
            t.join();
        } catch (InterruptedException ignore){}
    }

    @Test
    void receiveMessage() {
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        }catch (UnknownHostException ignored){}

        assert addr != null;
        FloorInfoReader.Data data = new FloorInfoReader.Data("4:12", "2", "up", "9");
        SerializableMessage sm =  new SerializableMessage(addr.getHostAddress(), 8082, Signal.WORK_REQ, MessageTypes.FLOOR, 1, UUID.randomUUID().toString(), UUID.randomUUID().toString(), data);

        Thread t = new Thread(() -> elevator.receiveMessage());

        t.start();
        try {
            DatagramSocket sendSocket = new DatagramSocket();
            SendMessage(sendSocket, sm, InetAddress.getLocalHost(), 8081);
            t.join();
        } catch (InterruptedException | IOException ignored){}
    }
}