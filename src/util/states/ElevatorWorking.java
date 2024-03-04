package util.states;

import elevator.ElevatorSubsystem;

/**
 * Represents the ElevatorWorking State
 */
public class ElevatorWorking extends ElevatorState{

    protected ElevatorWorking(ElevatorSubsystem ctx){
        super(ctx);
    }

    /**
     * Handles the "Complete  Request" event
     *
     * @return The next state
     */
    @Override
    public ElevatorState handleCompleteRequest() {
        return new ElevatorIdle(this.ctx);
    }
}
