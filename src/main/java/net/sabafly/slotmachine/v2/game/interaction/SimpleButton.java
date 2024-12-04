package net.sabafly.slotmachine.v2.game.interaction;

import dev.cerus.maps.util.Vec2;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.BiConsumer;

@ApiStatus.NonExtendable
public class SimpleButton extends Clickable{

    public SimpleButton(int x, int y, int w, int h, BiConsumer<Vec2, Player> task) {
        super(x, y, (pos,p) -> {
            if (pos.x >= x && pos.x <= x + w && pos.y >= y && pos.y <= y + h) {
                task.accept(pos, p);
            }
        });
    }

}
