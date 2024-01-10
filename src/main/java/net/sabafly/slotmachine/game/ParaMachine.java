package net.sabafly.slotmachine.game;

import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.graphics.ColorCache;
import dev.cerus.maps.api.graphics.MapGraphics;
import dev.cerus.maps.plugin.map.MapScreenRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@ConfigSerializable
public abstract class ParaMachine implements Machine<ParaMachine> {

    long tick = 0;
    final MapScreen screen;

    public ParaMachine() {
        this.screen = MapScreenRegistry.getScreen(MapScreenRegistry.getNextFreeId());
    }

    @Override
    public ParaMachine load(File file) throws ConfigurateException {
        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(file.toPath())
                .indent(2)
                .headerMode(HeaderMode.PRESET)
                .nodeStyle(NodeStyle.BLOCK)
                .build();
        CommentedConfigurationNode node =loader.load();
        return node.get(this.getClass());
    }

    @Override
    public void save(File file) {

    }

    @Override
    public void run() {
        final MapGraphics<?, ?> graphics = screen.getGraphics();
        graphics.fillComplete(ColorCache.rgbToMap(0, 0, 0));
        tick++;
    }


    protected void sendPlayers() {
        final Collection<Player> receivers = new HashSet<>();
        final Set<UUID> viewers = ScreenManager.getViewerMap().computeIfAbsent(screen.getId(), $ -> new HashSet<>());
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(screen.getLocation().getWorld())) {
                final double dist = player.getLocation().distanceSquared(screen.getLocation());
                if (viewers.contains(player.getUniqueId()) && dist > ScreenManager.MAX_DIST) {
                    // Remove
                    viewers.remove(player.getUniqueId());
                    screen.destroyFrames(player);
                } else if (!viewers.contains(player.getUniqueId()) && dist < ScreenManager.MAX_DIST) {
                    // Add
                    viewers.add(player.getUniqueId());
                    screen.spawnFrames(player);
                    screen.sendMaps(true, player);
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
            screen.sendMaps(false, receivers);
        }
        for (final UUID uuid : Set.copyOf(viewers)) {
            if (Bukkit.getPlayer(uuid) == null) {
                viewers.remove(uuid);
            }
        }
    }

}
