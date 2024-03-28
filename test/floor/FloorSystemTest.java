package floor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import util.MessageBuffer;
import util.Messages.MessageTypes;
import util.Messages.SerializableMessage;
import util.Messages.Signal;

import java.io.IOException;
import java.net.*;
import java.util.UUID;

import static util.MessageHelper.ReceiveMessage;
import static util.MessageHelper.SendMessage;

class FloorSystemTest {

    static FloorSystem floorSystem;

    @BeforeAll
    static void createSystem() {
        MessageBuffer COMM_BUFF;
        try {
            COMM_BUFF = new MessageBuffer(
                    "FloorTest",
                    new DatagramSocket(8082),
                    new InetSocketAddress(InetAddress.getLocalHost(), 8080),
                    8080
            );
            COMM_BUFF.listenAndFillBuffer();
            floorSystem = new FloorSystem(COMM_BUFF);
        } catch (SocketException | UnknownHostException ignored){}

    }

    @Test
    void sendMessage(){
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        }catch (UnknownHostException ignored){}
        FloorInfoReader.Data data = new FloorInfoReader.Data("4:12", "2", "up", "9", "0");

        assert addr != null;
        SerializableMessage sm =  new SerializableMessage(addr.getHostAddress(), 8081, Signal.WORK_REQ, MessageTypes.FLOOR, 2, UUID.randomUUID().toString(), UUID.randomUUID().toString(), data);

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
        floorSystem.sendMessage(new SerializableMessage[]{sm});
        try {
            t.join();
        } catch (InterruptedException ignore){}
    }

    @Test
    void startInteractions(){
        Thread t = new Thread(() -> {
            DatagramSocket receiveSocket = null;
            DatagramSocket sendSocket = null;
            DatagramPacket receivePacket = null;
            int count = 0;

            byte[] messageArray = new byte[1024];
            try {
                receiveSocket = new DatagramSocket(8080);
                sendSocket = new DatagramSocket();
                receivePacket = new DatagramPacket(messageArray, messageArray.length);
            } catch (Exception ignored){}

            assert receiveSocket != null;
            assert sendSocket != null;
            while (count < 10) {
                SerializableMessage msg = ReceiveMessage(receiveSocket, messageArray, receivePacket);
                SerializableMessage done = new SerializableMessage(msg.senderAddr(), 8081, Signal.DONE, MessageTypes.ELEVATOR, 1, UUID.randomUUID().toString(), msg.reqID(), msg.data());
                try {
                    SendMessage(sendSocket, done, InetAddress.getLocalHost(), 8082);
                } catch (IOException ignored) {}
                count++;
            }
            receiveSocket.close();
        });
        t.start();
        floorSystem.startFloorInteractions();
        try {
            t.join();
        } catch (InterruptedException ignored) {}
    }

}