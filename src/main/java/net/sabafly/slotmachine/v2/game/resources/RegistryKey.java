package net.sabafly.slotmachine.v2.game.resources;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;

public class RegistryKey {

    public static Key create(@KeyPattern.Value final String key) {
        return Key.key("slotmachine", key);
    }

}
