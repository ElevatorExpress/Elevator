import java.util.Arrays;
import java.util.HashMap;

public class Barista implements Runnable{
    private final ResourceType specialtyIngredient;

    private final HashMap<ResourceType, ResourceType[]> resourceMap;

    private final BeanMaster threadSafeBeanMasterShared;

    private int cofeeGoal = 0;


    public Barista(ResourceType specialtyIngredient, BeanMaster threadSafeBeanMasterShared) {

        this.specialtyIngredient = specialtyIngredient;
        resourceMap = new HashMap<>();
        Arrays.stream(ResourceType.values())
                .filter(type -> type != specialtyIngredient)
                .forEach(type -> resourceMap.put(type, new ResourceType[]{null}));
        this.threadSafeBeanMasterShared = threadSafeBeanMasterShared;
    }
    public void setCoffeeGoal(int coffeeGoal){
        this.cofeeGoal = coffeeGoal;
    }
    public void makeCoffee(){
        for (ResourceType r : resourceMap.keySet()) {
            if(resourceMap.get(r)[0] == null){
                resourceMap.get(r)[0] = threadSafeBeanMasterShared.getResource(r);
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

        int coffeeNumber = threadSafeBeanMasterShared.incrementCoffeeCounter();

        if(coffeeNumber <= cofeeGoal){
            System.out.println("Barista made coffee number: " + coffeeNumber + " with: " + sb + " from BeanMaster and " + specialtyIngredient + " from " + specialtyIngredient +" Barista");
        }

    }




    public void run(){
        makeCoffee();

    }


}
