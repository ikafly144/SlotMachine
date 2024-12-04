package net.sabafly.slotmachine.v2.game.render.map;

import com.google.common.base.Preconditions;
import dev.cerus.maps.api.colormap.ColorMaps;
import dev.cerus.maps.api.graphics.ColorCache;
import dev.cerus.maps.api.graphics.MapGraphics;
import net.sabafly.slotmachine.v2.game.render.Renderer;
import net.sabafly.slotmachine.v2.game.render.Screen;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageFilter;
import java.awt.image.BufferedImageOp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MapScreenImpl<C, P, I extends BufferedImage> extends MapGraphics<C, P> implements Screen<I> {

    public static final byte COLOR = ColorMaps.latest().rgbToMapColor(0, 0, 0xff).mapColor();
    private final List<Renderer<?extends I>> renderers = new ArrayList<>();
    private final MapGraphics<C, P> graphics;
    private AtomicBoolean isRendering = new AtomicBoolean(false);

    public MapScreenImpl(MapGraphics<C, P> graphics) {
        this.graphics = graphics;
    }

    @Override
    public void draw(I image, int x, int y) {
        graphics.drawImage(image, x, y);
    }

    @Override
    public void drawText(String text, int x, int y) {
        graphics.drawText(x, y, text, COLOR, 1);
    }

    @Override
    public void drawRect(int x, int y, int w, int h, byte color) {
        graphics.drawRect(x, y, w, h, (byte)color, 1.0f);
    }

    @Override
    public void addRenderer(@NotNull Renderer<I> renderer) {
        Preconditions.checkNotNull(renderer, "Renderer cannot be null");
        Preconditions.checkArgument(!isRendering.get(), "Cannot add renderer while rendering");
        renderers.add(renderer);
        renderer.getChildren().forEach(this::addRenderer);
    }

    @Override
    public void render(Screen<? super I> screen) {
        isRendering.set(true);
        renderers.sort(Comparator.comparing(Renderer::zIndex));
        renderers.reversed();
        for (Renderer<? extends I> renderer : renderers) {
            if (renderer.isVisible()) renderer.render(screen);
        }
        renderers.clear();
        isRendering.set(false);
    }


    @Override
    public void renderOnto(C renderTarget, P params) {
        graphics.renderOnto(renderTarget, params);
    }

    @Override
    public byte setPixel(int x, int y, float alpha, byte color) {
        return graphics.setPixel(x, y, alpha, color);
    }

    @Override
    public byte getPixel(int x, int y) {
        return graphics.getPixel(x, y);
    }

    @Override
    public MapGraphics<C, P> copy() {
        return graphics.copy();
    }

    @Override
    public int getWidth() {
        return graphics.getWidth();
    }

    @Override
    public int getHeight() {
        return graphics.getHeight();
    }

}
