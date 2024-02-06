package net.sabafly.slotmachine.game.slot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.sabafly.slotmachine.SlotMachine;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

import static net.sabafly.slotmachine.game.slot.SlotRegistry.WheelPattern.*;

public class SlotRegistry {

    public enum WheelSet {
        JUGGLER(
                new WheelPattern[]{
                        GRAPE,
                        REPLAY,
                        GRAPE,
                        BAR,
                        CHERRY,
                        GRAPE,
                        REPLAY,
                        GRAPE,
                        CLOWN,
                        SEVEN,
                        GRAPE,
                        REPLAY,
                        GRAPE,
                        CHERRY,
                        BAR,
                        GRAPE,
                        REPLAY,
                        GRAPE,
                        REPLAY,
                        SEVEN,
                        BELL,
                },
                new WheelPattern[]{
                        CLOWN,
                        CHERRY,
                        GRAPE,
                        BAR,
                        REPLAY,
                        CHERRY,
                        GRAPE,
                        BELL,
                        REPLAY,
                        CHERRY,
                        GRAPE,
                        BAR,
                        REPLAY,
                        CHERRY,
                        GRAPE,
                        BELL,
                        REPLAY,
                        CHERRY,
                        GRAPE,
                        SEVEN,
                        REPLAY,
                },
                new WheelPattern[]{
                        REPLAY,
                        BELL,
                        CLOWN,
                        GRAPE,
                        REPLAY,
                        BELL,
                        CLOWN,
                        GRAPE,
                        REPLAY,
                        BELL,
                        CLOWN,
                        GRAPE,
                        REPLAY,
                        BELL,
                        CLOWN,
                        GRAPE,
                        REPLAY,
                        BELL,
                        BAR,
                        SEVEN,
                        GRAPE,
                }
        ),
        ;
        private final WheelPattern[] left;
        private final WheelPattern[] center;
        private final WheelPattern[] right;

        WheelSet(WheelPattern[] left, WheelPattern[] center, WheelPattern[] right) {
            this.left = left;
            this.center = center;
            this.right = right;
        }

        public WheelPattern[] getLeft() {
            return left;
        }

        public WheelPattern[] getCenter() {
            return center;
        }

        public WheelPattern[] getRight() {
            return right;
        }
    }

    public enum SettingSet {
        JUGGLER(
                new Setting(128, 96, 58, 48, 1839, 64, 8192, 8977, 64),
                new Setting(169, 152, 58, 53, 1839, 64, 8192, 8977, 64),
                new Setting(196, 164, 58, 60, 1839, 64, 8192, 8977, 64),
                new Setting(221, 198, 60, 62, 1839, 64, 10886, 8977, 64),
                new Setting(255, 245, 60, 76, 1839, 64, 10886, 8977, 64),
                new Setting(255, 255, 60, 76, 1839, 64, 11338, 8977, 64)
        );
        public static final int SIZE = 6;

        private final List<Setting> settings;

        SettingSet(
                Setting setting1,
                Setting setting2,
                Setting setting3,
                Setting setting4,
                Setting setting5,
                Setting setting6
        ) {
            settings = new ArrayList<>();
            settings.add(setting1);
            settings.add(setting2);
            settings.add(setting3);
            settings.add(setting4);
            settings.add(setting5);
            settings.add(setting6);
        }

        public Setting get(int setting) {
            return settings.get(setting - 1);
        }
    }

    public record Setting(int big, int reg, int big_c, int reg_c, int bell, int cherry,
                          int grape, int replay, int clown) {
        public static Setting Debug = new Setting(0, 0, 32768, 32768, 0, 0, 0, 0, 0);
        public static Setting Bonus = new Setting(0, 0, 0, 0, 0, 0, 57344, 8192, 0);
    }

    public static class Wheel {
        final WheelPattern[] wheelPatterns;
        boolean isRunning = false;
        int count;
        final Pos pos;

        public Wheel(WheelPattern[] wheelPatterns, Pos pos) {
            this.wheelPatterns = wheelPatterns;
            this.pos = pos;
            this.count = ThreadLocalRandom.current().nextInt(wheelPatterns.length);
        }

