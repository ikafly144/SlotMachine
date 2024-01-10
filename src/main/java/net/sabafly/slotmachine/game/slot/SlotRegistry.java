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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.random.RandomGenerator;

import static net.sabafly.slotmachine.game.slot.SlotRegistry.WheelPattern.*;

public class SlotRegistry {

    public static final Wheels JUGGLER = new Wheels(
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
    );

    public static class Wheels {

        final Wheel[] patterns;
        Line highlightLine;
        Flag highlightFlag;

        public Wheels(
                final WheelPattern[] left,
                final WheelPattern[] center,
                final WheelPattern[] right
                ) {
            this.patterns = new Wheel[] {
                    new Wheel(left, Pos.LEFT),
                    new Wheel(center, Pos.CENTER),
                    new Wheel(right, Pos.RIGHT)
            };
        }

        public Wheel getWheel(final Pos pos) {
            return patterns[pos.getIndex()];
        }

        public BufferedImage getImage(long tick) {
                /*上から | 2 |
                        | 1 |
                        | 0 |*/
            ArrayList<ArrayList<BufferedImage>> reelImages = new ArrayList<>();
            for (Wheels.Pos pos : Wheels.Pos.values()) {
                ArrayList<BufferedImage> images = new ArrayList<>();
                if (getWheel(pos).isRunning() &&(tick+pos.getIndex())%2==1) images.add(getWheel(pos).getImage(3));
                images.add(getWheel(pos).getImage(2));
                images.add(getWheel(pos).getImage(1));
                images.add(getWheel(pos).getImage(0));
                reelImages.add(images);
            }
            final int width = AssetImage.ImageType.REEL_ICON.width;
            final int height = AssetImage.ImageType.REEL_ICON.height;
            final int length = reelImages.size();
            BufferedImage combinedImage = new BufferedImage(width * length + length - 1, height * length + length - 1, BufferedImage.TYPE_INT_ARGB);
            Graphics combinedGraphics = combinedImage.getGraphics();
            boolean shadow = (tick/5)%2 == 0 && highlightFlag != null && highlightLine != null;
            for (Wheels.Pos pos : Wheels.Pos.values()) {

                ArrayList<BufferedImage> images = reelImages.get(pos.getIndex());

                for (int i = 0; i < images.size(); i++) {
                    int y = i * height + i;
                    if (getWheel(pos).isRunning() &&(tick+i)%2==1) y-=height/2;
                    combinedGraphics.drawImage(images.get(i), pos.getIndex() * width + pos.getIndex(), y, null);
                    if (shadow && highlightLine.get(pos.getIndex()) == 2 - i && highlightFlag.getWheelPatterns().length > pos.getIndex())
                        combinedGraphics.drawImage(AssetImage.SHADOW.getImage(), pos.getIndex() * width + pos.getIndex(), y, null);
                }

            }

            combinedGraphics.dispose();

            return  combinedImage;
        }

        public void step() {
            for (Wheel wheel : this.patterns) {
                wheel.step();
            }
        }

        public int stoppedWheels() {
            int count = 0;
            for (Wheel wheel : this.patterns) {
                if (wheel.isStopped()) count++;
            }
            return count;
        }

        private Wheel[] stoppedReels() {
            ArrayList<Wheel> list = new ArrayList<>();
            for (Pos pos : Pos.values()) {
                if (isStopped(pos)) list.add(getWheel(pos));
            }
            return list.toArray(new Wheel[]{});
        }

        private boolean isStopped(Pos pos) {
            return getWheel(pos).isStopped();
        }

        private Flag getFlagShifted(Pos pos, int shift) {
            int left = getWheel(Pos.LEFT).getLength() + (pos == Pos.LEFT ? shift : 0);
            int center = getWheel(Pos.CENTER).getLength() + (pos == Pos.CENTER ? shift : 0);
            int right = getWheel(Pos.RIGHT).getLength() + (pos == Pos.RIGHT ? shift : 0);
            return SlotRegistry.Flag.getFlag(left, center, right);
        }

