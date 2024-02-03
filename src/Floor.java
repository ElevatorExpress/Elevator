import Messages.MessageInterface;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Floor implements SubSystem {
    private final ResourceType specialtyIngredient;

    private final HashMap<ResourceType, ResourceType[]> resourceMap;

    private final MessageBuffer messageBuffer;

    private int cofeeGoal = 0;

    private final UtilityInterface<Integer> coffeeCounter;


    public Floor(ResourceType specialtyIngredient, MessageBuffer messageBuffer, UtilityInterface<Integer> coffeeCounter) {
        this.coffeeCounter = coffeeCounter;
        this.specialtyIngredient = specialtyIngredient;

        resourceMap = new HashMap<>();

        Arrays.stream(ResourceType.values())
                .filter(type -> type != specialtyIngredient)
                .forEach(type -> resourceMap.put(type, new ResourceType[]{null}));
        this.messageBuffer = messageBuffer;
    }
    public void setCoffeeGoal(int coffeeGoal){
        this.cofeeGoal = coffeeGoal;
    }


//    public void makeCoffee() throws InterruptedException {
//        int cCounter = coffeeCounter.get();
////        boolean done = false;
//        while (true) {
//
//            ResourceType[] resources = messageBuffer.get(specialtyIngredient);
//            cCounter = coffeeCounter.put(1);
//            if (cCounter > cofeeGoal) {
//                break;
//            }
//            StringBuilder sb = new StringBuilder();
//            for (ResourceType r : resources) {
//                sb.append(r).append(", ");
//            }
//            sb.delete(sb.length() - 2, sb.length());
//
//
//            System.out.println("Floor made coffee number: " + cCounter + " with: " + sb + " from BeanMaster and " + specialtyIngredient + " from " + specialtyIngredient + " Floor");
//
//        }
//    }



    public void run(){


    }



    public void receiveMessage(MessageInterface[] messages) {
        System.out.println("MESSAGEs RECIEVED");
        for (MessageInterface message : messages) {
            System.out.println("MESSAGE: " + message.toString());
        }
    }


    public String[] sendMessage(MessageInterface[] messages) {
        messageBuffer.put(messages);
        String [] ids = new String[messages.length];
        for (int i = 0; i < messages.length; i++) {
            ids[i] = messages[i].getMessageId();
        }
        return ids;
    }

    /**
     *
     */
    @Override
    public void receiveMessage() {

    }

    /**
     * @param message
     * @return
     */
    @Override
    public String[] sendMessage(Object[] message) {
        return new String[0];
    }
}
