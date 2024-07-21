package net.sabafly.slotmachine.inventory;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public abstract class ParaInventory implements InventoryHolder {

    private final Inventory inventory;
    @Getter
    private final Plugin plugin;

    public ParaInventory(Plugin plugin, Integer size, Component component) {
        this.plugin = plugin;
        this.inventory = this.plugin.getServer().createInventory(this, size, component);
    }

    @Override
    public org.bukkit.inventory.@NotNull Inventory getInventory() {
        return this.inventory;
    }

}
