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
            currentFloorInfo = new FloorInfo(Filelocation);
            This method is supposed to create floor info. Read from file.
         */
        // Temp
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
    public FloorInfo get() {return currentFloorInfo;}

    @Override
    public synchronized FloorInfo put(FloorInfo fInfo) {
        floorInfoCatcher.add(fInfo);
        return fInfo;
    }


}
