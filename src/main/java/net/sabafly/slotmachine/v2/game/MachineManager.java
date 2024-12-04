package net.sabafly.slotmachine.v2.game;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import dev.cerus.maps.api.Frame;
import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.event.PlayerClickScreenEvent;
import dev.cerus.maps.plugin.map.MapScreenRegistry;
import dev.cerus.maps.version.VersionAdapterFactory;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.SneakyThrows;
import net.kyori.adventure.key.Key;
import net.sabafly.slotmachine.v2.game.data.DataFolderLoader;
import net.sabafly.slotmachine.v2.game.machine.Juggler;
import net.sabafly.slotmachine.v2.game.machine.Machine;
import net.sabafly.slotmachine.v2.game.machine.BaseMachine;
import net.sabafly.slotmachine.v2.game.render.map.MapScreenImpl;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class MachineManager implements Listener {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static MachineManager instance;

    public static void init(@NotNull Plugin plugin) {
        Preconditions.checkNotNull(plugin, "Plugin cannot be null");
        Preconditions.checkArgument(instance == null, "MachineManager is already initialized");
        instance = new MachineManager(plugin);
        plugin.getServer().getPluginManager().registerEvents(instance, plugin);
    }

    private static void shutdown() {
        for (var entry : machineTasks.entrySet()) {
            entry.getValue().cancel();
        }
        machineTasks.clear();
        for (Map.Entry<Triple<Key, Integer, Integer>, Set<Machine<BufferedImage>>> entry : machineLocations.entrySet()) {
            for (Machine<?> machine : entry.getValue()) {
                saveMachine(machine);
            }
        }
        machineLocations.clear();
        machineScreenIds.clear();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void loadMachines() {
        final Path path = DataFolderLoader.getInstance().getPath();
        final File machines = path.resolve("machines").toFile();
        if (!machines.exists() || (machines.isFile() && machines.delete())) machines.mkdirs();
        Preconditions.checkArgument(machines.isDirectory(), "Machines directory is not a directory");
        Preconditions.checkArgument(machines.canRead(), "Machines directory is not readable");
        Preconditions.checkArgument(machines.canWrite(), "Machines directory is not writable");
        final File[] reference = machines.listFiles();
        Preconditions.checkNotNull(reference, "Machines directory is empty");
        for (File file : reference) {
            if (file.isDirectory()) continue;
            final String name = file.getName();
            if (!name.endsWith(".json")) continue;
            try {
                final Pair<Machine<BufferedImage>, Integer> pair = MachineLoader.load(file.toPath());
                final int screenId = pair.getRight();
                final Machine<BufferedImage> machine = pair.getLeft();
                registerMachine(machine, readMachineLocation(screenId), screenId);
                saveMachine(machine, screenId);
            } catch (ConfigurateException e) {
                LOGGER.error("Failed to createId machine from file: {}", file, e);
            }
        }
    }

    public static void saveMachine(@NotNull Machine<?> machine) {
        final int screenId = machineScreenIds.get(machine.id());
        saveMachine(machine, screenId);
    }

    private static void saveMachine(@NotNull Machine<?> machine, int screenId) {
        try {
            MachineLoader.save(machine, getMachinePath(machine), screenId);
        } catch (ConfigurateException e) {
            LOGGER.error("Failed to save machine: {}", machine, e);
        }
    }

    public static void createMachine(Machine<BufferedImage> machine, Entity entity) {
        Preconditions.checkArgument(entity instanceof ItemFrame, "Entity must be an item frame");
        final Location location = entity.getLocation();

        final MapScreen screen = new MapScreen(MapScreenRegistry.getNextFreeId(), new VersionAdapterFactory().makeAdapter(), 1, 1);
        screen.sendMaps(true);
        screen.setLocation(location);
        final Frame[][] frame = new Frame[][]{
                {
                        new Frame(
                                entity.getWorld(),
                                location.getBlockX(),
                                location.getBlockY(),
                                location.getBlockZ(),
                                entity.getFacing(),
                                entity.getEntityId(),
                                ((ItemFrame) entity).isVisible(),
                                entity.getType() == EntityType.GLOW_ITEM_FRAME
                        )
                }
        };
        screen.setFrames(frame);
        MapScreenRegistry.registerScreen(screen);

        entity.remove();
        registerMachine(machine, location, screen.getId());
    }

    private static void registerMachine(Machine<BufferedImage> machine, Location location, int screenId) {
        try {
            MachineLoader.save(machine, getMachinePath(machine), screenId);
        } catch (ConfigurateException e) {
            LOGGER.error("Failed to save machine: {}", machine, e);
        }
        final Triple<Key, Integer, Integer> key = Triple.of(location.getWorld().key(), location.getChunk().getX(), location.getChunk().getZ());
        machineLocations.computeIfAbsent(key, (k) -> new HashSet<>()).add(machine);
        machineScreenIds.put(machine.id(), screenId);
        machine.setup();
        if (!machineTasks.containsKey(key)) {
            startTask(key);
        }
    }

    private static void startTask(Triple<Key, Integer, Integer> key) {
        final RegionScheduler scheduler = Bukkit.getRegionScheduler();
        var world = Bukkit.getWorld(key.getLeft());
        if (world == null) return;
        ScheduledTask task = scheduler.runAtFixedRate(instance.plugin, world, key.getMiddle(), key.getRight(), (t) -> {
            for (var machine : machineLocations.get(key)) {
                machine.tick();
                int screenId = machineScreenIds.get(machine.id());
                final MapScreen screen = MapScreenRegistry.getScreen(screenId);
                MapScreenImpl<?, ?, BufferedImage> mapScreen = new MapScreenImpl<>(screen.getGraphics());
                mapScreen.addRenderer(machine);
                mapScreen.render(mapScreen);
                machine.sendPlayers(screen);
            }
        }, 1, 1);
        machineTasks.put(key, task);
    }

    private final Plugin plugin;

    private MachineManager(Plugin plugin) {
        this.plugin = plugin;
    }

    private static final Map<UUID, Integer> machineScreenIds = new HashMap<>();
    private static final Map<Triple<Key, Integer, Integer>, Set<Machine<BufferedImage>>> machineLocations = new HashMap<>();
    private static final Map<Triple<Key, Integer, Integer>, ScheduledTask> machineTasks = new HashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.isNewChunk()) return;
        final Chunk chunk = event.getChunk();
        final World world = event.getWorld();
        final Triple<Key, Integer, Integer> key = Triple.of(world.key(), chunk.getX(), chunk.getZ());
        if (!machineLocations.containsKey(key)) return;
        startTask(key);
    }

    @EventHandler(ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        final Chunk chunk = event.getChunk();
        final World world = event.getWorld();
        final Triple<Key, Integer, Integer> key = Triple.of(world.key(), chunk.getX(), chunk.getZ());
        ScheduledTask task = machineTasks.remove(key);
        if (task != null) {
            task.cancel();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin() != instance.plugin) return;
        shutdown();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerClickScreen(PlayerClickScreenEvent event) {
        int screenId = event.getClickedScreen().getId();
        machineScreenIds.entrySet().stream().filter(e -> e.getValue() == screenId).findFirst().ifPresent(e -> {
            Machine<?> machine = machineLocations.values().stream().flatMap(Collection::stream).filter(m -> m.id().equals(e.getKey())).findFirst().orElse(null);
            if (machine != null) {
                machine.onClick(event.getPlayer(), event.getClickPos());
                event.setCancelled(true);
            }
        });
    }


    private static class MachineLoader implements Listener {
        @SneakyThrows
        private static Pair<Machine<BufferedImage>, Integer> load(Path path) throws ConfigurateException {
            ConfigurationNode node = GsonConfigurationLoader.builder()
                    .path(path)
                    .build()
                    .load();
            Type type = node.node("type").require(Type.class);
            UUID id = node.node("id").get(UUID.class);
            int screenId = node.node("screen").getInt();
            Preconditions.checkNotNull(type, "Machine type cannot be null");
            Preconditions.checkNotNull(id, "Machine id cannot be null");
            //noinspection SwitchStatementWithTooFewBranches
            return Pair.of(BaseMachine.load((
                    switch (type) {
                        case JUGGLER -> Juggler.createId(id);
                        //noinspection UnnecessaryDefault
                        default -> throw new IllegalArgumentException("Unknown machine type: " + type);
                    }), node), screenId);
        }

        private static void save(Machine<?> machine, Path path, int screenId) throws ConfigurateException {
            ConfigurationLoader<?> loader = GsonConfigurationLoader.builder()
                    .path(path)
                    .build();
            ConfigurationNode node = machine.save();
            node.node("type").set(switch (machine) {
                case Juggler ignored -> Type.JUGGLER;
            });
            node.node("screen").set(screenId);
            loader.save(node);
        }

        private enum Type {
            JUGGLER
        }
    }

    private static Path getMachinePath(Machine<?> machine) {
        return DataFolderLoader.getInstance().getPath().resolve("machines").resolve(machine.id().toString() + ".json");
    }

    private static Location readMachineLocation(int id) {
        return MapScreenRegistry.getScreen(id).getLocation();
    }

}
