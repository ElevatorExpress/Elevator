public class ThreadSafeCounterUtility implements UtilityInterface<Integer> {

    private volatile int counter;

    public ThreadSafeCounterUtility(int counter) {
        this.counter = counter;
    }
    public synchronized Integer get() {
        return this.counter;
    }

    public synchronized Integer put(Integer val) {
        this.counter += val;
        return this.counter;
    }

}
