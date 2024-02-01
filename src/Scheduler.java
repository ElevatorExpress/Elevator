import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Scheduler {
    private final ThreadSafeResource beans = new ThreadSafeResource(ResourceType.BEANS, 1);
    private final ThreadSafeResource milk = new ThreadSafeResource(ResourceType.MILK, 1);
    private final ThreadSafeResource sugar = new ThreadSafeResource(ResourceType.SUGAR, 1);
    private int coffeeCounter = 0;
    private int coffeeGoal = 0;
    private final Floor[] floors = new Floor[3];

    public ResourceType getResource(ResourceType r){
        return switch (r) {
            case BEANS -> beans.get();
            case MILK -> milk.get();
            case SUGAR -> sugar.get();
            default -> null;
        };
    }

    public synchronized int incrementCoffeeCounter(){
        coffeeCounter++;
        if (coffeeCounter == coffeeGoal){
            notifyAll();
        }
        return coffeeCounter;
    }

    public synchronized int getCoffeeCounter(){
        return coffeeCounter;
    }

    public void init(){

        List<Integer> randIndex = new ArrayList<>(Arrays.asList(0, 1, 2));
        Collections.shuffle(randIndex);
        for (int i = 0; i < 3; i++) {
            this.floors[i] = new Floor(ResourceType.values()[randIndex.get(i)], this);
        }
        System.out.println("BARISTAS: " + Arrays.toString(floors));
    }



    public synchronized void makeCoffee(int amount) {
        coffeeGoal = amount;

        ExecutorService executor = Executors.newFixedThreadPool(3);
            for (Floor floor : floors) {
                floor.setCoffeeGoal(amount);
                executor.execute(floor);
            }
//            TimeUnit.SECONDS.sleep(3);
            System.out.println("Scheduler made coffees");
            executor.shutdown();
        }


}
