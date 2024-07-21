package net.sabafly.slotmachine.v2.game.slot;

import net.sabafly.slotmachine.SlotMachine;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class Key {
    private static final Plugin plugin = SlotMachine.getPlugin();
    @NotNull
    public static final NamespacedKey TYPE = new NamespacedKey(plugin, "type");
    @NotNull
    public static final NamespacedKey SIZE = new NamespacedKey(plugin, "size");
    @NotNull
    public static final NamespacedKey ACTION = new NamespacedKey(plugin, "action");
    @NotNull
    public static final NamespacedKey COIN = new NamespacedKey(plugin, "coin");
    @NotNull
    public static final NamespacedKey DATE = new NamespacedKey(plugin, "date");
    @NotNull
    public static final NamespacedKey PRICE = new NamespacedKey(plugin, "price");
    @NotNull
    public static final NamespacedKey UNIX_TIME = new NamespacedKey(plugin, "unixtime");
}
