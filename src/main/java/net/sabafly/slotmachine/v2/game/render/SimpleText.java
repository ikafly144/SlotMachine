package net.sabafly.slotmachine.v2.game.render;

import org.jetbrains.annotations.ApiStatus;

import java.awt.*;

@ApiStatus.NonExtendable
public class SimpleText <I extends Image> implements Renderer<I> {

    private final String text;
    private final int x;
    private final int y;

    public SimpleText(String text) {
        this(text, 0, 0);
    }

    public SimpleText(String text, int x, int y) {
        this.text = text;
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(Screen<? super I> screen) {
        screen.drawText(text, x, y);
    }
}
