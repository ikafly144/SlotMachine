package net.sabafly.slotmachine.game;

import net.sabafly.slotmachine.SlotMachine;
import net.sabafly.slotmachine.configuration.Medals;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MedalBank {
    static Map<UUID, Long> medalMap = new HashMap<>();
    static String lastTakeMedalDay = "";
    static Map<UUID, Long> medalTakeMap = new HashMap<>();

    public static void save() throws ConfigurateException {
        SlotMachine.getPlugin().getLogger().info("Saving medal data...");
        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(new File(SlotMachine.getPlugin().getDataFolder(), "medals.yml").toPath())
                .indent(2)
                .headerMode(HeaderMode.PRESET)
                .nodeStyle(NodeStyle.BLOCK)
                .build();
        loader.load().node("medals").set(new Medals(medalMap));
        SlotMachine.getPlugin().getLogger().info("Saving medal data... Done!");
    }

    public static void load() throws ConfigurateException {
        SlotMachine.getPlugin().getLogger().info("Loading medal data...");
        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(new File(SlotMachine.getPlugin().getDataFolder(), "medals.yml").toPath())
                .indent(2)
                .headerMode(HeaderMode.PRESET)
                .nodeStyle(NodeStyle.BLOCK)
                .build();
        medalMap = loader.load().node("medals").get(Medals.class, new Medals()).medalMap;
        SlotMachine.getPlugin().getLogger().info("Loading medal data... Done!");
    }


    public static void addMedal(HumanEntity player, long medal) {
        if (medalMap.containsKey(player.getUniqueId())) {
            medalMap.put(player.getUniqueId(), medalMap.get(player.getUniqueId()) + medal);
        } else {
            medalMap.put(player.getUniqueId(), medal);
        }
    }

    public static void takeMedal(HumanEntity player, Long medal) {
        if (medalMap.containsKey(player.getUniqueId())) {
            medalMap.put(player.getUniqueId(), medalMap.get(player.getUniqueId()) - medal);
        } else {
            medalMap.put(player.getUniqueId(), -medal);
        }
    }

    public static long getMedal(HumanEntity player) {
        return medalMap.getOrDefault(player.getUniqueId(), 0L);
    }

    public static void setMedal(HumanEntity player, long medal) {
        medalMap.put(player.getUniqueId(), medal);
    }

    public static boolean canTakeMedal(HumanEntity player, Long medal) {
        if (!lastTakeMedalDay.equals(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now()))) {
            medalTakeMap.clear();
            lastTakeMedalDay = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        long m = getMedal(player);
        if (m < medal) return false;
        Long p = medalTakeMap.getOrDefault(player.getUniqueId(), 0L);
        return p + medal < SlotMachine.getPluginConfig().lend.savedMedalMaxUsePerDay;
    }

    public static Map<UUID, Long> getMedalMap() {
        return medalMap;
    }

    public static void removeMedal(HumanEntity target, long amount) {
        if (medalMap.containsKey(target.getUniqueId())) {
            medalMap.put(target.getUniqueId(), medalMap.get(target.getUniqueId()) - amount);
        } else {
            medalMap.put(target.getUniqueId(), -amount);
        }
    }

    public static Long removeMedalAll(HumanEntity player) {
        return medalMap.remove(player.getUniqueId());
    }
}
