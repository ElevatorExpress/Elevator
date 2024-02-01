import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {
    public static void main(String[] args) {

        BeanMaster threadSafeBeanMasterShared = new BeanMaster();
        threadSafeBeanMasterShared.init();
        threadSafeBeanMasterShared.makeCoffee(20);
    }
}