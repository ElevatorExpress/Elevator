import java.util.Arrays;
import java.util.HashMap;

public class Floor implements Runnable{
    private final ResourceType specialtyIngredient;

    private final HashMap<ResourceType, ResourceType[]> resourceMap;

    private final Scheduler threadSafeSchedulerShared;

    private int cofeeGoal = 0;


    public Floor(ResourceType specialtyIngredient, Scheduler threadSafeSchedulerShared) {

        this.specialtyIngredient = specialtyIngredient;
        resourceMap = new HashMap<>();
        Arrays.stream(ResourceType.values())
                .filter(type -> type != specialtyIngredient)
                .forEach(type -> resourceMap.put(type, new ResourceType[]{null}));
        this.threadSafeSchedulerShared = threadSafeSchedulerShared;
    }
    public void setCoffeeGoal(int coffeeGoal){
        this.cofeeGoal = coffeeGoal;
    }
    public void makeCoffee(){
        for (ResourceType r : resourceMap.keySet()) {
            if(resourceMap.get(r)[0] == null){
                resourceMap.get(r)[0] = threadSafeSchedulerShared.getResource(r);
            }
        }


        StringBuilder sb = new StringBuilder();
        for (ResourceType[] r : resourceMap.values()) {
            for (ResourceType resourceType : r) {
                sb.append(resourceType).append(", ");
            }
        }
        sb.delete(sb.length()-2,sb.length());



        for (ResourceType r : resourceMap.keySet()) {
            resourceMap.get(r)[0] = null;
        }

        int coffeeNumber = threadSafeSchedulerShared.incrementCoffeeCounter();

        if(coffeeNumber <= cofeeGoal){
            System.out.println("Floor made coffee number: " + coffeeNumber + " with: " + sb + " from BeanMaster and " + specialtyIngredient + " from " + specialtyIngredient +" Floor");
        }

    }




    public void run(){
        while(threadSafeSchedulerShared.getCoffeeCounter() < cofeeGoal) makeCoffee();

    }


}
