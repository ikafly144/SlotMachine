package net.sabafly.slotmachine.v2.game.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Box<V> {

    private final V x;
    private final V y;
    private final V w;
    private final V h;

    private Box(V x, V y, V w, V h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    @Contract(value = "_, _, _, _ -> new", pure = true)
    public static <V> @NotNull Box<V> of(V x, V y, V w, V h) {
        return new Box<>(x, y, w, h);
    }

    public <T, R> R create(@NotNull QuadFunction<V, T, R> consumer, T t) {
        return consumer.create(x, y, w, h, t);
    }

    public <T, R> R create(@NotNull TriFunction<V, T, R> consumer, T t) {
        return consumer.create(x, y, w, t);
    }

    public <T, R> R create(@NotNull BiFunction<V, T, R> consumer, T t) {
        return consumer.create(x, y, t);
    }

    public <T, R> R create(@NotNull MonoFunction<V, T, R> consumer, T t) {
        return consumer.create(x, t);
    }

    @FunctionalInterface
    public interface QuadFunction<V, T, R> {
        R create(V x, V y, V w, V h, T t);
    }

    @FunctionalInterface
    public interface TriFunction<V, T, R> {
        R create(V x, V y, V z, T t);
    }

    @FunctionalInterface
    public interface BiFunction<V, T, R> {
        R create(V x, V y, T t);
    }

    @FunctionalInterface
    public interface MonoFunction<V, T, R> {
        R create(V x, T t);
    }

}
