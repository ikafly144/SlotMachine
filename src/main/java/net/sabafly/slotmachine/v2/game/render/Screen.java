package net.sabafly.slotmachine.v2.game.render;

import org.jetbrains.annotations.ApiStatus;

import java.awt.*;

public interface Screen<I extends Image> extends Renderer<I> {

    void draw(I image, int x, int y);

    @ApiStatus.Internal
    void drawText(String text, int x, int y);

    void drawRect(int x, int y, int w, int h, byte color);

    void addRenderer(Renderer<I> renderer);

}
