package net.sabafly.slotmachine.game;

import lombok.Getter;
import net.sabafly.slotmachine.SlotMachine;
import net.sabafly.slotmachine.configuration.Medals;
import org.bukkit.entity.HumanEntity;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MedalBank {
    @Getter
    static Map<UUID, Long> medalMap = new HashMap<>();
    static LocalDate lastTakeMedalDay = LocalDate.now();
    static Map<UUID, Long> medalTakeMap = new HashMap<>();

    public static void save(File file) throws ConfigurateException {
        SlotMachine.getPlugin().getLogger().info("Saving medal data...");
        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(new File(file, "medals.yml").toPath())
                .indent(2)
                .headerMode(HeaderMode.PRESET)
                .nodeStyle(NodeStyle.BLOCK)
                .build();
        final CommentedConfigurationNode node = loader.load();
        node.set(new Medals(medalMap));
        loader.save(node);
        SlotMachine.getPlugin().getLogger().info("Saving medal data... Done!");
    }

    public static void load(File file) throws ConfigurateException {
        SlotMachine.getPlugin().getLogger().info("Loading medal data...");
        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(new File(file, "medals.yml").toPath())
                .indent(2)
                .headerMode(HeaderMode.PRESET)
                .nodeStyle(NodeStyle.BLOCK)
                .build();
        medalMap = loader.load().get(Medals.class, new Medals()).medalMap;
        SlotMachine.getPlugin().getLogger().info("Loading medal data... Done!");
        SlotMachine.getPlugin().getLogger().info("Medal data count: " + medalMap.size());
    }


    public static void addMedal(UUID player, long medal) {
        if (medalMap.containsKey(player)) {
            medalMap.put(player, medalMap.get(player) + medal);
        } else {
            medalMap.put(player, medal);
        }
    }

    public static void takeMedal(UUID player, long medal) {
        if (medalMap.containsKey(player)) {
            medalMap.put(player, medalMap.get(player) - medal);
            medalTakeMap.put(player, medalTakeMap.getOrDefault(player, 0L) + medal);
        } else {
            medalMap.put(player, -medal);
            medalTakeMap.put(player, medal);
        }
    }

    public static long getMedal(UUID player) {
        return medalMap.getOrDefault(player, 0L);
    }

    public static void setMedal(UUID player, long medal) {
        medalMap.put(player, medal);
    }

    public static boolean canTakeMedal(UUID player, long medal) {
        if (!lastTakeMedalDay.equals(LocalDate.now())) {
            medalTakeMap.clear();
            lastTakeMedalDay = LocalDate.now();
        }
        long m = getMedal(player);
        if (m < medal) return false;
        Long p = medalTakeMap.getOrDefault(player, 0L);
        return p + medal < SlotMachine.getPluginConfig().lend.savedMedalMaxUsePerDay;
    }

    public static void removeMedal(UUID target, long amount) {
        if (medalMap.containsKey(target)) {
            medalMap.put(target, medalMap.get(target) - amount);
        } else {
            medalMap.put(target, -amount);
        }
    }

    public static Long removeMedalAll(HumanEntity player) {
        return medalMap.remove(player.getUniqueId());
    }
}
