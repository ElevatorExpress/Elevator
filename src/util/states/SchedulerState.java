package util.states;

import scheduler.Scheduler;

public abstract class SchedulerState {
    /**
     * TODO: This
     * Scheduling
     *  - Reading Buffer
     *  - Serve Elevator Requests
     *  - Serve Floor Requests
     * Error
     * Emergency
     */

    protected Scheduler ctx;

    protected SchedulerState(Scheduler context){
        this.ctx = context;
    }

    public SchedulerState handleLookingForRequest(){
        return this;
    }

    public SchedulerState doneReadingRequest(){
        return this;
    }

    public SchedulerState handleDoneServing(){
        return this;
    }

}
