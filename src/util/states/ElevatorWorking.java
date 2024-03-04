package util.states;

import elevator.ElevatorSubsystem;

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
