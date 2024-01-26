package net.sabafly.slotmachine.game.slot;

import dev.cerus.maps.api.Frame;
import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.plugin.map.MapScreenRegistry;
import dev.cerus.maps.version.VersionAdapterFactory;
import net.sabafly.slotmachine.SlotMachine;
import net.sabafly.slotmachine.configuration.Medals;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Deprecated(forRemoval = true)
public class SlotManager implements AutoCloseable {

    private final File dataFolder;
    private final Map<Integer, SlotEntity> maps;
    private final Map<Player, SlotEntity> sessionMap;
    private final Map<Integer, Set<UUID>> screenViewerMap = new HashMap<>();
    private final Plugin plugin;
    private final Map<UUID, Long> medalMap = new HashMap<>();
    private String lastTakeMedalDay = "";
    private final Map<UUID, Long> medalTakeMap = new HashMap<>();

    public SlotManager(File dataFolder, Plugin plugin) throws IOException {
        this.dataFolder = dataFolder;
        this.maps = new ConcurrentHashMap<>();
        this.sessionMap = new ConcurrentHashMap<>();
        this.plugin = plugin;
    }

    public void load() {
        dataFolder.mkdirs();
        File medal = new File(dataFolder.getParentFile(), "medals.yml");
        if (medal.exists()) {
            final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .path(medal.toPath())
                    .indent(2)
                    .headerMode(HeaderMode.PRESET)
                    .nodeStyle(NodeStyle.BLOCK)
                    .build();
            try{
                CommentedConfigurationNode node = loader.load();
                Medals md = node.get(Medals.class, new Medals());
                medalMap.putAll(md.medalMap);
            } catch (Exception e) {
                plugin.getLogger().info("failed to load medals in " + medal.getAbsolutePath());
                e.printStackTrace();
            }
        }
        File[] files = dataFolder.listFiles();
        if (files == null) return;
        for (File file : files) {
            try {
                final SlotEntity slotEntity = SlotEntity.load(this, file, plugin);
                if (slotEntity == null) continue;
                putMap(slotEntity, slotEntity.getScreen());
            } catch (Exception e) {
                plugin.getLogger().info("failed to load map in " + file.getAbsolutePath());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws Exception {
        save();
    }

    public void newMap(Entity entity) {
        resetMap(entity);
    }

    private void putMap(SlotEntity slotEntity, MapScreen screen) {

        maps.put(screen.getId(), slotEntity);

    }

    public void removeMap(SlotEntity slotEntity) {
        maps.remove(slotEntity.getScreen().getId());
        MapScreenRegistry.removeScreen(slotEntity.getScreen().getId());
        dataFolder.listFiles((dir, name) -> {
            if (name.startsWith(slotEntity.getUuid() + "")) {
                new File(dir, name).delete();
            }
            return false;
        });
    }

    public void resetMap(Entity entity) {
        ItemFrame itemFrame = (ItemFrame) entity;

        MapScreen screen = new MapScreen(MapScreenRegistry.getNextFreeId(), new VersionAdapterFactory().makeAdapter(), 1, 1);
        screen.sendMaps(true);
        Location location = itemFrame.getLocation();
        screen.setLocation(location);
        screen.setFrames(
            new Frame[][]{
                {
                    new Frame(
                        itemFrame.getWorld(),
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ(),
                        itemFrame.getFacing(),
                        itemFrame.getEntityId(),
                        itemFrame.isVisible(),
                        itemFrame.getType() == EntityType.GLOW_ITEM_FRAME
                    )
                }
            }
        );
        MapScreenRegistry.registerScreen(screen);

        itemFrame.remove();

        SlotEntity slotEntity = new SlotEntity(itemFrame.getUniqueId(), screen, this, plugin, SlotRegistry.LegacySetting.SETTING_3);
        putMap(slotEntity, slotEntity.getScreen());

    }

    public void save() throws Exception {
        dataFolder.mkdirs();
        File medal = new File(dataFolder.getParentFile(), "medals.yml");
        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(medal.toPath())
                .indent(2)
                .headerMode(HeaderMode.PRESET)
                .nodeStyle(NodeStyle.BLOCK)
                .build();
        loader.save(loader.createNode().set(new Medals(medalMap)));

        for (Map.Entry<Integer, SlotEntity> entry: maps.entrySet()) {
            entry.getValue().save();
        }
    }

    protected void registerPlayingPlayer(Player player, SlotEntity slotEntity) {
        if (sessionMap.containsKey(player)) {
            leavePlayer(player);
        }
        sessionMap.put(player, slotEntity);
    }

    public void leavePlayer(@NotNull Player player) {
        leavePlayer(player, false);
    }

    protected void leavePlayer(@NotNull Player player, boolean cancelled) {
        SlotEntity map = sessionMap.remove(player);
        if (map == null) return;
        if (!cancelled) map.stop(player);
    }

    public void joinPlayer(@NotNull Player player) {
        maps.forEach(((uuid, slotEntity) -> {
            MapScreen screen = slotEntity.getScreen();
            screen.sendMaps(true, player);
        }));
    }

    @Nullable
    public SlotEntity findMap(Integer screenId) {
        return maps.get(screenId);
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public Map<Integer, Set<UUID>> getScreenViewerMap() {
        return screenViewerMap;
    }

    public void setMedal(HumanEntity player, long medal) {
        medalMap.put(player.getUniqueId(), medal);
    }

    public long getMedal(HumanEntity player) {
        return medalMap.getOrDefault(player.getUniqueId(), 0L);
    }

    public void addMedal(HumanEntity player, long medal) {
        medalMap.put(player.getUniqueId(), getMedal(player) + medal);
    }

    public void removeMedal(HumanEntity player, long medal) {
        medalMap.put(player.getUniqueId(), getMedal(player) - medal);
    }

    public boolean canTakeMedal(HumanEntity player, long medal) {
        if (!lastTakeMedalDay.equals(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))) {
            medalTakeMap.clear();
            lastTakeMedalDay = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        long m = getMedal(player);
        if (m < medal) return false;
        Long p = medalTakeMap.getOrDefault(player.getUniqueId(), 0L);
        return p + medal < SlotMachine.getPluginConfig().lend.savedMedalMaxUsePerDay;
    }

    public void takeMedal(HumanEntity player, long medal) {
        removeMedal(player, medal);
        Long p = medalTakeMap.getOrDefault(player.getUniqueId(), 0L);
        medalTakeMap.put(player.getUniqueId(), p + medal);
    }

    @Nullable
    public Long removeMedalAll(HumanEntity player) {
        return medalMap.remove(player.getUniqueId());
    }

    public Map<UUID, Long> getMedalMap() {
        return medalMap;
    }
}
