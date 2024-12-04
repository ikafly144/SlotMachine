package net.sabafly.slotmachine.game;

import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.event.PlayerClickScreenEvent;
import net.sabafly.slotmachine.SlotMachine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.util.*;

@Deprecated(forRemoval = true)
public class ScreenManager implements Listener {

    public static final double MAX_DIST = Math.pow(48, 2);

    private static final Map<UUID, Set<UUID>> viewerMap = new HashMap<>();

    private static final Map<UUID, Machine<?>> machineMap = new HashMap<>();

    private static final Map<MapScreen, Machine<?>> screenMap = new HashMap<>();

    private static final Map<UUID, BukkitTask> taskMap = new HashMap<>();

    public static Map<UUID, Set<UUID>> getViewerMap() {
        return viewerMap;
    }

    public static Map<UUID, Machine<?>> getMachineMap() {
        return machineMap;
    }

    public static Map<MapScreen, Machine<?>> getScreenMap() { return screenMap; }

    public static ScreenManager getInstance() {
        return new ScreenManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onScreenClick(PlayerClickScreenEvent event) {
        MapScreen screen = event.getClickedScreen();
        if (screen == null) return;

        Machine<?> machine = getScreenMap().get(screen);
        if (machine == null) return;

        machine.onClick(event.getPlayer(), event.getClickPos());

        event.setCancelled(true);
    }

    public static void load(final File dataFolder, BukkitScheduler scheduler) throws ConfigurateException {
        dataFolder.mkdirs();
        final File[] files = dataFolder.listFiles();
        if (files == null) return;
        for (final File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            SlotMachine.getPlugin().getLogger().info("Loading " + file.getName());
            final Machine<?> machine = Machine.of(file);
            machine.getScreen().sendMaps(true);
            registerMachine(machine, scheduler);
        }
    }

    public static void save(final File dataFolder) throws ConfigurateException {
        dataFolder.mkdirs();
        for (final Machine<?> machine : getMachineMap().values()) {
            final ConfigurationLoader<?> node = YamlConfigurationLoader.builder()
                    .path(new File(dataFolder, machine.filename()).toPath())
                    .nodeStyle(NodeStyle.BLOCK)
                    .headerMode(HeaderMode.NONE)
                    .build();
            machine.save(node);
        }
    }

    public static void registerMachine(Machine<?> machine, BukkitScheduler scheduler) {
        getMachineMap().put(machine.getUniqueId(), machine);
        getScreenMap().put(machine.getScreen(), machine);
        getViewerMap().put(machine.getUniqueId(), new HashSet<>());

        BukkitTask task = scheduler.runTaskTimerAsynchronously(SlotMachine.getPlugin(), machine, 1L, 0);

        SlotMachine.getPlugin().getLogger().info("registered machine: " + machine.getUniqueId());

        taskMap.put(machine.getUniqueId(), task);

    }

    public static void destroyMachine(Machine<?> machine) {
        getMachineMap().remove(machine.getUniqueId());
        getScreenMap().remove(machine.getScreen());
        getViewerMap().remove(machine.getUniqueId());

        BukkitTask task = taskMap.get(machine.getUniqueId());
        if (task != null) {
            task.cancel();
            taskMap.remove(machine.getUniqueId());
        }

    }

}
