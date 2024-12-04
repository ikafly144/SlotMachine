package net.sabafly.slotmachine.v2.game.machine;

import dev.cerus.maps.api.colormap.ColorMaps;
import net.kyori.adventure.key.Key;
import net.sabafly.slotmachine.v2.game.asset.AssetException;
import net.sabafly.slotmachine.v2.game.asset.AssetManager;
import net.sabafly.slotmachine.v2.game.interaction.SimpleButton;
import net.sabafly.slotmachine.v2.game.render.Renderer;
import net.sabafly.slotmachine.v2.game.render.Screen;
import net.sabafly.slotmachine.v2.game.render.SimpleRect;
import net.sabafly.slotmachine.v2.game.render.SimpleText;
import net.sabafly.slotmachine.v2.game.util.Box;
import net.sabafly.slotmachine.v2.game.util.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Juggler extends BaseMachine<BufferedImage> implements StateMachine<BufferedImage> {

    @Override
    protected void doSave(ConfigurationNode node) throws SerializationException {
    }

    @Override
    protected void doLoad(ConfigurationNode node) throws SerializationException {
    }

    public static @NotNull Juggler create() throws SerializationException {
        return new Juggler();
    }

    public static @NotNull Juggler createId(@NotNull UUID id) {
        return new Juggler(id);
    }

    private Juggler() {
        super();
    }

    private Juggler(UUID uuid) {
        super(uuid);
    }

    private static final Logger LOGGER = LogUtils.getLogger();
    private int idleTicks = 0;
    private boolean idle = false;

    @Override
    public void doSetup() {
        LOGGER.debug("Juggler setup");
        setState(State.IDLE);
        addInteractionComponent(Box.of(0, 0, 100, 100)
                .create(SimpleButton::new, (pos, p) -> LOGGER.info("{} clicked: {}", p, pos)));
    }

    @Override
    public void doTick() {
        switch (getState()) {
            case IDLE -> {
                if (!idle) idleTicks++;
                if (idleTicks >= 20*60) {
                    idle = true;
                    idleTicks = 0;
                }
            }
            case SPINNING -> {
            }
        }
    }

    @Override
    public @NotNull List<@NotNull Renderer<BufferedImage>> getChildren() {
        List<Renderer<BufferedImage>> renderers = new ArrayList<>(super.getChildren());
        if (idle && getTime() % 20 != 0) return renderers;
        renderers.add(new SimpleText<>("tick: " + getTime()));
        var box = Box.of(0, 0, 100, 100);
        renderers.add(box.create(SimpleRect::new, ColorMaps.current().rgbToMapColor(0, 0, 0).mapColor()));
        return renderers;
    }

    @Override
    public void render(Screen<? super BufferedImage> screen) {
        if (idle && getTime() % 20 != 0) return;
        try {
            screen.draw(AssetManager.getInstance().getAsset(Key.key("slotmachine:background")), 0, 0);
        } catch (AssetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onStateChange(State from, State to) {
        LOGGER.debug("Juggler state change: {} -> {}", from, to);
        switch (from) {
            case IDLE -> {
                idle = false;
                idleTicks = 0;
            }
            default -> {
            }
        }
    }
}
