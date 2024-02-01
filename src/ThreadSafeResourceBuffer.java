import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ThreadSafeResourceBuffer {

    private boolean bufferEmpty = true;


    private final ResourceType[] resourceBuffer;

    private final Map<ResourceType,ResourceType> availableResources = new HashMap<ResourceType,ResourceType>();

    public ThreadSafeResourceBuffer(int size) {
        this.resourceBuffer = new ResourceType[size];
    }

    public boolean isBufferEmpty(){
        return bufferEmpty;
    }

    public synchronized ResourceType[] get(ResourceType r) {
        while (!excludesUneededType(r) || bufferEmpty) {
            try {
//                notifyAll();
                wait();
            } catch (InterruptedException e) {
                System.err.println("Producer ERROR: " + e.getMessage());
            }
        }
            ResourceType[] resources = new ResourceType[2];
            System.arraycopy(resourceBuffer, 0, resources, 0, 2);
            resourceBuffer[0] = null;
            resourceBuffer[1] = null;
            availableResources.clear();

            bufferEmpty = true;
            notifyAll();
            return resources;

    }

    public synchronized void put(ResourceType[] resources) {

            while (!bufferEmpty) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.err.println("Producer ERROR: " + e.getMessage());
                }
            }
            System.arraycopy(resources, 0, resourceBuffer, 0, 2);
            availableResources.put(resources[0], resources[0]);
            availableResources.put(resources[1], resources[1]);
            bufferEmpty = false;
            notifyAll();
    }


    public synchronized Boolean excludesUneededType(ResourceType r) {
        return !availableResources.containsKey(r) && !bufferEmpty;
    }


}

