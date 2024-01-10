package net.sabafly.slotmachine.game.slot;

import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainTasks;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.font.MapFont;
import dev.cerus.maps.api.graphics.ColorCache;
import dev.cerus.maps.api.graphics.MapGraphics;
import dev.cerus.maps.plugin.map.MapScreenRegistry;
import dev.cerus.maps.util.Vec2;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.format.TextColor;
import net.milkbowl.vault.economy.EconomyResponse;
import net.sabafly.slotmachine.SlotMachine;
import net.sabafly.slotmachine.song.Song;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class SlotEntity implements Runnable {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private final SlotManager manager;

    public SlotRegistry.Flag getFlag() {
        return reelPattern.estFlag;
    }

    int debugFlag = 0;

    public void nextFlag() {
        reelPattern.estFlag = SlotRegistry.Flag.values()[debugFlag++% SlotRegistry.Flag.values().length];
    }

    int debugSetting = 0;

    public void nextSetting() {
        setting = SlotRegistry.Setting.values()[debugSetting++% SlotRegistry.Setting.values().length];
    }

    public Player getPlayer() {
        return player;
    }

    private Status status = Status.IDLE;

    public SlotRegistry.Setting getSetting() {
        return setting;
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getTotalPayIn() {
        return totalPayIn;
    }

    public long getTotalPayOut() {
        return totalPayOut;
    }

    public long getTotalBonus() {
        return totalBonus;
    }

    public enum Status {
        IDLE(false),
        PLAYING(true),
        MAINTENANCE(false),
        ;

        final boolean playing;

        Status(boolean playing) {
            this.playing = playing;
        }

        public boolean isPlaying() {
            return playing;
        }
    }

    private final UUID uuid;
    private final MapScreen screen;
    private final Plugin plugin;
    private @NotNull RandomGenerator rng = RandomGeneratorFactory.of("Random").create(ThreadLocalRandom.current().nextInt());
    private final BukkitTask task;
    private SlotRegistry.Setting setting;
    private String lastPlayed = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE);

    public boolean isPlaying() {
        return this.getPlayer() != null && this.status.isPlaying();
    }

    @Nullable
    public static SlotEntity load(SlotManager manager, File file, Plugin plugin) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()),StandardCharsets.UTF_8))) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            return  load(manager, json, plugin);
        }
    }

    @Nullable
    public static SlotEntity load(SlotManager manager, JsonObject json, Plugin plugin) {
        UUID uuid = UUID.fromString(json.get("uuid").getAsString());
        int screenId = json.get("screen_id").getAsInt();
        SlotRegistry.Setting setting = SlotRegistry.Setting.getSetting(json.get("setting").getAsInt());
        MapScreen screen = MapScreenRegistry.getScreen(screenId);
        if (screen == null) {
            return null;
        }
        SlotEntity entity = new SlotEntity(uuid,screen,manager,plugin,setting);
        if (json.get("pay_in") != null) entity.totalPayIn = json.get("pay_in").getAsLong();
        if (json.get("pay_out") != null) entity.totalPayOut = json.get("pay_out").getAsLong();
        if (json.get("bonus") != null) entity.totalBonus = json.get("bonus").getAsLong();
        return entity;
    }

    private static long lastId = 0L;

    public SlotEntity(UUID uuid, @NotNull MapScreen screen, SlotManager manager, Plugin plugin, SlotRegistry.Setting setting) {
        this.uuid = uuid;
        this.screen = screen;
        this.manager = manager;
        this.plugin = plugin;
        this.setting = setting;
        this.task = this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin,this, lastId++,0);
    }

    public void save() throws Exception {
        manager.getDataFolder().mkdirs();
        File file = new File(manager.getDataFolder(), getUuid() + ".json");
        JsonObject json = new JsonObject();
        json.addProperty("uuid", getUuid().toString());
        json.addProperty("screen_id", screen.getId());
        json.addProperty("setting", setting.getIndex());
        json.addProperty("pay_in", getTotalPayIn());
        json.addProperty("pay_out", getTotalPayOut());
        json.addProperty("bonus", getTotalBonus());
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8))) {
            pw.println(GSON.toJson(json));
            pw.flush();
        }
    }

    public void destroy() {
        this.task.cancel();
        final Set<UUID> viewers = manager.getScreenViewerMap().computeIfAbsent(screen.getId(), $ -> new HashSet<>());
        viewers.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(screen::destroyFrames);
        viewers.clear();
        manager.removeMap(this);
        cancelPlay();
    }

    public void start(Player player) throws IllegalStateException {
        if (this.isPlaying()) {
            throw new IllegalStateException("already playing!");
        }
        this.manager.registerPlayingPlayer(player, this);
        this.status = Status.PLAYING;
        this.coin = 0;
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE);
        if (!lastPlayed.equals(now)) {
            this.gameCount = 0;
        }
        this.rng = RandomGeneratorFactory.of("Random").create(ThreadLocalRandom.current().nextInt());
        rng.nextDouble();
        rng.nextInt();
        this.lastPlayed = now;
        this.waiting = false;
        this.cooldown = timeTick;
        reelPattern.endBonus();
        this.player = player;
    }

    public void stop(Player player) {
        if (this.getPlayer() == null) return;
        if (coin > 0) {
            ItemStack ticket = SlotRegistry.getTicket(coin);
            player.updateInventory();
            player.getInventory().addItem(ticket);
        }
        cancelPlay();
    }

    public void cancelPlay() {
        if (this.getPlayer() == null) return;
        manager.leavePlayer(this.getPlayer(), true);
        player = null;
        status = Status.IDLE;
        reelPattern.endBonus();
    }

    private long timeTick = 0L;
    private long cooldown = 0L;
    private boolean waiting = false;
    private long gameCount = 0L;
    private Player player;
    private long totalPayIn = 0L;
    private long totalPayOut = 0L;
    private long totalBonus = 0L;

    private final ReelPattern reelPattern = new ReelPattern();

    private class ReelPattern {

        final Reel[] reels = {
                new Reel(SlotRegistry.ReelSet.LEFT.wheelPatterns, ReelPos.LEFT),
                new Reel(SlotRegistry.ReelSet.CENTER.wheelPatterns, ReelPos.CENTER),
                new Reel(SlotRegistry.ReelSet.RIGHT.wheelPatterns, ReelPos.RIGHT),
        };

        public Reel getReel(ReelPos pos) {
            return reels[pos.getIndex()];
        }

        public long getBonusGameCount() {
            return bonusGameCount;
        }

        public long getBonusStartGame() {
            return bonusStartGame;
        }

        public long getLastBonusGame() {
            return lastBonusGame;
        }

        enum ReelPos {
            LEFT(0),
            CENTER(1),
            RIGHT(2),
            ;
            final int index;

            ReelPos(int index) {
                this.index = index;
            }

            public int getIndex() {
                return index;
            }
        }

        private class Reel {

            private final SlotRegistry.WheelPattern[] wheelPatterns;
            private int reelCount;
            private Boolean stopped = true;
            private int stoppedReelCount;
            private final ReelPos pos;

            private Reel(SlotRegistry.WheelPattern[] wheelPatterns, ReelPos pos) {
                this.wheelPatterns = wheelPatterns;
                this.reelCount = rng.nextInt(wheelPatterns.length);
                this.pos = pos;
            }

            private BufferedImage getImage(Integer range) {
                return wheelPatterns[(getReelCount()+range)% wheelPatterns.length].getImage().getImage();
            }

            private void step() {
                if (!this.stopped) {
                    reelCount++;
                }
            }

            private void stop(int step) {
                this.stopped = true;
                this.reelCount += step;
                this.stoppedReelCount = this.reelCount;
            }

            private void stop() {
                this.stop(0);
            }

            private void start() {
                this.stopped = false;
                this.reelCount = rng.nextInt(wheelPatterns.length);
            }

            private SlotRegistry.WheelPattern[] getReelPattern() {
                ArrayList<SlotRegistry.WheelPattern> list = new ArrayList<>();
                for (int i=0;i<7;i++) {
                    list.add(this.wheelPatterns[(getReelCount()+i)% wheelPatterns.length]);
                }
                return list.toArray(new SlotRegistry.WheelPattern[]{});
            }

            private boolean isRunning() {
                return !this.isStopped();
            }

            private boolean isStopped() {
                return this.stopped;
            }

            private int getReelCount() {
                return isStopped() ? stoppedReelCount : reelCount;
            }

            public ReelPos getPos() {
                return pos;
            }
        }

        public BufferedImage getImage() {
                /*上から | 2 |
                        | 1 |
                        | 0 |*/
            ArrayList<ArrayList<BufferedImage>> reelImages = new ArrayList<>();
            for (ReelPos pos : ReelPos.values()) {
                ArrayList<BufferedImage> images = new ArrayList<>();
                if (!getReel(pos).isStopped()&&(timeTick+pos.getIndex())%2==1) images.add(getReel(pos).getImage(3));
                images.add(getReel(pos).getImage(2));
                images.add(getReel(pos).getImage(1));
                images.add(getReel(pos).getImage(0));
                reelImages.add(images);
            }
            final int width = AssetImage.ImageType.REEL_ICON.width;
            final int height = AssetImage.ImageType.REEL_ICON.height;
            final int length = reelImages.size();
            BufferedImage combinedImage = new BufferedImage(width * length + length - 1, height * length + length - 1, BufferedImage.TYPE_INT_ARGB);
            Graphics combinedGraphics = combinedImage.getGraphics();
            boolean shadow = (timeTick/5)%2 == 0 && reelPattern.highlightFlag != null && reelPattern.highlightLine != null;
            for (ReelPos pos : ReelPos.values()) {

                ArrayList<BufferedImage> images = reelImages.get(pos.getIndex());

                for (int i = 0; i < images.size(); i++) {
                    int y = i * height + i;
                    if (!getReel(pos).isStopped()&&(timeTick+i)%2==1) y-=height/2;
                    combinedGraphics.drawImage(images.get(i), pos.getIndex() * width + pos.getIndex(), y, null);
                    if (shadow && reelPattern.highlightLine[pos.getIndex()] == reelPattern.highlightLine.length - 1 - i && reelPattern.highlightFlag.getWheelPatterns().length > pos.getIndex())
                        combinedGraphics.drawImage(AssetImage.SHADOW.getImage(), pos.getIndex() * width + pos.getIndex(), y, null);
                }

            }

            combinedGraphics.dispose();

            return  combinedImage;
        }


        public void step() {
            for (ReelPos pos : ReelPos.values()) {
                getReel(pos).step();
            }
        }

        private boolean isStopped() {
            for (ReelPos pos : ReelPos.values()) {
                if (isRunning(pos)) return false;
            }
            return true;
        }

        private boolean isRunning() {
            return !isStopped();
        }

        private boolean isStopped(ReelPos pos) {
            return getReel(pos).isStopped();
        }

        private boolean isRunning(ReelPos pos) {
            return getReel(pos).isRunning();
        }

        SlotRegistry.Flag estFlag;
        SlotRegistry.Flag highlightFlag;
        int [] highlightLine;

        private void startGame() {
            if (waiting) return;
            if (coin < 3) throw new IllegalStateException("not enough coin");
            if (!replay) coin -= isBonus() ? 1 : 3;
            replay = false;
            gameCount++;
            waiting = true;
            highlightFlag = null;
            TaskChain<?> chain = SlotMachine.newChain();
            if (cooldown - timeTick > 0) chain.delay((int) (cooldown - timeTick));
            chain
                .async(() -> {
                    waiting = false;
                    Song.START.play(screen.getLocation());
                    if (isBonus()) {
                        bonusGameCount++;
                        estFlag = SlotRegistry.Flag.genFlag(rng, SlotRegistry.Setting.BONUS);
                    } else {
                        if (estFlag == null || !estFlag.isBonus()) estFlag = SlotRegistry.Flag.genFlag(rng, setting);
                        if (estFlag != null && estFlag.isEarlyAnnounce()) announced = true;
                    }
                    for (ReelPos pos : ReelPos.values()) {
                        getReel(pos).start();
                    }
                });
            chain.execute();
        }

        private SlotRegistry.Flag bonus = null;
        private long bonusGameCount = 0;
        private long bonusStartGame = 0;
        private long lastBonusGame = 0;
        private int bonusCoinCount = 0;
        private boolean replay = false;

        private boolean isBonus() {
            return bonus != null;
        }

        private boolean announced = false;

        private boolean isEstBonus() {
            return estFlag != null && estFlag.isBonus();
        }

        private boolean isAnnounceBonus() {
            return isEstBonus() && announced;
        }

        private void stop(ReelPos pos) {
            Song.CLICK_CHORD.play(screen.getLocation());
            int stepCount = getStepCount(this.estFlag, pos);
            this.getReel(pos).stop(stepCount);
            if (this.isStopped()) {
                if (hasFlag()) {
                    SlotRegistry.Flag flag = getFlag();
                    if (isDebug()) getPlayer().sendPlainMessage("win" + (this.estFlag != null ? " " + this.estFlag : ""));
                    if (flag.isBonus()) {
                        announced = false;
                        String key = "bonus" + ThreadLocalRandom.current().nextInt();
                        TaskChain<?> chain = SlotMachine.newSharedChain(key);
                        Song.BIG_WIN.play(screen.getLocation(), chain);
                        bonus = flag;
                        bonusStartGame = gameCount;
                        bonusCoinCount = 0;
                        totalBonus++;
                        chain.execute();
                        TaskChain<?> c = SlotMachine.newSharedChain(key);
                        Song.BIG_BGM.play(screen.getLocation(), c);
                        c.async(new Task(key));
                        c.execute();
                    }

                    // ボーナス中の処理
                    if (isBonus()) {
                        bonusCoinCount += flag.getCoin();
                        if (bonus == SlotRegistry.Flag.F_BB && bonusCoinCount >= 280) endBonus();
                        if (bonus == SlotRegistry.Flag.F_RB && bonusCoinCount >= 98) endBonus();
                    }

                    addCoin(flag.getCoin());

                    if (flag.isCherry() && estFlag.isBonus()) {
                        announced = true;
                        new Song(List.of(
                                new Song.Note(Sound.BLOCK_PISTON_CONTRACT, 1, 0, 1)
                        )).play(screen.getLocation());
                    }
                    if (flag == SlotRegistry.Flag.F_REPLAY) {
                        Song.REPLAY.play(screen.getLocation());
                        replay = true;
                    }
                    if (flag == SlotRegistry.Flag.F_GRAPE) Song.GRAPE.play(screen.getLocation());

                    estFlag = null;
                    highlightFlag = flag;
                    highlightLine = SlotRegistry.Flag.getStopLine(getReel(ReelPos.LEFT).getReelCount(), getReel(ReelPos.CENTER).getReelCount(), getReel(ReelPos.RIGHT).getReelCount());
                }
                else {
                    if (isDebug()) getPlayer().sendPlainMessage("lose");
                    // TODO: はずれサウンド
                    if (estFlag == null || !estFlag.isBonus())
                        this.estFlag = null;
                    if (estFlag != null && !estFlag.isCherry() && !announced) {
                        announced = true;
                        new Song(List.of(
                                new Song.Note(Sound.BLOCK_PISTON_CONTRACT, 1, 0, 1)
                        )).play(screen.getLocation());
                    }
                }

            }
        }

        private void endBonus() {
            bonus = null;
            lastBonusGame = gameCount;
        }

        private class Task implements TaskChainTasks.GenericTask {
            final String key;
            private Task(String key) {
                this.key = key;
            }

            @Override
            public void runGeneric() {
                TaskChain<?> c = SlotMachine.newSharedChain(key);
                c.delay(6);
                if (isBonus()) {
                    Song.BIG_BGM.play(screen.getLocation(), c);
                    c.async(this);
                } else {
                    Song.BIG_END.play(screen.getLocation(), c);
                }
                c.execute();
            }

        }

        private int runningReelCount() {
            int i = 0;
            for (ReelPos pos : ReelPos.values()) {
                if (isRunning(pos)) i++;
            }
            return i;
        }

        private int stoppedReelCount() {
            int i = 0;
            for (ReelPos pos : ReelPos.values()) {
                if (isStopped(pos)) i++;
            }
            return i;
        }

        private Reel[] stoppedReels() {
            ArrayList<Reel> list = new ArrayList<>();
            for (ReelPos pos : ReelPos.values()) {
                if (isStopped(pos)) list.add(getReel(pos));
            }
            return list.toArray(new Reel[]{});
        }

        private int getStepCount(SlotRegistry.Flag flag, ReelPos pos) {
            SlotRegistry.WheelPattern[] wheelPatterns = getReel(pos).getReelPattern();
            if (flag != null) {
                for (int i = 0; i < wheelPatterns.length - 2; i++) {
                    if (flag.isCherry()) {
                        if (pos == ReelPos.LEFT && getReel(ReelPos.CENTER).isRunning() && getReel(ReelPos.RIGHT).isRunning()) {
                            if (flag.isBonus()) {
                                if (Arrays.stream(wheelPatterns)
                                        .skip(i)
                                        .limit(3)
                                        .anyMatch(wheelPattern -> wheelPattern == SlotRegistry.WheelPattern.CHERRY))
                                    return i;
                            } else {
                                if (wheelPatterns[i] == SlotRegistry.WheelPattern.CHERRY || wheelPatterns[i + 2] == SlotRegistry.WheelPattern.CHERRY)
                                    return i;
                            }
                        } else if (pos == ReelPos.CENTER && getReel(ReelPos.LEFT).isStopped() && getReel(ReelPos.RIGHT).isRunning()) {
                            if (flag.isBonus()) {
                                if (getReel(ReelPos.LEFT).getReelPattern()[0] == SlotRegistry.WheelPattern.CHERRY || getReel(ReelPos.LEFT).getReelPattern()[2] == SlotRegistry.WheelPattern.CHERRY && Arrays.stream(wheelPatterns)
                                        .skip(i)
                                        .limit(3)
                                        .noneMatch(wheelPattern -> wheelPattern == SlotRegistry.WheelPattern.CHERRY))
                                    return i;
                                else if (getReel(ReelPos.LEFT).getReelPattern()[1] == SlotRegistry.WheelPattern.CHERRY)
                                    return i;
                            } else {
                                if (getReel(ReelPos.LEFT).getReelPattern()[0] == SlotRegistry.WheelPattern.CHERRY) {
                                    if (Arrays.stream(wheelPatterns)
                                            .skip(i)
                                            .limit(2)
                                            .anyMatch(wheelPattern -> wheelPattern == SlotRegistry.WheelPattern.CHERRY))
                                        return i;
                                } else if (getReel(ReelPos.LEFT).getReelPattern()[2] == SlotRegistry.WheelPattern.CHERRY) {
                                    if (Arrays.stream(wheelPatterns)
                                            .skip(i + 1)
                                            .limit(2)
                                            .anyMatch(wheelPattern -> wheelPattern == SlotRegistry.WheelPattern.CHERRY))
                                        return i;
                                }
                            }
                        } else {
                            if (pos == ReelPos.LEFT && Arrays.stream(wheelPatterns)
                                    .skip(i)
                                    .limit(3)
                                    .anyMatch(wheelPattern -> wheelPattern == SlotRegistry.WheelPattern.CHERRY))
                                return i;
                        }
                    } else if (flag.getWheelPatterns().length == 3) {
                        if (stoppedReelCount() == 0 && (wheelPatterns[i] == flag.getWheelPatterns()[pos.getIndex()] || wheelPatterns[i + 1] == flag.getWheelPatterns()[pos.getIndex()] || wheelPatterns[i + 2] == flag.getWheelPatterns()[pos.getIndex()])) {
                            if (pos != ReelPos.LEFT)
                                return i;
                            else if (wheelPatterns[i] != SlotRegistry.WheelPattern.CHERRY && wheelPatterns[i + 1] != SlotRegistry.WheelPattern.CHERRY && wheelPatterns[i + 2] != SlotRegistry.WheelPattern.CHERRY)
                                return i;
                        }
                        if (stoppedReelCount() == 1) {
                            if (pos == ReelPos.LEFT && Arrays.stream(wheelPatterns)
                                    .skip(i)
                                    .limit(3)
                                    .anyMatch(wheelPattern -> wheelPattern == SlotRegistry.WheelPattern.CHERRY))
                                continue;
                            Reel[] stoppedReels = stoppedReels();
                            SlotRegistry.WheelPattern[] stoppedWheelPatterns = stoppedReels[0].getReelPattern();
                            if (Arrays.stream(stoppedWheelPatterns)
                                    .skip(i)
                                    .limit(3)
                                    .noneMatch(wheelPattern -> wheelPattern == flag.getWheelPatterns()[stoppedReels[0].getPos().getIndex()]))
                                continue;
                            if (stoppedReels[0].getPos() == ReelPos.CENTER) {
                                final SlotRegistry.WheelPattern wheelPattern = flag.getWheelPatterns()[ReelPos.CENTER.getIndex()];
                                if (stoppedWheelPatterns[1] == wheelPattern && (wheelPatterns[i] == wheelPattern || wheelPatterns[i + 2] == wheelPattern))
                                    return i;
                                else if (stoppedWheelPatterns[0] == wheelPattern && wheelPatterns[i] == wheelPattern)
                                    return i;
                                else if (stoppedWheelPatterns[2] == wheelPattern && wheelPatterns[i + 2] == wheelPattern)
                                    return i;
                            } else if (pos == ReelPos.CENTER) {
                                final int j = stoppedReels[0].getPos().getIndex();
                                final SlotRegistry.WheelPattern wheelPattern = flag.getWheelPatterns()[j];
                                if (stoppedWheelPatterns[1] == wheelPattern && wheelPatterns[i + 1] == wheelPattern)
                                    return i;
                                else if (stoppedWheelPatterns[0] == wheelPattern && (wheelPatterns[i] == wheelPattern || wheelPatterns[i + 1] == wheelPattern))
                                    return i;
                                else if (stoppedWheelPatterns[2] == wheelPattern && (wheelPatterns[i + 2] == wheelPattern || wheelPatterns[i + 1] == wheelPattern))
                                    return i;
                            } else {
                                final SlotRegistry.WheelPattern wheelPattern = flag.getWheelPatterns()[pos.getIndex()];
                                if (stoppedWheelPatterns[1] == wheelPattern && wheelPatterns[i + 1] == wheelPattern) return i;
                                else if (stoppedWheelPatterns[0] == wheelPattern && (wheelPatterns[i] == wheelPattern || wheelPatterns[i + 2] == wheelPattern))
                                    return i;
                                else if (stoppedWheelPatterns[2] == wheelPattern && (wheelPatterns[i] == wheelPattern || wheelPatterns[i + 2] == wheelPattern))
                                    return i;
                            }
                        }
                        if (stoppedReelCount() == 2) {
                            if (pos == ReelPos.LEFT && wheelPatterns[i] != SlotRegistry.WheelPattern.CHERRY && wheelPatterns[i + 1] != SlotRegistry.WheelPattern.CHERRY && wheelPatterns[i + 2] != SlotRegistry.WheelPattern.CHERRY)
                                continue;
                            for (int j = 0; j < wheelPatterns.length - 2; j++) {
                                SlotRegistry.Flag f = getFlagShifted(pos, j);
                                if (f == flag) return j;
                            }
                        }
                    }
                }
            }

            // はずれる処理
            if (stoppedReelCount() == 2){
                for (int i = 0; i < wheelPatterns.length - 2; i++) {
                    SlotRegistry.Flag f = getFlagShifted(pos, i);
                        if (f == null) return i;
                }
            } else {
                for (int i = 0; i < wheelPatterns.length - 2; i++) {
                    if (pos != ReelPos.LEFT)
                        return i;
                    else if (Arrays.stream(wheelPatterns)
                            .skip(i)
                            .limit(3)
                            .noneMatch(wheelPattern -> wheelPattern == SlotRegistry.WheelPattern.CHERRY))
                        return i;
                }
            }
            return 0;
        }

        private boolean hasFlag() {
            int left = getReel(ReelPos.LEFT).getReelCount();
            int center = getReel(ReelPos.CENTER).getReelCount();
            int right = getReel(ReelPos.RIGHT).getReelCount();
            return SlotRegistry.Flag.hasFlag(left, center, right);
        }

        @Nullable
        private SlotRegistry.Flag getFlagShifted(ReelPos pos, int i) {
            int left = getReel(ReelPos.LEFT).getReelCount() + (pos == ReelPos.LEFT ? i : 0);
            int center = getReel(ReelPos.CENTER).getReelCount() + (pos == ReelPos.CENTER ? i : 0);
            int right = getReel(ReelPos.RIGHT).getReelCount() + (pos == ReelPos.RIGHT ? i : 0);
            return SlotRegistry.Flag.getFlag(left, center, right);
        }
    }

    private static final double MAX_DIST = Math.pow(48, 2);
    private boolean isDebug = false;

    public void setDebug(boolean debug) {
        isDebug = debug;
    }

    public boolean isDebug() {
        return isDebug;
    }

    @Override
    public void run() {
        final MapGraphics<?, ?> graphics = screen.getGraphics();
        switch (status) {
            case IDLE -> {
                graphics.fillComplete(ColorCache.rgbToMap(0xff,0xff,0xff));
                graphics.drawImage(AssetImage.IDLE.getImage(), 0, 0);
            }
            case PLAYING -> {
                graphics.fillRect(0, 0, 128, 128, ColorCache.rgbToMap(0xff, 0xff, 0xff), 0xff);
                graphics.drawImage(AssetImage.BASE.getImage(), 0, 0);
                graphics.drawImage(reelPattern.getImage(), 16, 16);
                if (isDebug) graphics.drawText(0, 0, "t:" + timeTick + "\nf:" + (reelPattern.estFlag != null ? reelPattern.estFlag.toString() : "") + "\ng:" + gameCount + "\ncredit:" + coin, MapFont.MINECRAFT_FONT, ColorCache.rgbToMap(0, 0, 0xff), 1);
                graphics.drawText(25, 81, "" + coin, MapFont.MINECRAFT_FONT, ColorCache.rgbToMap(0xff, 0xd8, 0x00), 1);
                if (reelPattern.isBonus()) graphics.drawText(55, 81, "" + reelPattern.bonusCoinCount, MapFont.MINECRAFT_FONT, ColorCache.rgbToMap(0xff, 0xd8, 0x00), 1);
                graphics.drawText(88, 81, "" + payOut, MapFont.MINECRAFT_FONT, ColorCache.rgbToMap(0xff, 0xd8, 0x00), 1);
                graphics.drawText(40, 3, "JUGGLER", MapFont.MINECRAFT_FONT, ColorCache.rgbToMap(0xff, 0xd8, 0x00), 1);
                graphics.drawImage(AssetImage.LIGHTING.getImage(), 0, 0);
                if (reelPattern.isAnnounceBonus()) graphics.drawImage(AssetImage.GOGO.getImage(), 0, 47);
                timeTick++;
                reelPattern.step();
            }
            case MAINTENANCE -> {
                graphics.fillComplete(ColorCache.rgbToMap(0xff,0xff,0xff));
                graphics.drawText(0, 48, "CALL STAFF", ColorCache.rgbToMap(0xff, 0xff, 0xff), 0);
            }
        }
        if (isDebug && lastClickedPos != null) graphics.drawRect(lastClickedPos.x, lastClickedPos.y, 0, 0, ColorCache.rgbToMap(0xff, 0x00, 0xff), 1);
        sendPlayers();
    }

    private void sendPlayers() {
        final Collection<Player> receivers = new HashSet<>();
        final Set<UUID> viewers = manager.getScreenViewerMap().computeIfAbsent(screen.getId(), $ -> new HashSet<>());
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(screen.getLocation().getWorld())) {
                final double dist = player.getLocation().distanceSquared(screen.getLocation());
                if (viewers.contains(player.getUniqueId()) && dist > MAX_DIST) {
                    // Remove
                    viewers.remove(player.getUniqueId());
                    screen.destroyFrames(player);
                } else if (!viewers.contains(player.getUniqueId()) && dist < MAX_DIST) {
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

    private Vec2 lastClickedPos;
    private int coin = 0;
    private int payOut = 0;

    private void addCoin(int coin) {
        TaskChain<?> chain =SlotMachine.newChain();
        this.payOut = Math.max(coin - 16, 0);
        this.coin += coin - Math.min(coin, 16);
        cooldown = timeTick + 48;
        for (int i = 0; i < Math.min(coin, 16); i++) {
            chain
                .async(() -> {
                    new Song(List.of(
                            new Song.Note(Sound.BLOCK_NOTE_BLOCK_BIT, 0.529732f, 1, 1f),
                            new Song.Note(Sound.BLOCK_NOTE_BLOCK_BIT, 0.529732f, 0, 1f)
                    )).play(screen.getLocation());
                    this.coin += 1;
                    this.payOut += 1;
                })
                .delay(3);
        }
        chain.async(() -> this.payOut = 0);
        chain.execute();
    }

    public void clicked(Vec2 pos, Player player) {
        lastClickedPos = pos;
        if (!isPlaying()) return;

        UI.Button button = UI.Button.getButton(pos);
        if (button == null) return;

        if (isDebug()) plugin.getLogger().info(button.toString());

        switch (button) {
            case STOP_1 -> {
                if (reelPattern.isRunning(ReelPattern.ReelPos.LEFT)) this.reelPattern.stop(ReelPattern.ReelPos.LEFT);
            }
            case STOP_2 -> {
                if (reelPattern.isRunning(ReelPattern.ReelPos.CENTER)) this.reelPattern.stop(ReelPattern.ReelPos.CENTER);
            }
            case STOP_3 -> {
                if (reelPattern.isRunning(ReelPattern.ReelPos.RIGHT)) this.reelPattern.stop(ReelPattern.ReelPos.RIGHT);
            }
            case PLAY -> {
                try {
                    if (reelPattern.isStopped())
                        this.reelPattern.startGame();
                } catch (IllegalStateException e) {
                    getPlayer().playSound(net.kyori.adventure.sound.Sound.sound(Key.key("block.note_block.didgeridoo"), net.kyori.adventure.sound.Sound.Source.MASTER, 1, 1));
                }
            }
            case LEND -> {
                EconomyResponse response = SlotMachine.getEconomy().withdrawPlayer(player, SlotMachine.getPluginConfig().lend.price);
                if (response.transactionSuccess()) {
                    ComponentBuilder<?, ?> c =
                            Component.text().append(
                                    Component.text("メダル貸出"),
                                    Component.newline(),
                                    Component.text("残高: " + SlotMachine.getEconomy().format(response.balance))
                            );
                    player.sendMessage(c);
                    plugin.getComponentLogger().info(c.build());
                    this.totalPayIn += SlotMachine.getPluginConfig().lend.count;
                    addCoin(SlotMachine.getPluginConfig().lend.count);
                } else {
                    player.sendMessage(
                        Component.text().color(TextColor.color(0xff0000)).append(
                            Component.text("手続きが完了しませんでした"),
                            Component.newline(),
                            Component.text("残高不足: " + SlotMachine.getEconomy().format(response.balance))
                        )
                    );
                }
            }
            case USE_SAVED -> {
                if (manager.canTakeMedal(player, SlotMachine.getPluginConfig().lend.count)) {
                    manager.takeMedal(player, SlotMachine.getPluginConfig().lend.count);
                    addCoin(SlotMachine.getPluginConfig().lend.count);
                    player.sendMessage(
                        Component.text().append(
                            Component.text("貯メダルからメダルを引き出しました"),
                            Component.newline(),
                            Component.text("残高: " + manager.getMedal(player))
                        )
                    );
                } else {
                    player.sendMessage(
                        Component.text().color(TextColor.color(0xff0000)).append(
                            Component.text("貯メダルが足りないか、今日の上限に達しています"),
                            Component.newline(),
                            Component.text("貯メダル: " + manager.getMedal(player))
                        )
                    );
                }
            }
            case PAY_OUT -> {
                this.stop(player);
                player.sendMessage(
                    Component.text().append(
                        Component.text("メダル払い出し"),
                        Component.newline(),
                        Component.text("総ゲーム数: " + gameCount),
                        Component.newline(),
                        Component.text("総メダル数: " + coin)
                    )
                );
                this.totalPayOut += coin;
            }
        }

    }

    static class UI {
        private enum Button {
            STOP_1(new Vec2(27, 64), new Vec2(23, 14)),
            STOP_2(new Vec2(52, 64), new Vec2(23, 14)),
            STOP_3(new Vec2(77, 64), new Vec2(23, 14)),
            PLAY(new Vec2(16, 66), new Vec2(10, 10)),
            LEND(new Vec2(113, 53), new Vec2(10, 10)),
            PAY_OUT(new Vec2(10, 73), new Vec2(3, 3)),
            USE_SAVED(new Vec2(116, 46), new Vec2(4, 4)),
            ;

            @Nullable
            public static Button getButton(Vec2 clickedPos) {
                for (Button value : Button.values()) {
                    if (value.start.x <= clickedPos.x && value.start.y <= clickedPos.y && clickedPos.x <= value.end.x && clickedPos.y <= value.end.y)
                        return  value;
                }
                return null;
            }

            final Vec2 start;
            final Vec2 end;
            final Vec2 size;
            Button(Vec2 start, Vec2 size) {
                this.start = start;
                size.x--;
                size.y--;
                this.end = new Vec2(start.x + size.x, start.y + size.y);
                this.size = size;
            }

            public Vec2 getEnd() {
                return end;
            }

            public Vec2 getSize() {
                return size;
            }

            public Vec2 getStart() {
                return start;
            }

        }
    }

    public MapScreen getScreen() {
        return screen;
    }
}
