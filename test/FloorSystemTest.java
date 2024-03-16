import floor.FloorInfoReader;
import floor.FloorSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.MessageBuffer;
import util.Messages.MessageTypes;
import util.Messages.Signal;

import java.io.*;
import java.net.*;
import java.util.UUID;

class FloorSystemTest {

    MessageBuffer COMM_BUFF;
    FloorSystem floorSystem;

    @BeforeEach
    void createSystem() throws SocketException, UnknownHostException {
        COMM_BUFF = new MessageBuffer(
                1, // Obsolete
                "FloorSystem ->",
                new DatagramSocket(8082),
                new InetSocketAddress("localhost", 8080),
                8080
        );

        floorSystem = new FloorSystem(COMM_BUFF);
    }

    @Test
    void startFloorInteractions() throws IOException, InterruptedException {
        Thread t = new Thread(() -> {
            try {
                byte[] buff1 = new byte[1024];
                DatagramSocket recv = new DatagramSocket(8080);
                DatagramPacket pack = new DatagramPacket(buff1, buff1.length);

                SerializableMessage msg = ReceiveMessage(recv, buff1, pack);
                System.out.println(msg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        t.start();

        Thread.sleep(1000);

        DatagramSocket send = new DatagramSocket();
        FloorInfoReader.Data data = new FloorInfoReader.Data(
                "1",
                "2",
                "3",
                "4"
        );

        //Create a request object with the above info
        SerializableMessage request = new SerializableMessage(
                "localhost",
                8082,
                Signal.WORK_REQ,
                MessageTypes.FLOOR,
                1,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                data
        );
        SendMessage(send, request, InetAddress.getLocalHost(), 8080);
        t.join();
    }

    public static SerializableMessage ReceiveMessage(DatagramSocket socket, byte[] buffer, DatagramPacket packet) {

        while (true) {
            System.out.println("Waiting for packet");
            try {
                socket.receive(packet);
                try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer, 0, packet.getLength()))) {
                    return (SerializableMessage) ois.readObject();

                } catch (Exception e) {
                    System.err.println("Error during deserialization: " + e.getMessage());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void SendMessage(DatagramSocket socket, SerializableMessage message, InetAddress addr, int port) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)){
            oos.writeObject(message);
            oos.flush();
            byte[] buffer = bos.toByteArray();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, addr, port);
            socket.send(packet);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public record SerializableMessage(String senderAddr, int senderPort, Signal signal, MessageTypes type, int senderID, String messageID, String reqID, FloorInfoReader.Data data) implements Serializable {
    }


}