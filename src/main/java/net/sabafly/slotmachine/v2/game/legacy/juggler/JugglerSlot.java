package net.sabafly.slotmachine.v2.game.legacy.juggler;

import com.fasterxml.uuid.Generators;
import dev.cerus.maps.api.Frame;
import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.font.MapFont;
import dev.cerus.maps.api.graphics.ColorCache;
import dev.cerus.maps.plugin.map.MapScreenRegistry;
import dev.cerus.maps.version.VersionAdapterFactory;
import net.sabafly.slotmachine.game.Machine;
import net.sabafly.slotmachine.v2.game.legacy.*;
import net.sabafly.slotmachine.v2.song.Song;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static net.sabafly.slotmachine.v2.game.legacy.WheelPattern.*;
import static net.sabafly.slotmachine.v2.game.legacy.juggler.JugglerRoles.R_BONUS;

public class JugglerSlot extends AbstractSlot {

    public JugglerSlot(MapScreen screen, UUID uuid) {
        super(screen, uuid);
    }

    protected Wheel left = new Wheel(new WheelPattern[]{
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
            Pos.LEFT);
    protected Wheel center = new Wheel(new WheelPattern[]{
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
            Pos.CENTER);
    protected Wheel right = new Wheel(new WheelPattern[]{
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
    },
            Pos.RIGHT);

    protected Set<Role> availableRoles = Set.of(
            JugglerRoles.R_BIG,
            JugglerRoles.R_REGULAR,
            JugglerRoles.R_CHERRY,
            JugglerRoles.R_CHERRY_B,
            JugglerRoles.R_BELL,
            JugglerRoles.R_GRAPE,
            JugglerRoles.R_CLOWN,
            JugglerRoles.R_REPLAY
    );

    protected Flag currentFlag = new Flag();
    protected int coin = 0;
    protected int bonusCoinCount = 0;
    protected int payOut = 0;
    protected JFlagGenerator flagGenerator = new JFlagGenerator();
    protected Setting setting = Setting.Debug; // TODO: Setting
    protected Bonus bonus = null;

    protected enum Bonus {
        REGULAR,
        BIG
    }

    protected Flag getFlag(Pos pos, int range) {
        var l = left.isStopped() || pos == Pos.LEFT ? left.getPatternSnapshot(pos == Pos.LEFT ? range : 0) : null;
        var c = center.isStopped() || pos == Pos.CENTER ? center.getPatternSnapshot(pos == Pos.CENTER ? range : 0) : null;
        var r = right.isStopped() || pos == Pos.RIGHT ? right.getPatternSnapshot(pos == Pos.RIGHT ? range : 0) : null;
        var flags = new Flag();
        availableRoles.forEach(flag -> {
            if (flag.combos().stream().anyMatch(combo -> combo.match(l, c, r))) {
                flags.add(flag);
            }
        });
        return flags;
    }

    protected boolean checkFlag(Wheel wheel, int range, Set<Role> roles) {
        var estRoles = getFlag(wheel.getPos(), range).getRoles();
        estRoles.removeAll(roles);
        return estRoles.isEmpty();
    }

    protected int calcStopSpot(Wheel wheel, Set<Role> roles) {
        for (int i = 0; i < 4; i++) {
            if (checkFlag(wheel, i, roles)) return i;
        }
        return 0;
    }

    @Override
    public void tick() {
        if (left.isRunning()) left.step();
        if (center.isRunning()) center.step();
        if (right.isRunning()) right.step();
    }

    @Override
    public List<UIButton> getButtons() {
        var buttons = new ArrayList<UIButton>();
        buttons.add(new UIButton(2, 64, 4, 4, (player, pos) -> {
            if (left.isRunning() || center.isRunning() || right.isRunning()) return;
            Song.START.play(getScreen().getLocation());
            currentFlag = flagGenerator.generate(setting);
            if (this.bonus != null) {
                if (this.bonus == Bonus.REGULAR) {
                    currentFlag.add(JugglerRoles.R_REGULAR);
                } else if (this.bonus == Bonus.BIG) {
                    currentFlag.add(JugglerRoles.R_BIG);
                }
            }
            left.start();
            center.start();
            right.start();
        }));
        buttons.add(new UIButton(27, 64, 23, 14, (player, pos) -> {
            if (left.isRunning()) {
                Song.CLICK_CHORD.play(getScreen().getLocation());
                left.stop(this.calcStopSpot(left, currentFlag.getRoles()));
            }
        }));
        buttons.add(new UIButton(52, 64, 23, 14, (player, pos) -> {
            if (center.isRunning()) {
                Song.CLICK_CHORD.play(getScreen().getLocation());
                center.stop(this.calcStopSpot(center, currentFlag.getRoles()));
            }
        }));
        buttons.add(new UIButton(77, 64, 23, 14, (player, pos) -> {
            if (right.isRunning()) {
                Song.CLICK_CHORD.play(getScreen().getLocation());
                right.stop(this.calcStopSpot(right, currentFlag.getRoles()));
            }
        }));
        return buttons;
    }

    @Override
    public List<UIText> getTexts() {
        List<UIText> texts = new ArrayList<>();
        texts.add(new UIText(25, 81, "" + coin, MapFont.MINECRAFT_FONT, ColorCache.rgbToMap(0xff, 0xd8, 0x00), 200));
        if (currentFlag.getRoles().stream().anyMatch(R_BONUS::contains))
            texts.add(new UIText(55, 81, "" + bonusCoinCount, MapFont.MINECRAFT_FONT, ColorCache.rgbToMap(0xff, 0xd8, 0x00), 200));
        texts.add(new UIText(88, 81, "" + payOut, MapFont.MINECRAFT_FONT, ColorCache.rgbToMap(0xff, 0xd8, 0x00), 200));
        texts.add(new UIText(40, 3, "JUGGLER", MapFont.MINECRAFT_FONT, ColorCache.rgbToMap(0xff, 0xd8, 0x00), 200));

        return texts;
    }

    @Override
    public List<UIImage> getImages() {
        var images = new ArrayList<UIImage>();
        images.add(new UIImage(0, 0, AssetImage.BASE.getImage(), 100));
        images.add(new UIImage(16, 16, getSlotImage(), 101));
        images.add(new UIImage(0, 0, AssetImage.LIGHTING.getImage(), 102));
        if (currentFlag.getRoles().stream().anyMatch(R_BONUS::contains)) images.add(new UIImage(0, 47, AssetImage.GOGO.getImage(), 103));
        return images;
    }

    protected BufferedImage getSlotImage() {
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
        final boolean shadow = (tick / 5) % 2 == 0 && false; // highlightFlag != null && highlightLine != null;
        for (final Pos pos : Pos.values()) {

            final ArrayList<BufferedImage> images = reelImages.get(pos.getIndex());

            for (int i = 0; i < images.size(); i++) {
                int y = i * height + i;
                if (getWheel(pos).isRunning() && (tick + pos.getIndex()) % 2 == 0) y -= height / 2;
                combinedGraphics.drawImage(images.get(i), pos.getIndex() * width + pos.getIndex(), y, null);
                if (shadow) // && highlightLine.get(pos) == 2 - i && highlightFlag.getWheelPatterns().length > pos.getIndex())
                    combinedGraphics.drawImage(AssetImage.SHADOW.getImage(), pos.getIndex() * width + pos.getIndex(), y, null);
            }

        }

        combinedGraphics.dispose();

        return combinedImage;
    }

    private Wheel getWheel(Pos pos) {
        return switch (pos) {
            case LEFT -> left;
            case CENTER -> center;
            case RIGHT -> right;
        };
    }

    @Override
    public void save(ConfigurationLoader<? extends ConfigurationNode> loader) throws ConfigurateException {
        ConfigurationNode node = loader.load();
        node.node("type").set(Type.JUGGLER);
        node.node("screen").set(getScreen().getId());
        loader.save(node);
    }

    public static Machine<?> load(CommentedConfigurationNode node, UUID uuid) {
        int screenId = node.node("screen").getInt();
        MapScreen screen = MapScreenRegistry.getScreen(screenId);
        if (screen == null) throw new IllegalStateException("screen is null");
        return new JugglerSlot(screen, uuid);
    }

    @Override
    public String filename() {
        return getUniqueId() + ".yml";
    }

    public Type type() {
        return Type.JUGGLER;
    }

    public static JugglerSlot create(@NotNull Entity entity) {
        if (!(entity instanceof ItemFrame itemFrame)) throw new IllegalArgumentException("entity is not ItemFrame");

        MapScreen screen = new MapScreen(MapScreenRegistry.getNextFreeId(), new VersionAdapterFactory().makeAdapter(), 1, 1);
        screen.sendMaps(true);
        Location location = itemFrame.getLocation();
        screen.setLocation(location);
        final dev.cerus.maps.api.Frame[][] frame = new dev.cerus.maps.api.Frame[][]{
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

        return new JugglerSlot(screen, Generators.timeBasedEpochGenerator().generate());
    }

}
