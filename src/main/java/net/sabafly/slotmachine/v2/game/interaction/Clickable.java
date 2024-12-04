package net.sabafly.slotmachine.v2.game.interaction;

import dev.cerus.maps.util.Vec2;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

public abstract class Clickable implements InteractionComponent {

    private final BiConsumer<Vec2, Player> task;
    protected final int x;
    protected final int y;

    Clickable(int x, int y, BiConsumer<Vec2, Player> task) {
        this.task = task;
        this.x = x;
        this.y = y;
    }

    @Override
    public void onClick(Player c, Vec2 pos) {
        task.accept(pos, c);
    }
}
