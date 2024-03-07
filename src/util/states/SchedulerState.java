package util.states;

import scheduler.Scheduler;

/**
 * Base Scheduler State
 */
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

    /**
     * Starts the state machine.
     * @param  context The context for this state machine.
     * @return The first state in the state machine.
     */
    public static SchedulerState start(Scheduler context){
        return new SchedulerScheduling(context);
    }

    protected SchedulerState(Scheduler context){
        this.ctx = context;
    }

    /**
     * Handles the "Look For Request" event
     * @return The next state
     */
    public SchedulerState handleLookForRequest(){
        return this;
    }

    /**
     * Handles the "Done Reading Request" event
     * @return The next state
     */
    public SchedulerState handleDoneReadingRequest(){
        return this;
    }

    /**
     * Handles the "Done Serving" event
     * @return The next state
     */
    public SchedulerState handleDoneServing(){
        return this;
    }

    /**
     * Handles the "Bad Message" event
     * @return The next state
     */
    public SchedulerState handleBadMessage() {return new SchedulerError(ctx, "Malformed Message");}

    /**
     * Handles the "Emergency" event
     * @return The next state
     */
    public SchedulerState handleEmergency() {return new SchedulerEmergency(ctx);}

}
