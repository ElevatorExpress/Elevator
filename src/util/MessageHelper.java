package util;

import util.Messages.SerializableMessage;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class MessageHelper {

    /**
     * Receives and deserializes a message
     * @param socket The Socket to receive a message from
     * @param buffer The buffer to store the message
     * @param packet The Packet to store the message
     * @return The deserialized message object
     */
    public static SerializableMessage RecieveMessage(DatagramSocket socket, byte[] buffer, DatagramPacket packet) {

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

    /**
     * Serializes and sends messages
     * @param socket The socket used to send a message
     * @param messages The messages to be sent
     * @param addr The target address
     * @param port The target port
     */
    public static void SendMessages(DatagramSocket socket, ArrayList<SerializableMessage> messages, InetAddress addr, int port) {
        Thread t = new Thread(() -> {
            try {
                synchronized (socket){
                    for (SerializableMessage message : messages) {
                        SendMessage(socket, message, addr,port);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

    /**
     * Serializes and sends a message
     * @param socket The socket used to send a message
     * @param message The message to be sent
     * @param addr The target address
     * @param port The target port
     */
    public static void SendMessage(DatagramSocket socket, SerializableMessage message,InetAddress addr, int port) throws IOException {
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
}