        private int getStepCount(Flag flag, Wheels.Pos pos) {
            Wheel wheel = getWheel(pos);
            if (flag != null) {
                for (int i = 0; i < wheel.getLength() - 2; i++) {
                    if (flag.isCherry()) {
                        if (pos == Pos.LEFT && getWheel(Pos.CENTER).isRunning() && getWheel(Pos.RIGHT).isRunning()) {
                            if (flag.isBonus()) {
                                if (Arrays.stream(wheel.getPatterns())
                                        .skip(i)
                                        .limit(3)
                                        .anyMatch(wheelPattern -> wheelPattern == WheelPattern.CHERRY))
                                    return i;
                            } else {
                                if (wheel.getPattern(i) == WheelPattern.CHERRY || wheel.getPattern(i + 2) == WheelPattern.CHERRY)
                                    return i;
                            }
                        } else if (pos == Pos.CENTER && getWheel(Pos.LEFT).isStopped() && getWheel(Pos.RIGHT).isRunning()) {
                            if (flag.isBonus()) {
                                if (getWheel(Pos.LEFT).getPattern(0) == WheelPattern.CHERRY || getWheel(Pos.LEFT).getPattern(2) == WheelPattern.CHERRY
                                        && Arrays.stream(wheel.getPatterns())
                                        .skip(i)
                                        .limit(3)
                                        .noneMatch(wheelPattern -> wheelPattern == WheelPattern.CHERRY))
                                    return i;
                                else if (getWheel(Pos.LEFT).getPattern(1) == WheelPattern.CHERRY)
                                    return i;
                            } else {
                                if (getWheel(Pos.LEFT).getPattern(0) == WheelPattern.CHERRY) {
                                    if (Arrays.stream(wheel.getPatterns())
                                            .skip(i)
                                            .limit(2)
                                            .anyMatch(wheelPattern -> wheelPattern == WheelPattern.CHERRY))
                                        return i;
                                } else if (getWheel(Pos.LEFT).getPattern(2) == WheelPattern.CHERRY) {
                                    if (Arrays.stream(wheel.getPatterns())
                                            .skip(i + 1)
                                            .limit(2)
                                            .anyMatch(wheelPattern -> wheelPattern == WheelPattern.CHERRY))
                                        return i;
                                }
                            }
                        } else {
                            if (pos == Pos.LEFT && Arrays.stream(wheel.getPatterns())
                                    .skip(i)
                                    .limit(3)
                                    .anyMatch(wheelPattern -> wheelPattern == WheelPattern.CHERRY))
                                return i;
                        }
                    } else if (flag.getWheelPatterns().length == 3) {
                        if (stoppedWheels() == 0 && (wheel.getPattern(i) == flag.getWheelPatterns()[pos.getIndex()] || wheel.getPattern(i + 1) == flag.getWheelPatterns()[pos.getIndex()] || wheel.getPattern(i + 2) == flag.getWheelPatterns()[pos.getIndex()])) {
                            if (pos != Pos.LEFT)
                                return i;
                            else if (wheel.getPattern(i) != WheelPattern.CHERRY && wheel.getPattern(i + 1) != WheelPattern.CHERRY && wheel.getPattern(i + 2) != WheelPattern.CHERRY)
                                return i;
                        }
                        if (stoppedWheels() == 1) {
                            if (pos == Pos.LEFT && Arrays.stream(wheel.getPatterns())
                                    .skip(i)
                                    .limit(3)
                                    .anyMatch(wheelPattern -> wheelPattern == WheelPattern.CHERRY))
                                continue;
                            Wheel[] stoppedWheels = stoppedReels();
                            WheelPattern[] stoppedWheelPatterns = stoppedWheels[0].getPatterns();
                            if (Arrays.stream(stoppedWheelPatterns)
                                    .skip(i)
                                    .limit(3)
                                    .noneMatch(wheelPattern -> wheelPattern == flag.getWheelPatterns()[stoppedWheels[0].getPos().getIndex()]))
                                continue;
                            if (stoppedWheels[0].getPos() == Pos.CENTER) {
                                final WheelPattern wheelPattern = flag.getWheelPatterns()[Pos.CENTER.getIndex()];
                                if (stoppedWheelPatterns[1] == wheelPattern && (wheel.getPattern(i) == wheelPattern || wheel.getPattern(i + 2) == wheelPattern))
                                    return i;
                                else if (stoppedWheelPatterns[0] == wheelPattern && wheel.getPattern(i) == wheelPattern)
                                    return i;
                                else if (stoppedWheelPatterns[2] == wheelPattern && wheel.getPattern(i + 2) == wheelPattern)
                                    return i;
                            } else if (pos == Pos.CENTER) {
                                final int j = stoppedWheels[0].getPos().getIndex();
                                final WheelPattern wheelPattern = flag.getWheelPatterns()[j];
                                if (stoppedWheelPatterns[1] == wheelPattern && wheel.getPattern(i + 1) == wheelPattern)
                                    return i;
                                else if (stoppedWheelPatterns[0] == wheelPattern && (wheel.getPattern(i) == wheelPattern || wheel.getPattern(i + 1) == wheelPattern))
                                    return i;
                                else if (stoppedWheelPatterns[2] == wheelPattern && (wheel.getPattern(i + 2) == wheelPattern || wheel.getPattern(i + 1) == wheelPattern))
                                    return i;
                            } else {
                                final WheelPattern wheelPattern = flag.getWheelPatterns()[pos.getIndex()];
                                if (stoppedWheelPatterns[1] == wheelPattern && wheel.getPattern(i + 1) == wheelPattern) return i;
                                else if (stoppedWheelPatterns[0] == wheelPattern && (wheel.getPattern(i) == wheelPattern || wheel.getPattern(i + 2) == wheelPattern))
                                    return i;
                                else if (stoppedWheelPatterns[2] == wheelPattern && (wheel.getPattern(i) == wheelPattern || wheel.getPattern(i + 2) == wheelPattern))
                                    return i;
                            }
                        }
                        if (stoppedWheels() == 2) {
                            if (pos == Pos.LEFT && wheel.getPattern(i) != WheelPattern.CHERRY && wheel.getPattern(i + 1) != WheelPattern.CHERRY && wheel.getPattern(i + 2) != WheelPattern.CHERRY)
                                continue;
                            for (int j = 0; j < wheel.getLength() - 2; j++) {
                                Flag f = getFlagShifted(pos, j);
                                if (f == flag) return j;
                            }
                        }
                    }
                }
            }

            // はずれる処理
            if (stoppedWheels() == 2){
                for (int i = 0; i < wheel.getLength() - 2; i++) {
                    Flag f = getFlagShifted(pos, i);
                    if (f == null) return i;
                }
            } else {
                for (int i = 0; i < wheel.getLength() - 2; i++) {
                    if (pos != Pos.LEFT)
                        return i;
                    else if (Arrays.stream(wheel.getPatterns())
                            .skip(i)
                            .limit(3)
                            .noneMatch(wheelPattern -> wheelPattern == SlotRegistry.WheelPattern.CHERRY))
                        return i;
                }
            }
            return 0;
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
            TOP(0, 0, 0),
            CENTER(1, 1, 1),
            BOTTOM(2, 2, 2),
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

            public int getLeft() {
                return left;
            }

            public int getCenter() {
                return center;
            }

            public int getRight() {
                return right;
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

    }

    public static class Wheel {
        final WheelPattern[] wheelPatterns;
        boolean isRunning = false;
        int count = 0;
        final Wheels.Pos pos;

        private Wheel(WheelPattern[] wheelPatterns, Wheels.Pos pos) {
            this.wheelPatterns = wheelPatterns;
            this.pos = pos;
        }

        private BufferedImage getImage( int range) {
            return wheelPatterns[(count+range)% wheelPatterns.length].getImage().getImage();
        }

        public WheelPattern[] getPatterns() {
            ArrayList<SlotRegistry.WheelPattern> list = new ArrayList<>();
            for (int i=0;i<7;i++) {
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

        public Wheels.Pos getPos() {
            return pos;
        }

        private boolean isRunning() {
            return isRunning;
        }

        public boolean isStopped() {
            return !isRunning();
        }

        public void step() {
            if (isRunning()) count++;
        }

    }

    public enum ReelSet {
        LEFT(new WheelPattern[]{
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
        }),
        CENTER(
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
                }),
        RIGHT(new WheelPattern[]{
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
        })
        ;
        public final WheelPattern[] wheelPatterns;
        ReelSet(WheelPattern[] wheelPatterns) {
            this.wheelPatterns = wheelPatterns;
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

        public AssetImage getImage() {
            return image;
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
            i -= setting.BB;
            if (i < 0) return F_BB.randomEarlyAnnounce(rng);
            i -= setting.RB;
            if (i < 0) return F_RB.randomEarlyAnnounce(rng);
            i -= setting.C_BB;
            if (i < 0) return F_BB.withCherry();
            i -= setting.C_RB;
            if (i < 0) return F_RB.withCherry();
            i -= setting.cherry;
            if (i < 0) return F_CHERRY;
            i -= setting.bell;
            if (i < 0) return F_BELL;
            i -= setting.grape;
            if (i < 0) return F_GRAPE;
            i -= setting.replay;
            if (i < 0) return F_REPLAY;
            i -= setting.clown;
            if (i < 0) return F_CLOWN;
            return null;
        }

        public static final int [][] stopLines = {
                {0,0,0},
                {1,1,1},
                {2,2,2},
                {0,1,2},
                {2,1,0},
        };

        boolean hasCherry = false;

        public static boolean hasFlag(int left, int center, int right) {
            Flag flag = getFlag(left, center, right);
            return flag != null;
        }

        public static Flag getFlag(int left, int center, int right) {
            for (int[] pattern : stopLines) {
                WheelPattern leftWheelPattern = ReelSet.LEFT.wheelPatterns[(left+pattern[0])% ReelSet.LEFT.wheelPatterns.length];
                WheelPattern centerWheelPattern = ReelSet.CENTER.wheelPatterns[(center+pattern[1])% ReelSet.CENTER.wheelPatterns.length];
                WheelPattern rightWheelPattern = ReelSet.RIGHT.wheelPatterns[(right+pattern[2])% ReelSet.RIGHT.wheelPatterns.length];

                Flag flag = getFlag(leftWheelPattern, centerWheelPattern, rightWheelPattern);
                if (flag != null) return flag;
            }
            return null;
        }

        public static int[] getStopLine(int left, int center, int right) {
            for (int[] stopLine : stopLines) {
                WheelPattern leftWheelPattern = ReelSet.LEFT.wheelPatterns[(left + stopLine[0]) % ReelSet.LEFT.wheelPatterns.length];
                WheelPattern centerWheelPattern = ReelSet.CENTER.wheelPatterns[(center + stopLine[1]) % ReelSet.CENTER.wheelPatterns.length];
                WheelPattern rightWheelPattern = ReelSet.RIGHT.wheelPatterns[(right + stopLine[2]) % ReelSet.RIGHT.wheelPatterns.length];

                Flag flag = getFlag(leftWheelPattern, centerWheelPattern, rightWheelPattern);
                if (flag != null) return stopLine;
            }
            return null;
        }

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

    public enum Setting {
        SETTING_1(128, 96, 58, 48, 1839, 64, 8192, 8977, 64),
        SETTING_2(169, 152, 58, 53, 1839, 64, 8192, 8977, 64),
        SETTING_3(196, 164, 58, 60, 1839, 64, 8192, 8977, 64),
        SETTING_4(221, 198, 60, 62, 1839, 64, 10886, 8977, 64),
        SETTING_5(255, 245, 60, 76, 1839, 64, 10886, 8977, 64),
        SETTING_6(255, 255, 60, 76, 1839, 64, 11338, 8977, 64),
        BONUS(0, 0, 0, 0, 0, 8192, 16384, 8192, 16384),
        ;

        public static Setting getSetting(int setting) {
            return values()[(setting-1)%6];
        }

        public final int BB;
        public final int RB;
        public final int C_BB;
        public final int C_RB;
        public final int cherry;
        public final int bell;
        public final int grape;
        public final int replay;
        public final int clown;

        Setting(int BB, int RB, int C_BB, int C_RB, int cherry, int bell, int grape, int replay, int clown) {
            this.BB = BB;
            this.RB = RB;
            this.C_BB = C_BB;
            this.C_RB = C_RB;
            this.cherry = cherry;
            this.bell = bell;
            this.grape = grape;
            this.replay = replay;
            this.clown = clown;
        }

        public int getIndex() {
            return this.ordinal()+1;
        }

    }

    public static ItemStack getTicket(int coin) {
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
        meta.getPersistentDataContainer().set(Key.COIN, PersistentDataType.INTEGER, coin);
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
