package net.sabafly.slotmachine.game;

import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainTasks;
import dev.cerus.maps.api.Frame;
import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.font.MapFont;
import dev.cerus.maps.api.graphics.ColorCache;
import dev.cerus.maps.api.graphics.MapGraphics;
import dev.cerus.maps.plugin.map.MapScreenRegistry;
import dev.cerus.maps.util.Vec2;
import dev.cerus.maps.version.VersionAdapterFactory;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.format.TextColor;
import net.milkbowl.vault.economy.EconomyResponse;
import net.sabafly.slotmachine.SlotMachine;
import net.sabafly.slotmachine.game.slot.AssetImage;
import net.sabafly.slotmachine.game.slot.SlotRegistry;
import net.sabafly.slotmachine.game.slot.SlotRegistry.*;
import net.sabafly.slotmachine.song.Song;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public class Slot extends ParaMachine {
    private final Wheels wheels;
    private final String wheelSet;
    private Status status = Status.IDLE;
    private boolean isDebug = false;
    private Vec2 lastClickedPos = null;
    private @NotNull RamData ram = new RamData();
    private int settingId;
    private final SettingSet settings;
    private final RandomGenerator random = RandomGenerator.of("Random");

    public Status getStatus() {
        return this.status;
    }

    public Setting getSetting() {
        return settings.get(settingId);
    }

    public boolean isDebug() {
        return this.isDebug;
    }

    public int getSettingId() {
        return settingId;
    }

    public void cancelPlay() {
        ram.bonusFlag = null;
        end();
    }

    private void end() {
        wheels.stop(Pos.LEFT, null);
        wheels.stop(Pos.CENTER, null);
        wheels.stop(Pos.RIGHT, null);
        setStatus(Status.IDLE);
    }

    public void nextFlag() {
        ram.estFlag = Flag.values()[(ram.estFlag == null ? 0 : ram.estFlag.ordinal() + 1) % Flag.values().length];
    }

    public void nextSetting() {
        this.settingId++;
        if (this.settingId > SettingSet.SIZE) this.settingId = 1;
    }

    public void stop(@NotNull Player player) {
        if (ram.coin > 0) {
            ItemStack ticket = SlotRegistry.getTicket(ram.coin);
            player.updateInventory();
            player.getInventory().addItem(ticket);
            ram.coin = 0;
        }
        end();
    }

    @ConfigSerializable
    public static class RamData {
        public long coin = 0L;
        public long payOut = 0;
        public Flag estFlag = null;
        public long gameCount = 0;
        public int bonusCoinCount = 0;
        public Flag bonusFlag = null;
        public boolean bonusAnnounced = false;
        public boolean replay;
    }

    private final @NotNull Stats stats;

    @ConfigSerializable
    public static class Stats {
        public long totalPayIn = 0L;
        public long totalPayOut = 0L;
        public long totalGameCount = 0;
        public int totalBonusCount = 0;
        public int totalBonusPayOut = 0;
        public int totalBigBonusCount = 0;
    }

    Slot(final WheelSet wheelSet, final SettingSet settings, final @NotNull MapScreen screen) {
        this(wheelSet, settings, screen, new Stats(), 2);
    }

    Slot(final WheelSet wheelSet, final SettingSet settings, final @NotNull MapScreen screen, final @NotNull Stats stats, final int setting) {
        this(wheelSet, settings, screen, stats, setting, UUID.randomUUID());
    }

    Slot(final WheelSet wheelSet, final SettingSet settings, final @NotNull MapScreen screen, final @NotNull Stats stats, final int setting, final UUID uuid) {
        super(screen, uuid);
        this.wheels = new Wheels(wheelSet);
        this.wheelSet = wheelSet.name();
        this.stats = stats;
        this.settings = settings;
        this.settingId = setting;
    }

    @NotNull
    public static Slot create(final Entity entity, final WheelSet wheelSet, final SettingSet settings) {
        if (!(entity instanceof ItemFrame itemFrame)) throw new IllegalArgumentException("entity is not ItemFrame");

        MapScreen screen = new MapScreen(MapScreenRegistry.getNextFreeId(), new VersionAdapterFactory().makeAdapter(), 1, 1);
        screen.sendMaps(true);
        Location location = itemFrame.getLocation();
        screen.setLocation(location);
        final Frame[][] frame = new Frame[][]{
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
        };
        screen.setFrames(frame);
        MapScreenRegistry.registerScreen(screen);

        itemFrame.remove();

        return new Slot(wheelSet, settings, screen);
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void toggleDebug() {
        setDebug(!isDebug);
    }

    public void setDebug(boolean debug) {
        this.isDebug = debug;
    }

    public boolean isBonusNow() {
        return ram.bonusFlag != null;
    }

    public boolean isBonusAnnounced() {
        return ram.bonusAnnounced;
    }

    @Override
    public void run() {
        final MapGraphics<?, ?> graphics = getScreen().getGraphics();
        render(graphics);
        sendPlayers();

        // 払い出し処理
        if (tick % 3 == 0 && ram.payOut > 0) {
            ram.payOut--;
            ram.coin++;
            new Song(List.of(
                    new Song.Note(Sound.BLOCK_NOTE_BLOCK_BIT, 0.529732f, 1, 1f),
                    new Song.Note(Sound.BLOCK_NOTE_BLOCK_BIT, 0.529732f, 0, 1f)
            )).play(screen.getLocation());
        }

        super.run();
        if (tick % 2 == 1) wheels.step();
    }

    @Override
    public List<UIImage> getImages() {
        List<UIImage> images = new ArrayList<>();
        images.add(new UIImage(0, 0, AssetImage.BASE.getImage(), 100));
        images.add(new UIImage(16, 16, wheels.getImage(tick), 101));
        images.add(new UIImage(0, 0, AssetImage.LIGHTING.getImage(), 102));
        if (isBonusAnnounced()) images.add(new UIImage(0, 47, AssetImage.GOGO.getImage(), 103));
        return images;
    }

    @Override
    public List<UIText> getTexts() {
        List<UIText> texts = new ArrayList<>();
        texts.add(new UIText(25, 81, "" + ram.coin, MapFont.MINECRAFT_FONT, ColorCache.rgbToMap(0xff, 0xd8, 0x00), 200));
        if (isBonusNow())
            texts.add(new UIText(55, 81, "" + ram.bonusCoinCount, MapFont.MINECRAFT_FONT, ColorCache.rgbToMap(0xff, 0xd8, 0x00), 200));
        texts.add(new UIText(88, 81, "" + ram.payOut, MapFont.MINECRAFT_FONT, ColorCache.rgbToMap(0xff, 0xd8, 0x00), 200));
        texts.add(new UIText(40, 3, "JUGGLER", MapFont.MINECRAFT_FONT, ColorCache.rgbToMap(0xff, 0xd8, 0x00), 200));

        final String debug = "t:" + tick + "\nf:" + (ram.estFlag != null ? ram.estFlag : "") + "\ng:" + ram.gameCount + "\ncredit:" + ram.coin + "\npayOut:" + ram.payOut + "\nbonus:" + ram.bonusCoinCount + "\nbonusFlag:" + ram.bonusFlag + "\nbonusAnnounced:" + ram.bonusAnnounced;

        if (isDebug)
            texts.add(new UIText(0, 0, debug, MapFont.MINECRAFT_FONT, ColorCache.rgbToMap(0x22, 0x22, 0x22), 9999));

        return texts;
    }

    @Override
    public List<UIRect> getRects() {
        List<UIRect> rects = new ArrayList<>();
        if (isDebug && lastClickedPos != null)
            rects.add(new UIRect(lastClickedPos.x, lastClickedPos.y, 0, 0, ColorCache.rgbToMap(0xff, 0x00, 0xff), 9999));
        return rects;
    }

    @Override
    public List<UIButton> getButtons() {
        List<UIButton> buttons = new ArrayList<>();
        buttons.add(new UIButton(27, 64, 23, 14, (player, pos) -> {
            if (getStatus().isPlaying()) {
                Song.CLICK_CHORD.play(getScreen().getLocation());
                wheels.stop(Pos.LEFT, ram.estFlag);
            }
        }));
        buttons.add(new UIButton(52, 64, 23, 14, (player, pos) -> {
            if (getStatus().isPlaying()) {
                Song.CLICK_CHORD.play(getScreen().getLocation());
                wheels.stop(Pos.CENTER, ram.estFlag);
            }
        }));
        buttons.add(new UIButton(77, 64, 23, 14, (player, pos) -> {
            if (getStatus().isPlaying()) {
                Song.CLICK_CHORD.play(getScreen().getLocation());
                wheels.stop(Pos.RIGHT, ram.estFlag);
            }
        }));
        buttons.add(new UIButton(16, 66, 10, 10, (player, pos) -> {
            if (getStatus() == Status.IDLE && ram.payOut < 1) {
                try {
                    wheels.start();
                } catch (Exception e) {
                    player.playSound(net.kyori.adventure.sound.Sound.sound(Key.key("block.note_block.didgeridoo"), net.kyori.adventure.sound.Sound.Source.MASTER, 1, 1));
                }
            }
        }));
        buttons.add(new UIButton(113, 53, 10, 10, (player, pos) -> {
            EconomyResponse response = SlotMachine.getEconomy().withdrawPlayer(player, SlotMachine.getPluginConfig().lend.price);
            if (response.transactionSuccess()) {
                ComponentBuilder<?, ?> c =
                        Component.text().append(
                                Component.text("メダル貸出"),
                                Component.newline(),
                                Component.text("残高: " + SlotMachine.getEconomy().format(response.balance))
                        );
                player.sendMessage(c);
                SlotMachine.getPlugin().getComponentLogger().info(c.build());
                this.ram.payOut += SlotMachine.getPluginConfig().lend.count;
                this.stats.totalPayIn += SlotMachine.getPluginConfig().lend.count;
            } else {
                player.sendMessage(
                        Component.text().color(TextColor.color(0xf72424)).append(
                                Component.text("手続きが完了しませんでした"),
                                Component.newline(),
                                Component.text("残高不足: " + SlotMachine.getEconomy().format(response.balance))
                        )
                );
            }
        }));
        buttons.add(new UIButton(10, 73, 3, 3, (player, pos) -> {
            this.stats.totalPayOut += this.ram.coin;
            player.sendMessage(
                    Component.text().append(
                            Component.text("メダル払い出し"),
                            Component.newline(),
                            Component.text("総ゲーム数: " + ram.gameCount),
                            Component.newline(),
                            Component.text("総メダル数: " + ram.coin)
                    )
            );
            this.stop(player);
        }));
        buttons.add(new UIButton(116, 46, 4, 4, (player, pos) -> {
            if (MedalBank.canTakeMedal(player.getUniqueId(), SlotMachine.getPluginConfig().lend.count)) {
                MedalBank.takeMedal(player.getUniqueId(), SlotMachine.getPluginConfig().lend.count);
                this.ram.payOut += SlotMachine.getPluginConfig().lend.count;
                player.sendMessage(
                        Component.text().append(
                                Component.text("貯メダルからメダルを引き出しました"),
                                Component.newline(),
                                Component.text("残高: " + MedalBank.getMedal(player.getUniqueId()))
                        )
                );
            } else {
                player.sendMessage(
                        Component.text().color(TextColor.color(0xff0000)).append(
                                Component.text("貯メダルが足りないか、今日の上限に達しています"),
                                Component.newline(),
                                Component.text("貯メダル: " + MedalBank.getMedal(player.getUniqueId()))
                        )
                );
            }
        }));
        return buttons;
    }

    @Override
    public void onClick(Player clicked, Vec2 pos) {
        if (isDebug)
            clicked.sendMessage(Component.text().append(Component.text("clicked: " + pos.x + ", " + pos.y), Component.text(" status: " + status)).build());
        lastClickedPos = pos;
        super.onClick(clicked, pos);
    }

    @Override
    public String filename() {
        return this.uuid + ".yml";
    }

    public Flag getFlag() {
        return ram.estFlag;
    }

    public enum Status {
        IDLE(false),
        PLAYING(true),
        MAINTENANCE(false),
        ;
        final boolean isPlaying;

        Status(boolean play) {
            this.isPlaying = play;
        }

        public boolean isPlaying() {
            return isPlaying;
        }
    }

    @Override
    public void save(final ConfigurationLoader<? extends ConfigurationNode> loader) throws ConfigurateException {
        ConfigurationNode node = loader.load();
        node.node("type").set(Type.SLOT);
        node.node("wheel").set(wheelSet);
        node.node("screen").set(getScreen().getId());
        node.node("stats").set(stats);
        node.node("ram").set(ram);
        node.node("setting").set(settings);
        node.node("setting_id").set(settingId);
        loader.save(node);
    }

    public static Slot load(CommentedConfigurationNode node, final UUID uuid) throws ConfigurateException {
        WheelSet wheelSet = node.node("wheel").get(WheelSet.class);
        if (wheelSet == null) throw new IllegalArgumentException("wheelSet is null");
        SettingSet settingSet = node.node("setting").get(SettingSet.class);
        if (settingSet == null) throw new IllegalArgumentException("settingSet is null");
        int screenId = node.node("screen").getInt();
        MapScreen screen = MapScreenRegistry.getScreen(screenId);
        if (screen == null) throw new IllegalArgumentException("screen is null");
        Stats stats = node.node("stats").get(Stats.class, new Stats());
        int setting = node.node("setting_id").getInt();
        Slot slot = new Slot(wheelSet, settingSet, screen, stats, setting, uuid);
        slot.ram = node.node("ram").get(RamData.class, new RamData());
        if (slot.isBonusNow()) {
            String key = "sound" + ThreadLocalRandom.current().nextInt();
            TaskChain<?> chain = SlotMachine.newSharedChain(key);
            chain.async(slot.new BgmTask(key));
            chain.execute();
        }
        return slot;
    }


    public class Wheels {

        final Wheel[] wheels;
        Line highlightLine;
        Flag highlightFlag;
        boolean waiting;

        public Wheels(WheelSet wheelSet) {
            this.wheels = new Wheel[]{
                    new Wheel(wheelSet.getLeft(), Pos.LEFT),
                    new Wheel(wheelSet.getCenter(), Pos.CENTER),
                    new Wheel(wheelSet.getRight(), Pos.RIGHT)
            };
        }

        public Wheel getWheel(final Pos pos) {
            return wheels[pos.getIndex()];
        }

        public BufferedImage getImage(final long tick) {
            final ArrayList<ArrayList<BufferedImage>> reelImages = new ArrayList<>();
            for (final Pos pos : Pos.values()) {
                ArrayList<BufferedImage> images = new ArrayList<>();
                final Wheel wheel = getWheel(pos);
                if (wheel.isRunning() && (tick + pos.getIndex()) % 2 == 0) images.add(wheel.getImage(3));
                images.add(wheel.getImage(2));
                images.add(wheel.getImage(1));
                images.add(wheel.getImage(0));
                reelImages.add(images);
            }
            final int width = AssetImage.ImageType.REEL_ICON.width;
            final int height = AssetImage.ImageType.REEL_ICON.height;
            final int length = reelImages.size();
            final BufferedImage combinedImage = new BufferedImage(width * length + length - 1, height * length + length - 1, BufferedImage.TYPE_INT_ARGB);
            final Graphics combinedGraphics = combinedImage.getGraphics();
            final boolean shadow = (tick / 5) % 2 == 0 && highlightFlag != null && highlightLine != null;
            for (final Pos pos : Pos.values()) {

                final ArrayList<BufferedImage> images = reelImages.get(pos.getIndex());

                for (int i = 0; i < images.size(); i++) {
                    int y = i * height + i;
                    if (getWheel(pos).isRunning() && (tick + pos.getIndex()) % 2 == 0) y -= height / 2;
                    combinedGraphics.drawImage(images.get(i), pos.getIndex() * width + pos.getIndex(), y, null);
                    if (shadow && highlightLine.get(pos) == 2 - i && highlightFlag.getWheelPatterns().length > pos.getIndex())
                        combinedGraphics.drawImage(AssetImage.SHADOW.getImage(), pos.getIndex() * width + pos.getIndex(), y, null);
                }

            }

            combinedGraphics.dispose();

            return combinedImage;
        }

        public void step() {
            for (Wheel wheel : this.wheels) {
                wheel.step();
            }
        }

        public int stoppedWheels() {
            int count = 0;
            for (Wheel wheel : this.wheels) {
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

        private boolean isStopped() {
            return stoppedWheels() == 3;
        }

        private Flag getFlagShifted(Pos pos, int shift) {
            int left = getWheel(Pos.LEFT).getLength() + (pos == Pos.LEFT ? shift : 0);
            int center = getWheel(Pos.CENTER).getLength() + (pos == Pos.CENTER ? shift : 0);
            int right = getWheel(Pos.RIGHT).getLength() + (pos == Pos.RIGHT ? shift : 0);
            return getFlag(left, center, right);
        }

        public Flag getFlag() {
            return getFlag(0, 0, 0);
        }

        public Flag getFlag(int left, int center, int right) {
            for (Line line : Line.values()) {
                final Flag f = Flag.getFlag(getWheel(Pos.LEFT).getPattern(line.get(Pos.LEFT) + left), getWheel(Pos.CENTER).getPattern(line.get(Pos.CENTER) + center), getWheel(Pos.RIGHT).getPattern(line.get(Pos.RIGHT) + right));
                if (f != null) {
                    return f;
                }
            }
            return null;
        }

        private int getStepCount(Flag flag, Pos pos) {
            Wheel wheel = getWheel(pos);
            if (flag != null) {
                for (int i = 0; i < wheel.getPatterns().length - 2; i++) {
                    if (flag.isCherry() && pos == Pos.LEFT) {
                        if (Arrays.stream(wheel.getPatterns()).skip(i)
                                .limit(3)
                                .anyMatch(wheelPattern -> wheelPattern == WheelPattern.CHERRY))
                            return i;
                    } else if (flag.getWheelPatterns().length == 3) {
                        if (!Arrays.stream(wheel.getPatterns()).allMatch(wheelPattern -> (pos != Pos.LEFT || wheelPattern != WheelPattern.CHERRY)))
                            continue;
                        switch (stoppedWheels()) {
                            case 0 -> {
                                if (Arrays.stream(wheel.getPatterns()).skip(i).limit(3).anyMatch(wheelPattern -> (wheelPattern == flag.getWheelPatterns()[pos.getIndex()])))
                                    return i;
                            }
                            case 1 -> {
                                Wheel[] stoppedWheels = stoppedReels();
                                WheelPattern[] stoppedWheelPatterns = stoppedWheels[0].getPatterns();
                                if (Arrays.stream(wheel.getPatterns()).skip(i).limit(3).noneMatch(wheelPattern -> wheelPattern == flag.getWheelPatterns()[pos.getIndex()]))
                                    continue;
                                if (stoppedWheels[0].getPos() == Pos.CENTER) {
                                    final WheelPattern wheelPattern = flag.getWheelPatterns()[pos.getIndex()];
                                    if (stoppedWheelPatterns[1] == wheelPattern && (wheel.getPattern(i) == wheelPattern || wheel.getPattern(i + 2) == wheelPattern))
                                        return i;
                                    else if (stoppedWheelPatterns[0] == wheelPattern && wheel.getPattern(i) == wheelPattern)
                                        return i;
                                    else if (stoppedWheelPatterns[2] == wheelPattern && wheel.getPattern(i + 2) == wheelPattern)
                                        return i;
                                } else if (pos == Pos.CENTER) {
                                    final WheelPattern wheelPattern = flag.getWheelPatterns()[pos.getIndex()];
                                    if (stoppedWheelPatterns[1] == wheelPattern && wheel.getPattern(i + 1) == wheelPattern)
                                        return i;
                                    else if (stoppedWheelPatterns[0] == wheelPattern && (wheel.getPattern(i) == wheelPattern || wheel.getPattern(i + 1) == wheelPattern))
                                        return i;
                                    else if (stoppedWheelPatterns[2] == wheelPattern && (wheel.getPattern(i + 2) == wheelPattern || wheel.getPattern(i + 1) == wheelPattern))
                                        return i;
                                } else {
                                    final WheelPattern wheelPattern = flag.getWheelPatterns()[pos.getIndex()];
                                    if (stoppedWheelPatterns[1] == wheelPattern && wheel.getPattern(i + 1) == wheelPattern)
                                        return i;
                                    else if (stoppedWheelPatterns[0] == wheelPattern && (wheel.getPattern(i) == wheelPattern || wheel.getPattern(i + 2) == wheelPattern))
                                        return i;
                                    else if (stoppedWheelPatterns[2] == wheelPattern && (wheel.getPattern(i) == wheelPattern || wheel.getPattern(i + 2) == wheelPattern))
                                        return i;
                                }
                            }
                            case 2 -> {
                                if (pos == Pos.LEFT && Arrays.stream(wheel.getPatterns()).skip(i).limit(3).anyMatch(wheelPattern -> wheelPattern == WheelPattern.CHERRY))
                                    continue;
                                Flag f = getFlagShifted(pos, i);
                                if (f == flag) return i;
                            }
                        }
                    }
                }
            }
            // はずれる処理
            if (stoppedWheels() == 2) {
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

        public void stop(Pos pos, Flag estFlag) {
            int stepCount = getStepCount(estFlag, pos);
            getWheel(pos).stop(stepCount);
            if (isStopped()) {
                this.highlightLine = null;
                this.highlightFlag = null;
                setStatus(Status.IDLE);
                Flag flag = this.getFlag();

                if (!ram.bonusAnnounced && estFlag != null && estFlag.isBonus()) {
                    announceBonus(estFlag);
                }

                if (flag == null) {
                    Song.LOSE.play(screen.getLocation());
                }

                if (estFlag != null && flag != null) {
                    String key = "sound" + ThreadLocalRandom.current().nextInt();
                    TaskChain<?> chain = SlotMachine.newSharedChain(key);

                    if (flag == SlotRegistry.Flag.F_REPLAY) {
                        Song.REPLAY.play(screen.getLocation(), chain);
                        ram.replay = true;
                    }

                    if (flag == SlotRegistry.Flag.F_GRAPE) Song.GRAPE.play(screen.getLocation(), chain);

                    ram.estFlag = null;
                    ram.payOut += estFlag.getCoin();
                    stats.totalPayIn += estFlag.getCoin();
                    stats.totalGameCount++;

                    highlightFlag = flag;
                    highlightLine = this.getFlagLine(flag);
                    chain.execute();

                    if (isBonusNow()) {
                        ram.bonusCoinCount += estFlag.getCoin();
                        if (ram.bonusCoinCount >= (ram.bonusFlag.isBigBonus() ? 280 : ram.bonusFlag.isRegularBonus() ? 98 : 0)) {
                            ram.bonusFlag = null;
                            ram.bonusCoinCount = 0;
                            ram.bonusAnnounced = false;
                        }
                    }

                    if (flag.isBonus()) {
                        TaskChain<?> c = SlotMachine.newSharedChain(key);
                        if (flag.isBigBonus()) {
                            stats.totalBigBonusCount++;

                            Song.BIG_WIN.play(screen.getLocation(), c);
                        }
                        stats.totalBonusCount++;
                        ram.bonusAnnounced = false;
                        ram.bonusFlag = estFlag;
                        ram.bonusCoinCount = 0;
                        c.async(new BgmTask(key));
                        c.execute();
                        stats.totalBonusPayOut += estFlag.getCoin();
                    }
                }
            }
        }

        private void announceBonus(Flag estFlag) {
            ram.bonusAnnounced = true;
            new Song(List.of(
                    new Song.Note(Sound.BLOCK_PISTON_CONTRACT, 1, 0, 1)
            )).play(screen.getLocation());
            estFlag.withoutCherry();
        }

        private Line getFlagLine(Flag flag) {
            for (Line line : Line.values()) {
                if (Flag.getFlag(getWheel(Pos.LEFT).getPattern(line.get(Pos.LEFT)), getWheel(Pos.CENTER).getPattern(line.get(Pos.CENTER)), getWheel(Pos.RIGHT).getPattern(line.get(Pos.RIGHT))) == flag) {
                    return line;
                }
            }
            return null;
        }

        public void start() {
            if (waiting) return;
            if (getStatus() != Status.IDLE) return;
            final int requiredCoin = isBonusNow() ? 1 : 3;
            if (!ram.replay && ram.coin < requiredCoin) throw new IllegalStateException("not enough coin");
            if (!ram.replay) ram.coin -= requiredCoin;
            this.highlightLine = null;
            this.highlightFlag = null;
            waiting = true;
            ram.gameCount++;
            ram.replay = false;
            if (ram.estFlag == null || !ram.estFlag.isBonus())
                ram.estFlag = Flag.genFlag(random, isBonusNow() ? Setting.Bonus : settings.get(settingId));
            if (!ram.bonusAnnounced && ram.estFlag != null && ram.estFlag.isBonus()) {
                announceBonus(ram.estFlag);
            }
            TaskChain<?> chain = SlotMachine.newChain();
            if (cooldown - tick > 0) chain.delay((int) (cooldown - tick));
            chain.async(() -> {
                setStatus(Status.PLAYING);
                Song.START.play(screen.getLocation());
                for (Wheel wheel : this.wheels) {
                    wheel.start();
                }
                waiting = false;
            }).execute();
        }
    }

    public class BgmTask implements TaskChainTasks.GenericTask {
        final String key;

        private BgmTask(String key) {
            this.key = key;
        }

        @Override
        public void runGeneric() {
            TaskChain<?> c = SlotMachine.newSharedChain(key);
            c.delay(6);
            if (isBonusNow()) {
                Song.BIG_BGM.play(screen.getLocation(), c);
                c.async(this);
            } else {
                Song.BIG_END.play(screen.getLocation(), c);
            }
            c.execute();
        }

    }


}

