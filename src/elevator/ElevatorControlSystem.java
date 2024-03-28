package elevator;

import util.MessageBuffer;
import util.Messages.SerializableMessage;

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ElevatorControlSystem {

    // Some code to handle shared object

    private HashMap<Integer, ArrayList> elevatorRequests;
    private ElevatorSubsystem e1, e2, e3;
    private Thread elevator1, elevator2, elevator3;

    public ElevatorControlSystem() throws SocketException, UnknownHostException {
        elevatorRequests = new HashMap<>();
        MessageBuffer queue = new MessageBuffer("ElevatorQueue", new DatagramSocket(8081), new InetSocketAddress(InetAddress.getLocalHost(), 8080), 8080);
        queue.listenAndFillBuffer();
        ElevatorSubsystem e1 = new ElevatorSubsystem(queue, 1);
        elevator1 = new Thread(e1, "Elevator1");
    }

    private void AssignRequest() {
        SerializableMessage sm = null;
        e1.addTrackedRequest(null); // get from shared object
    }

    private void updateScheduler() {

    }

    public static void main(String[] args) throws SocketException, UnknownHostException {
        ElevatorControlSystem me = new ElevatorControlSystem();
        while (true) {
            me.updateScheduler();
        }
    }
}
