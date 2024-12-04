package net.sabafly.slotmachine.v2.game.interaction;

import dev.cerus.maps.util.Vec2;
import org.bukkit.entity.Player;

public interface InteractionComponent {

    /**
     * @param c clicked player.
     */
    void onClick(Player c,Vec2 pos);

}
