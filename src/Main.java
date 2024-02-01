//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

public class Main {
    public Main() {
    }

    public static void main(String[] args) {
        Scheduler threadSafeSchedulerShared = new Scheduler();
        threadSafeSchedulerShared.init();
        threadSafeSchedulerShared.makeCoffee(20);
        System.out.println("Scheduler made coffees");
    }
}
