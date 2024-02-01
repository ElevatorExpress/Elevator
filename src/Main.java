//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

public class Main {
    public Main() {
    }

    public static void main(String[] args) throws InterruptedException {
        Scheduler threadSafeSchedulerShared = new Scheduler(20);
//        ThreadSafeResourceBuffer threadSafeResourceBuffer = new ThreadSafeResourceBuffer(2);
        threadSafeSchedulerShared.makeCoffee();
    }
}
