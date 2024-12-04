package net.sabafly.slotmachine.v2.game.render;


import java.awt.*;

public class SimpleRect  <I extends Image> implements Renderer<I> {

    private final int x;
    private final int y;
    private final int w;
    private final int h;
    private final byte color;

    public SimpleRect(int x, int y, int w, int h, byte color) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.color = color;
    }

    @Override
    public void render(Screen<? super I> screen) {
        screen.drawRect(x, y, w, h, color);
    }
}
