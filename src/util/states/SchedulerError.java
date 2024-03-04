package util.states;

import scheduler.Scheduler;

/**
 * Represents the SchedulerError State
 */
public class SchedulerError extends SchedulerState{

    private final String reason;

    protected SchedulerError(Scheduler context, String reason) {
        super(context);
        this.reason = reason;
    }

    /**
     * @return The reason for the error
     */
    public String getReason(){
        return reason;
    }


}
