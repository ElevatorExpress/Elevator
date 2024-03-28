package scheduler;

import org.junit.runner.Runner;
import util.SubSystemSharedState;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class SchedulerV2 implements Runnable {
    private SubSystemSharedState sharedState;
    Registry registry;


    public SchedulerV2(SubSystemSharedState sharedState) throws RemoteException, AlreadyBoundException {
        this.sharedState = sharedState;
        registry = LocateRegistry.getRegistry();
        registry.bind("SharedSubSystemState", sharedState);
    }


    public void schedule(){
        // Grab the state, then make assignents.

    }
    public void run () {
        while (true) {
            // do something

        }
    }
}
