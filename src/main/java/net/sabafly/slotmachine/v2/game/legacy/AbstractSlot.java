package net.sabafly.slotmachine.v2.game.legacy;

import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.graphics.MapGraphics;
import dev.cerus.maps.util.Vec2;
import net.sabafly.slotmachine.game.ParaMachine;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class AbstractSlot extends ParaMachine {

    private LocalDateTime lastClickedTime = LocalDateTime.now();

    public AbstractSlot(MapScreen screen, UUID uuid) {
        super(screen, uuid);
    }

    public void onDestroy() {}
    public void tick() {}

    @Override
    public void onClick(Player player, Vec2 vec2) {
        lastClickedTime = LocalDateTime.now();
        super.onClick(player, vec2);
    }

    @Override
    public void run() {
        this.tick();
        if (!LocalDateTime.now().isAfter(lastClickedTime.plusSeconds(30)) || this.tick % 20 == 0) {
            final MapGraphics<?, ?> graphics = getScreen().getGraphics();
            render(graphics);
            sendPlayers();
        }
        super.run();
    }

    @Override
    public void destroy() {
        onDestroy();
        super.destroy();
    }

}
