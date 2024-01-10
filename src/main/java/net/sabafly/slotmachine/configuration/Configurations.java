package net.sabafly.slotmachine.configuration;

import org.bukkit.Material;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "FieldCanBeLocal", "FieldMayBeFinal", "MismatchedQueryAndUpdateOfCollection", "MismatchedQueryAndUpdateOfCollection"})
@ConfigSerializable
public class Configurations extends ConfigurationPart {
    static final int CURRENT_VERSION = Transformations.VERSION_LATEST;

    @Setting(Configuration.VERSION_FIELD)
    public int version = CURRENT_VERSION;

    public Lend lend = new Lend();

    @ConfigSerializable
    public static class Lend extends ConfigurationPart {
        public int price = 100;
        public int count = 57;
        public int savedMedalMaxUsePerDay = 450;

        public Lend() {
        }
    }

    public Prize prize = new Prize();

    @ConfigSerializable
    public static class Prize extends ConfigurationPart {
        public Material largeItem = Material.GOLD_INGOT;
        public int largePrice = 100;
        public int largeSell = 50;
        public Material mediumItem = Material.IRON_INGOT;
        public int mediumPrice = 50;
        public int mediumSell = 25;
        public Material smallItem = Material.COPPER_INGOT;
        public int smallPrice = 10;
        public int smallSell = 5;
        public List<CustomPrize> customPrizes = new ArrayList<>();

        @ConfigSerializable
        public static class CustomPrize extends ConfigurationPart {
            public String name = null;
            public String lore = null;
            public Material item = Material.DIAMOND;
            public int count = 1;
            public int price = 100;
            public String playerName = "ikafly144";

            public CustomPrize() {
            }
        }

        public Prize() {
        }

    }

    public Configurations() {
    }

}
