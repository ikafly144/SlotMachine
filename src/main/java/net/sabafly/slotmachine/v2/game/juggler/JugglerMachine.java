package net.sabafly.slotmachine.v2.game.juggler;

import com.bergerkiller.bukkit.common.map.MapDisplayProperties;
import net.sabafly.slotmachine.v2.game.ParaMachine;
import net.sabafly.slotmachine.v2.game.juggler.ui.UIButton;
import net.sabafly.slotmachine.v2.game.juggler.ui.UIWheels;
import net.sabafly.slotmachine.v2.game.slot.Pos;
import net.sabafly.slotmachine.v2.game.slot.Wheel;
import net.sabafly.slotmachine.v2.game.slot.WheelPattern;

import static net.sabafly.slotmachine.v2.game.slot.WheelPattern.*;

public class JugglerMachine extends ParaMachine {

    protected Wheel left = new Wheel(new WheelPattern[]{
            GRAPE,
            REPLAY,
            GRAPE,
            BAR,
            CHERRY,
            GRAPE,
            REPLAY,
            GRAPE,
            CLOWN,
            SEVEN,
            GRAPE,
            REPLAY,
            GRAPE,
            CHERRY,
            BAR,
            GRAPE,
            REPLAY,
            GRAPE,
            REPLAY,
            SEVEN,
            BELL,
    },
            Pos.LEFT);
    protected Wheel center = new Wheel(new WheelPattern[]{
            CLOWN,
            CHERRY,
            GRAPE,
            BAR,
            REPLAY,
            CHERRY,
            GRAPE,
            BELL,
            REPLAY,
            CHERRY,
            GRAPE,
            BAR,
            REPLAY,
            CHERRY,
            GRAPE,
            BELL,
            REPLAY,
            CHERRY,
            GRAPE,
            SEVEN,
            REPLAY,
    },
            Pos.CENTER);
    protected Wheel right = new Wheel(new WheelPattern[]{
            REPLAY,
            BELL,
            CLOWN,
            GRAPE,
            REPLAY,
            BELL,
            CLOWN,
            GRAPE,
            REPLAY,
            BELL,
            CLOWN,
            GRAPE,
            REPLAY,
            BELL,
            CLOWN,
            GRAPE,
            REPLAY,
            BELL,
            BAR,
            SEVEN,
            GRAPE,
    },
            Pos.RIGHT);

    public Wheel getWheel(Pos pos) {
        return switch (pos) {
            case CENTER -> center;
            case LEFT -> left;
            case RIGHT -> right;
        };
    }

    @Override
    public boolean setup() {
        addWidget(new UIButton((event) -> {
            left.start();
            center.start();
            right.start();
        }).setBounds(0, 64, 16, 16));
        addWidget(new UIWheels<>(this));
        return true;
    }

    @Override
    public void tick() {
        left.step();
        center.step();
        right.step();
    }
}
