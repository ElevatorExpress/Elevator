import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Scheduler implements Runnable {
    private final ThreadSafeResourceBuffer resourceBuffer = new ThreadSafeResourceBuffer(2);
    private UtilityInterface<Integer> cofeeCounter;
    private final int coffeeGoal;

    private final Floor[] floors = new Floor[3];


    private final ResourceType[] resourceTypes = ResourceType.values();
    public boolean checkBufferContainsRequired(ResourceType r){
        return !resourceBuffer.excludesUneededType(r);
    }


    private void loadResources() throws InterruptedException {
            while (cofeeCounter.get() <= coffeeGoal) {
                Collections.shuffle(Arrays.asList(resourceTypes));

                resourceBuffer.put(Arrays.copyOfRange(resourceTypes, 0, 2));

            }
        }




    public synchronized int getCoffeeCounter(){
        return cofeeCounter.get();
    }

    public Scheduler(int amount){
        this.coffeeGoal = amount;
        this.cofeeCounter = new ThreadSafeCounterUtility(0);
        List<Integer> randIndex = new ArrayList<>(Arrays.asList(0, 1, 2));
        Collections.shuffle(randIndex);
        for (int i = 0; i < 3; i++) {
            this.floors[i] = new Floor(ResourceType.values()[randIndex.get(i)],  resourceBuffer, cofeeCounter);
        }
        System.out.println("BARISTAS: " + Arrays.toString(floors));
    }



    public void makeCoffee() throws InterruptedException {


        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (Floor floor : floors) {
            floor.setCoffeeGoal(coffeeGoal);
            executor.execute(floor);
        }
        System.out.println("Scheduler made coffees4");
        System.out.println("COFFEE COUNTER: "+cofeeCounter.get());


            loadResources();

        }




    @Override
    public void run() {
        try {
            makeCoffee();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
