package net.sabafly.slotmachine.game;

import com.fasterxml.uuid.Generators;
import dev.cerus.maps.api.MapScreen;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class ParaMachine implements Machine<ParaMachine> {

    long tick = 0;
    long cooldown = 0;
    final MapScreen screen;
    final UUID uuid;

    public ParaMachine(final MapScreen screen) {
        this(screen, Generators.timeBasedEpochGenerator().generate());
    }

    public ParaMachine(final MapScreen screen, UUID uuid) {
        this.uuid = uuid;
        this.screen = screen;
    }

    @Override
    public void run() {
        tick++;
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    public void sendPlayers() {
        final Collection<Player> receivers = new HashSet<>();
        final Set<UUID> viewers = ScreenManager.getViewerMap().computeIfAbsent(this.getUniqueId(), $ -> new HashSet<>());
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(getScreen().getLocation().getWorld())) {
                final double dist = player.getLocation().distanceSquared(getScreen().getLocation());
                if (viewers.contains(player.getUniqueId()) && dist > ScreenManager.MAX_DIST) {
                    // Remove
                    viewers.remove(player.getUniqueId());
                    getScreen().destroyFrames(player);
                } else if (!viewers.contains(player.getUniqueId()) && dist < ScreenManager.MAX_DIST) {
                    // Add
                    viewers.add(player.getUniqueId());
                    getScreen().spawnFrames(player);
                    getScreen().sendMaps(true, player);
                }
            } else {
                viewers.remove(player.getUniqueId());
            }
            if (viewers.contains(player.getUniqueId())) {
                // Update
                receivers.add(player);
            }
        }
        if (!receivers.isEmpty()) {
            getScreen().sendMaps(false, receivers);
        }
        for (final UUID uuid : Set.copyOf(viewers)) {
            if (Bukkit.getPlayer(uuid) == null) {
                viewers.remove(uuid);
            }
        }
    }

    @Override
    public MapScreen getScreen() {
        return screen;
    }
}
