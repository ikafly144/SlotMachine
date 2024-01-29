package net.sabafly.slotmachine.inventory;

import net.kyori.adventure.text.Component;
import net.sabafly.slotmachine.game.Machine;
import net.sabafly.slotmachine.game.Slot;
import net.sabafly.slotmachine.game.slot.SlotRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class ConfigMenu extends ParaInventory {

    private final Entity entity;
    private final Slot slotEntity;

    public ConfigMenu(Plugin plugin, Slot entity) {
        super(plugin, 9, Component.text("config menu"));
        this.entity = null;
        this.slotEntity = entity;
        update(entity);
    }

    public void update() {
        update(slotEntity);
    }

    private void update(final Slot entity) {
        Inventory inventory = getInventory();

        ItemStack closeItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta meta = closeItem.getItemMeta();
        meta.displayName(Component.text("cancel game"));

        if (entity.getStatus().isPlaying())
            meta.getPersistentDataContainer().set(SlotRegistry.Key.ACTION, PersistentDataType.STRING, "CANCEL_GAME");

        closeItem.setItemMeta(meta);
        inventory.setItem(0, closeItem);

        ItemStack flagItem = new ItemStack(Material.WHITE_BANNER);
        meta = flagItem.getItemMeta();
        meta.displayName(Component.text("flag"));
        meta.lore(List.of(Component.text("flag: " + (entity.getFlag() != null ? entity.getFlag() : "null"))));

        meta.getPersistentDataContainer().set(SlotRegistry.Key.ACTION, PersistentDataType.STRING, "TOGGLE_FLAG");

        flagItem.setItemMeta(meta);
        inventory.setItem(1, flagItem);

        ItemStack settingItem = new ItemStack(Material.CHEST);
        meta = settingItem.getItemMeta();
        meta.displayName(Component.text("setting"));
        meta.lore(List.of(Component.text("setting: " + entity.getSettingId())));

        meta.getPersistentDataContainer().set(SlotRegistry.Key.ACTION, PersistentDataType.STRING, "TOGGLE_SETTING");

        settingItem.setItemMeta(meta);
        inventory.setItem(2, settingItem);

        ItemStack debugItem = new ItemStack(Material.BEDROCK);
        meta = debugItem.getItemMeta();
        meta.displayName(Component.text("debug"));
        meta.lore(List.of(Component.text("debug: " + entity.isDebug())));

        meta.getPersistentDataContainer().set(SlotRegistry.Key.ACTION, PersistentDataType.STRING, "TOGGLE_DEBUG");

        debugItem.setItemMeta(meta);
        inventory.setItem(3, debugItem);

        ItemStack resetItem = new ItemStack(Material.BARRIER);
        meta = resetItem.getItemMeta();
        meta.displayName(Component.text("reset"));

        meta.getPersistentDataContainer().set(SlotRegistry.Key.ACTION, PersistentDataType.STRING, "RESET");

        resetItem.setItemMeta(meta);
        inventory.setItem(4, resetItem);

        ItemStack DestroyItem = new ItemStack(Material.TNT);
        meta = DestroyItem.getItemMeta();
        meta.displayName(Component.text("destroy"));

        meta.getPersistentDataContainer().set(SlotRegistry.Key.ACTION, PersistentDataType.STRING, "DESTROY");

        DestroyItem.setItemMeta(meta);
        inventory.setItem(8, DestroyItem);

        ItemStack item = new ItemStack(Material.PAPER);
        meta = item.getItemMeta();
        meta.displayName(Component.text("info"));
        meta.lore(List.of(
                Component.text("uuid: " + entity.getUniqueId()),
                Component.text("flag: " + (entity.getFlag() != null ? entity.getFlag() : "null")),
                Component.text("setting_id: " + entity.getSettingId()),
                Component.text("setting: " + (entity.getSetting() != null ? entity.getSetting() : "null")),
                Component.text("debug: " + entity.isDebug()),
                Component.text("status: " + entity.getStatus()),
                Component.text("totalPayIn: " + entity.getStats().totalPayIn),
                Component.text("totalPayOut: " + entity.getStats().totalPayOut),
                Component.text("totalBonus: " + entity.getStats().totalBonusCount),
                Component.text("totalBonusPayOut: " + entity.getStats().totalBonusPayOut),
                Component.text("totalBigBonus: " + entity.getStats().totalBigBonusCount),
                Component.text("totalGameCount: " + entity.getStats().totalGameCount)
        ));

        item.setItemMeta(meta);
        inventory.setItem(7, item);
    }

    public ConfigMenu(Plugin plugin, Entity entity) {
        super(plugin, 9, Component.text("create slot"));
        this.entity = entity;
        this.slotEntity = null;
        Inventory inventory = getInventory();

        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("create new"));

        meta.getPersistentDataContainer().set(SlotRegistry.Key.ACTION, PersistentDataType.STRING, "CREATE");

        item.setItemMeta(meta);

        inventory.setItem(4, item);
    }

    @Nullable
    public Entity getEntity() {
        return entity;
    }

    public Machine<?> getMachine() {
        return slotEntity;
    }

}
