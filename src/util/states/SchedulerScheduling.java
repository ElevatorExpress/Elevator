package util.states;

import scheduler.Scheduler;

public class SchedulerScheduling extends SchedulerState {

    private SubState internalState;

    public enum SubState {
        READING_BUFF,
        SERVING_ELEVATORS,
        SERVING_FLOORS;

        SubState(){}

        private SubState next(){
            if (this == READING_BUFF) {return SERVING_ELEVATORS;}
            else if (this == SERVING_ELEVATORS) {return SERVING_FLOORS;}
            else return null;
        }

    }

    protected SchedulerScheduling(Scheduler ctx) {
        super(ctx);
        this.internalState = SubState.READING_BUFF;
    }


    @Override
    public SchedulerState doneReadingRequest() {
        internalState = internalState.next();
        return this;
    }

    @Override
    public SchedulerState handleDoneServing() {
        internalState = internalState.next();
        return this;
    }

    public SubState getSubState(){
        return this.internalState;
    }

}
