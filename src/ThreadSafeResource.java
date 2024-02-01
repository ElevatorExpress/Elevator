public class ThreadSafeResource {
    private final ResourceType type;
    private boolean bufferEmpty = false;
    private final ResourceType[] resourceBuffer;

    public ThreadSafeResource(ResourceType type, int size) {
        this.type = type;
        this.resourceBuffer = new ResourceType[size];
        resourceBuffer[0] = ResourceType.valueOf(type.toString());
    }

    public ResourceType get() {
        synchronized (this) {
            while (bufferEmpty) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.err.println("Consumer ERROR: " + e.getMessage());
                }
            }
            ResourceType resource = resourceBuffer[0];
            resourceBuffer[0] = null;
            bufferEmpty = true;
            notifyAll();
            put();
            return resource;
        }
    }

    public void put() {
        synchronized (this) {
            while (!bufferEmpty) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.err.println("Producer ERROR: " + e.getMessage());
                }
            }
            resourceBuffer[0] = ResourceType.valueOf(type.toString());
            bufferEmpty = false;
            notifyAll();
        }
    }


    public ResourceType getType() {
        return type;
    }


}

