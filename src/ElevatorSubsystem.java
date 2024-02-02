import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ElevatorSubsystem implements Runnable {
    private ResourceType specialtyIngredient; // the ingredient owned by barista
    private final HashMap<ResourceType, ResourceType[]> resourceMap; // something idk rn
    private final ThreadSafeResourceBuffer threadSafeResourceBuffer; // the 'counter' (put and get methods)
    private Map.Entry<Integer, String> lamp ; // some elevator button


    public ElevatorSubsystem(ResourceType specialtyIngredient, ThreadSafeResourceBuffer threadSafeResourceBuffer, UtilityInterface<Integer> coffeeCounter) {
        resourceMap = new HashMap<>();
        lamp = new AbstractMap.SimpleEntry<>(0, "Off");
        Arrays.stream(ResourceType.values())
                .filter(type -> type != specialtyIngredient)
                .forEach(type -> resourceMap.put(type, new ResourceType[]{null}));
        this.threadSafeResourceBuffer = threadSafeResourceBuffer;
    }

    private void setLamp(Integer floorButton){
        if (floorButton == null) {
            lamp = new AbstractMap.SimpleEntry<>(floorButton, "Off");
        } else {
            lamp = new AbstractMap.SimpleEntry<>(floorButton, "On");
        }

    }

    /*get work
        -> does nothing just gets the tasks
        -> prints "received tasks from scheduler"

     perform work
        -> case 0:
            -> assume starting at floor 0
                -> is this info held in the lamp (model is stupid but works assuming that elevator always reached floor on the lamp)
            -> button press scenarios
                -> button press is the current floor for both elevator and floor button (direction is void in this case)
                    -> Skip sleep
                    -> print saying current floor
                -> button press is a new floor. elevator and floor button are equal (direction must match the dest floor. ie elevator can only go up)
                    -> set the lamp to on + the floor dest
                    -> sleep (this is elevator going up)
                    -> print elevator arrived at floor  (request from floor button and elevator button)
                -> button is a new floor. elevator and floor button are not equal (direction must match the dest floor. ele go up)
                    -> set the lamp to on + the floor dest
                    -> sleep (this is elevator going up)
                    -> print elevator arrived at floor (request from floor button or elevator button) (do i signal here)
                    -> sleep (this ele going up)
                    -> print elevator arrived at floor (request from floor button or elevator button) (I def signal after, but for both or just one)
        -> case 1:
            -> starting floor is top floor
                 -> is this info held in the lamp (model is stupid but works assuming that elevator always reached floor on the lamp)
                 -> maybe a CONST for top floor???
            -> button press scenarios
                -> button press is the current floor for both ele and floor button (direction is void in this case)
                    -> Skip sleep
                    -> print saying current floor
                -> button press is a new floor and ele and floor button are equal (direction must match the dest floor. ie elevator can only go down)
                    -> set the lamp to on + the floor dest
                    -> sleep (this is elevator going down)
                    -> print elevator arrived for both floor and ele button request
                -> button is a new floor. elevator and floor button are not equal (direction must match the dest floor. ele go down)
                    -> set the lamp to on + the floor dest
                    -> sleep (this is elevator going down)
                    -> print elevator arrived at floor (request from floor button or elevator button) (do i signal here)
                    -> sleep (this ele going down)
                    -> print elevator arrived at floor (request from floor button or elevator button) (I def signal after, but for both or just one)
        -> case 2:
            -> starting floor is not top or bottom floor
                 -> is this info held in the lamp (model is stupid but works assuming that elevator always reached floor on the lamp)
                 -> maybe a CONST for top and bottom floor???
            -> button press scenarios
                -> button press is the current floor for both ele and floor(direction is void in this case)
                    -> Skip sleep
                    -> print saying current floor
                -> button press is the current floor for ele but not for floor(direction is floor button direction) (is this possible? can an elevator go somewhere if someone is inside it but gives no input)
                    -> sleep (ele going up and down)
                    -> print that ele is arrived at floor button request floor
                -> button press is a new floor and ele and floor button are equal (direction up or down depending on starting point).
                    -> set the lamp to on + the floor dest
                    -> sleep (this is elevator going up or down)
                    -> print elevator arrived
                -> button press is a new floor but ele button is direction is opposite of the dest floor. Which way do I go?
                    -> set the lamp to on + the floor dest
                    -> sleep (this is elevator going up)
                    -> print elevator arrived
     */

    //signal completion

    public void getScheduledWork() throws InterruptedException {
        // Assuming data validation is already done. i.e no bad data being passed
        ResourceType[] floorInputData = threadSafeResourceBuffer.get(specialtyIngredient);
        System.out.println("Received task from Scheduler");

        //return dataStructure

    }

    // takes in the data structure
    private void performWork() {
        /*
         setLamp(floorInputData.getButton())
         */
        System.out.println("Lamp for floor button " + lamp.getKey() + " is " + lamp.getValue());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Arrived at ");
    }
    public void signalWorkCompletion() throws InterruptedException {

    }

    public void run(){
        try {
            while (true) { // termination condition??? when does elevator thread die
                getScheduledWork();
                performWork();
                signalWorkCompletion();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
