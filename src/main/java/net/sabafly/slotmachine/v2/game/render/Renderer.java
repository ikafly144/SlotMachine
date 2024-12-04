package net.sabafly.slotmachine.v2.game.render;

import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public interface Renderer<I extends Image> {

    /**
     * Render this renderer to the screen.
     * @param screen the screen to render to.
     */
    void render (Screen<? super I> screen);


    /**
     * @return the z-index of this renderer.
     *       default is {@code 0}.
     */
    @IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE)
    default int zIndex() {
        return 0;
    }

    /**
     * @return true if this renderer should be rendered.
     *        false if this {@link #render(Screen)} should not be called.
     *        default is {@code true}.
     */
    default boolean isVisible() {
        return true;
    }

    default @NotNull List<@NotNull Renderer<I>> getChildren() {
        return Collections.emptyList();
    }

}