        public BufferedImage getImage(final int range) {
            return getPattern(range).getImage();
        }

        public WheelPattern[] getPatterns() {
            ArrayList<SlotRegistry.WheelPattern> list = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                list.add(getPattern(i));
            }
            return list.toArray(new SlotRegistry.WheelPattern[]{});
        }

        public WheelPattern getPattern(int i) {
            return wheelPatterns[(count + i) % getLength()];
        }

        public int getLength() {
            return wheelPatterns.length;
        }

        public Pos getPos() {
            return pos;
        }

        public boolean isRunning() {
            return isRunning;
        }

        public boolean isStopped() {
            return !isRunning();
        }

        public void step() {
            if (isRunning()) count++;
        }

        public void stop(int i) {
            isRunning = false;
            count += i;
        }

        public void start() {
            isRunning = true;
        }

        public long getCount() {
            return count;
        }
    }

    public enum Pos {
        LEFT,
        CENTER,
        RIGHT,
        ;

        public int getIndex() {
            return this.ordinal();
        }
    }

    public enum Line {
        // スロットの揃うライン
        TOP(2, 2, 2),
        CENTER(1, 1, 1),
        BOTTOM(0, 0, 0),
        LEFT(0, 1, 2),
        RIGHT(2, 1, 0),
        ;
        final int left;
        final int center;
        final int right;

        Line(int left, int center, int right) {
            this.left = left;
            this.center = center;
            this.right = right;
        }

        public int get(Pos pos) {
            return get(pos.getIndex());
        }

        public int get(int i) {
            return switch (i) {
                case 0 -> left;
                case 1 -> center;
                case 2 -> right;
                default -> -1;
            };
        }

    }

    public enum WheelPattern {
        SEVEN(AssetImage.SEVEN),
        BAR(AssetImage.BAR),
        CHERRY(AssetImage.CHERRY),
        BELL(AssetImage.BELL),
        GRAPE(AssetImage.GRAPE),
        CLOWN(AssetImage.CLOWN),
        REPLAY(AssetImage.REPLAY),
        ;

        private final AssetImage image;

        WheelPattern(AssetImage image) {
            this.image = image;
        }

        public BufferedImage getImage() {
            return image.getImage();
        }

    }

    public enum Flag {
        F_BB(0, SEVEN, SEVEN, SEVEN),
        F_RB(0, SEVEN, SEVEN, BAR),
        F_CHERRY(1, CHERRY),
        F_BELL(14, BELL, BELL, BELL),
        F_GRAPE(8, GRAPE, GRAPE, GRAPE),
        F_REPLAY(0, REPLAY, REPLAY, REPLAY),
        F_CLOWN(10, CLOWN, CLOWN, CLOWN),
        ;

        private final WheelPattern[] wheelPatterns;
        private final int coin;
        private boolean earlyAnnounce = false;

        Flag(int coin, WheelPattern... wheelPatterns) {
            this.coin = coin;
            this.wheelPatterns = wheelPatterns;
        }

        public Flag randomEarlyAnnounce(RandomGenerator rng) {
            // 四分の一の確率で早期告知
            earlyAnnounce = rng.nextInt(4) == 0;
            return this;
        }

        public static Flag getFlag(WheelPattern... wheelPatterns) {
            outer:
            for (Flag flag : values()) {
                for (int i = 0; i < flag.getWheelPatterns().length; i++) {
                    if (wheelPatterns[i] != flag.getWheelPatterns()[i]) continue outer;
                }
                return flag;
            }
            return null;
        }

        @Nullable
        public static Flag genFlag(final RandomGenerator rng, Setting setting) {
            int i = rng.nextInt(65536);
            i -= setting.big();
            if (i < 0) return F_BB.randomEarlyAnnounce(rng);
            i -= setting.reg();
            if (i < 0) return F_RB.randomEarlyAnnounce(rng);
            i -= setting.big_c();
            if (i < 0) return F_BB.withCherry();
            i -= setting.reg_c();
            if (i < 0) return F_RB.withCherry();
            i -= setting.cherry();
            if (i < 0) return F_CHERRY;
            i -= setting.bell();
            if (i < 0) return F_BELL;
            i -= setting.grape();
            if (i < 0) return F_GRAPE;
            i -= setting.replay();
            if (i < 0) return F_REPLAY;
            i -= setting.clown();
            if (i < 0) return F_CLOWN;
            return null;
        }

        boolean hasCherry = false;

        Flag withCherry() {
            hasCherry = true;
            return this;
        }

        public void withoutCherry() {
            hasCherry = false;
        }

        public boolean isBonus() {
            return this == F_BB || this == F_RB;
        }

        public boolean isBigBonus() {
            return isBonus() && this == F_BB;
        }

        public boolean isRegularBonus() {
            return isBonus() && this == F_RB;
        }

        public boolean isCherry() {
            return hasCherry || this == F_CHERRY;
        }

        public WheelPattern[] getWheelPatterns() {
            return wheelPatterns;
        }

        public int getCoin() {
            return coin;
        }

        public boolean isEarlyAnnounce() {
            return earlyAnnounce;
        }
    }

    public static ItemStack getTicket(long coin) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("引き換え用レシート"));
        meta.lore(List.of(
                MiniMessage.miniMessage().deserialize("<gray>%s".formatted(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))),
                MiniMessage.miniMessage().deserialize("<gray>メダル<bold>%d</bold>枚".formatted(coin)),
                Component.empty(),
                MiniMessage.miniMessage().deserialize("<gray>引き換え用レシート"),
                MiniMessage.miniMessage().deserialize("<gray>当日のみ有効")
        ));
        meta.getPersistentDataContainer().set(Key.TYPE, PersistentDataType.STRING, "TICKET");
        meta.getPersistentDataContainer().set(Key.COIN, PersistentDataType.LONG, coin);
        meta.getPersistentDataContainer().set(Key.DATE, PersistentDataType.STRING, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
        meta.getPersistentDataContainer().set(Key.UNIX_TIME, PersistentDataType.LONG, System.currentTimeMillis());
        item.setItemMeta(meta);
        return item;
    }

    public static class PrizeItem {
        public static ItemStack getLargeItem() {
            ItemStack item = new ItemStack(SlotMachine.getPluginConfig().prize.largeItem);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("特殊景品（大）"));
            meta.getPersistentDataContainer().set(Key.TYPE, PersistentDataType.STRING, "PAY_OUT");
            meta.getPersistentDataContainer().set(Key.SIZE, PersistentDataType.STRING, "LARGE");
            meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
            return item;
        }

        public static ItemStack getMediumItem() {
            ItemStack item = new ItemStack(SlotMachine.getPluginConfig().prize.mediumItem);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("特殊景品（中）"));
            meta.getPersistentDataContainer().set(Key.TYPE, PersistentDataType.STRING, "PAY_OUT");
            meta.getPersistentDataContainer().set(Key.SIZE, PersistentDataType.STRING, "MEDIUM");
            meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
            return item;
        }

        public static ItemStack getSmallItem() {
            ItemStack item = new ItemStack(SlotMachine.getPluginConfig().prize.smallItem);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("特殊景品（小）"));
            meta.getPersistentDataContainer().set(Key.TYPE, PersistentDataType.STRING, "PAY_OUT");
            meta.getPersistentDataContainer().set(Key.SIZE, PersistentDataType.STRING, "SMALL");
            meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
            return item;
        }

    }

    private static final Plugin plugin = SlotMachine.getPlugin();

    public static final class Key {
        @NotNull
        public static final NamespacedKey TYPE = new NamespacedKey(plugin, "type");
        @NotNull
        public static final NamespacedKey SIZE = new NamespacedKey(plugin, "size");
        @NotNull
        public static final NamespacedKey ACTION = new NamespacedKey(plugin, "action");
        @NotNull
        public static final NamespacedKey COIN = new NamespacedKey(plugin, "coin");
        @NotNull
        public static final NamespacedKey DATE = new NamespacedKey(plugin, "date");
        @NotNull
        public static final NamespacedKey PRICE = new NamespacedKey(plugin, "price");
        @NotNull
        public static final NamespacedKey UNIX_TIME = new NamespacedKey(plugin, "unixtime");
    }

}
