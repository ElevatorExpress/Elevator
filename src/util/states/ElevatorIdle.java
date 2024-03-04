package util.states;

import elevator.ElevatorSubsystem;

/**
 * Represents the ElevatorIdle State
 */
public class ElevatorIdle extends ElevatorState{

    protected ElevatorIdle(ElevatorSubsystem ctx){
        super(ctx);
    }

    /**
     * Handles the "Receive Request" event
     *
     * @return The next state
     */
    @Override
    public ElevatorState handleReceiveRequest() {
        return new ElevatorWorking(this.ctx);
    }
}
