package net.sabafly.slotmachine.v2.game.machine;

import java.awt.*;

public sealed interface StateMachine<I extends Image> extends Machine<I> permits Juggler, BaseMachine {

    enum State {
        IDLE,
        SPINNING,
        STOPPING,
        STOPPED
    }

    void setState(State state);

    State getState();

}
