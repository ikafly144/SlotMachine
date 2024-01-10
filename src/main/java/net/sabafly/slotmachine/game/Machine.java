package net.sabafly.slotmachine.game;

import org.spongepowered.configurate.ConfigurateException;

import java.io.File;

public interface Machine<T extends Machine<T>> extends Runnable {
    T load(File file) throws ConfigurateException;

    void save(File file);
}
