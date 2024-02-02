import java.util.ArrayList;

public class Floor implements Runnable, UtilityInterface<Floor.FloorInfo>{
    private final UtilityInterface<FloorInfo> scheduler;
    private final FloorInfo currentFloorInfo;
    /** @noinspection MismatchedQueryAndUpdateOfCollection*/
    private final ArrayList<FloorInfo> floorInfoCatcher;


    public Floor(UtilityInterface<FloorInfo> ownerScheduler) {
        scheduler = ownerScheduler;
        floorInfoCatcher = new ArrayList<>();
        /*
            this.floorInfo = FloorInfo.create( fName | Path );
            This method is supposed to create floor info. Read from file.
         */
        // Temp
        currentFloorInfo = new FloorInfo();
    }

    public void run(){
        // Does this only have to work once ?
        sendFloorData();
    }

    //
    private void sendFloorData(){
        this.scheduler.put(this.currentFloorInfo);
    }

    /**
     * Unused
     *
     * @return null
     */
    @Override
    public FloorInfo get() {return null;}

    @Override
    public synchronized FloorInfo put(FloorInfo fInfo) {
        floorInfoCatcher.add(fInfo);
        return fInfo;
    }

    public record FloorInfo(){}


}
