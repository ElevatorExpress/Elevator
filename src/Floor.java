import java.util.Arrays;
import java.util.HashMap;

public class Floor implements Runnable{
    private final ResourceType specialtyIngredient;

    private final HashMap<ResourceType, ResourceType[]> resourceMap;

    private final ThreadSafeResourceBuffer threadSafeResourceBuffer;

    private int cofeeGoal = 0;

    private final UtilityInterface<Integer> coffeeCounter;


    public Floor(ResourceType specialtyIngredient, ThreadSafeResourceBuffer threadSafeResourceBuffer, UtilityInterface<Integer> coffeeCounter) {
        this.coffeeCounter = coffeeCounter;
        this.specialtyIngredient = specialtyIngredient;

        resourceMap = new HashMap<>();

        Arrays.stream(ResourceType.values())
                .filter(type -> type != specialtyIngredient)
                .forEach(type -> resourceMap.put(type, new ResourceType[]{null}));
        this.threadSafeResourceBuffer = threadSafeResourceBuffer;
    }
    public void setCoffeeGoal(int coffeeGoal){
        this.cofeeGoal = coffeeGoal;
    }


    public void makeCoffee() throws InterruptedException {
        int cCounter = coffeeCounter.get();
//        boolean done = false;
        while (true) {

            ResourceType[] resources = threadSafeResourceBuffer.get(specialtyIngredient);
            cCounter = coffeeCounter.put(1);
            if (cCounter > cofeeGoal) {
                break;
            }
            StringBuilder sb = new StringBuilder();
            for (ResourceType r : resources) {
                sb.append(r).append(", ");
            }
            sb.delete(sb.length() - 2, sb.length());


            System.out.println("Floor made coffee number: " + cCounter + " with: " + sb + " from BeanMaster and " + specialtyIngredient + " from " + specialtyIngredient + " Floor");

        }
    }



    public void run(){
        try {
            makeCoffee();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }


}
