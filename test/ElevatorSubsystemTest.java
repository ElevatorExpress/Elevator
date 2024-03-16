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

    static MessageBuffer queue;
    static ElevatorSubsystem elevator;
    static DatagramSocket socket;

    @BeforeAll
    static void createElevatorSystem() {
        InetAddress addr;
        try {
            addr = InetAddress.getLocalHost();
            socket = new DatagramSocket(8081);
            queue = new MessageBuffer(
                    1,
                    "ElevatorTest",
                    socket,
                    new InetSocketAddress(addr, 8080),
                    8080
            );
        } catch (SocketException | UnknownHostException  ignored) {}
        queue.listenAndFillBuffer();
        elevator = new ElevatorSubsystem(queue, 1);
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
                DatagramSocket reviveSocket = new DatagramSocket(8080);
                DatagramPacket receivePacket = new DatagramPacket(messageArray, messageArray.length);
                SerializableMessage msg = ReceiveMessage(reviveSocket, messageArray, receivePacket);
                Assertions.assertEquals(sm, msg);
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