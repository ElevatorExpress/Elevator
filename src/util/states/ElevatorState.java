package util.states;

import elevator.ElevatorSubsystem;

public abstract class ElevatorState {

    protected ElevatorSubsystem ctx;

    protected ElevatorState(ElevatorSubsystem context){
        this.ctx = context;
    }

    /**
     * Starts the state machine.
     * @param context The context for this state machine.
     * @return The first state in the state machine.
     */
    public static ElevatorState start(ElevatorSubsystem context){
        return new ElevatorIdle(context);
    }

    /**
     * Handles the "Complete  Request" event
     * @return The next state
     */
    public ElevatorState handleCompleteRequest(){
        return this;
    }

    /**
     * Handles the "Receive Request" event
     * @return The next state
     */
    public ElevatorState handleReceiveRequest(){
        return this;
    }

}
