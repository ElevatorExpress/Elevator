package util.states;

import scheduler.Scheduler;

/**
 * Represents the SchedulerScheduling State
 * Contains 3 Substates:
 * - READING_BUFF
 * - SERVING_ELEVATORS
 * - SERVING_FLOORS
 */
public class SchedulerScheduling extends SchedulerState {

//    private SubState internalState;

    /**
     * The sub-states of SchedulerScheduling
     */
//    public enum SubState {
//        SERVING_ELEVATORS,
//        SERVING_FLOORS,
//        READ_BUFF;
//
//        SubState(){}
//
//        /**
//         * Get the next state in the machine.
//         * @return The next state
//         */
//        private SubState next(){
//            if (this == READ_BUFF) {return SERVING_ELEVATORS;}
//            else if (this == SERVING_ELEVATORS) {return SERVING_FLOORS;}
//            else return null;
//        }
//
//        /**
//         * Reset the internal state.
//         * @return The READING_BUFF state
//         */
//        private static SubState reset(){
//            return READ_BUFF;
//        }
//    }

    protected SchedulerScheduling(Scheduler ctx) {
        super(ctx);
//        this.internalState = SubState.READ_BUFF;
    }

//    /**
//     * Handles the "Done Reading Request" event
//     * @return The next state
//     */
//    @Override
//    public SchedulerState handleDoneReadingRequest() {
//        if (internalState != null) internalState = internalState.next();
//        return this;
//    }
//
//    /**
//     * Handles the "Done Serving" event
//     * @return The next state
//     */
//    @Override
//    public SchedulerState handleDoneServing() {
//        if (internalState.next() == null) internalState = SubState.reset();
//        else internalState = internalState.next();
//        return this;
//    }
//
//    /**
//     * @return The current SubState
//     */
//    public SubState getSubState(){
//        return this.internalState;
//    }

}
