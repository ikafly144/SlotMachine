package net.sabafly.slotmachine.inventory;

import net.kyori.adventure.text.Component;
import net.sabafly.slotmachine.game.Machine;
import net.sabafly.slotmachine.game.Slot;
import net.sabafly.slotmachine.v2.game.slot.Key;
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
    private final Machine<?> slotEntity;

    public ConfigMenu(Plugin plugin, Machine<?> entity) {
        super(plugin, 9, Component.text("config menu"));
        this.entity = null;
        this.slotEntity = entity;
        update(entity);
    }

    public void update() {
        update(slotEntity);
    }

    private void update(final Machine<?> entity) {
        switch (entity.type()) {
            case SLOT -> {
                if (entity instanceof Slot slot) {
                    Inventory inventory = getInventory();

                    ItemStack closeItem = new ItemStack(Material.REDSTONE_BLOCK);
                    ItemMeta meta = closeItem.getItemMeta();
                    meta.displayName(Component.text("cancel game"));

                    if (slot.getStatus().isPlaying())
                        meta.getPersistentDataContainer().set(Key.ACTION, PersistentDataType.STRING, "CANCEL_GAME");

                    closeItem.setItemMeta(meta);
                    inventory.setItem(0, closeItem);

                    ItemStack flagItem = new ItemStack(Material.WHITE_BANNER);
                    meta = flagItem.getItemMeta();
                    meta.displayName(Component.text("flag"));
                    meta.lore(List.of(Component.text("flag: " + (slot.getFlag() != null ? slot.getFlag() : "null"))));

                    meta.getPersistentDataContainer().set(Key.ACTION, PersistentDataType.STRING, "TOGGLE_FLAG");

                    flagItem.setItemMeta(meta);
                    inventory.setItem(1, flagItem);

                    ItemStack settingItem = new ItemStack(Material.CHEST);
                    meta = settingItem.getItemMeta();
                    meta.displayName(Component.text("setting"));
                    meta.lore(List.of(Component.text("setting: " + slot.getSettingId())));

                    meta.getPersistentDataContainer().set(Key.ACTION, PersistentDataType.STRING, "TOGGLE_SETTING");

                    settingItem.setItemMeta(meta);
                    inventory.setItem(2, settingItem);

                    ItemStack debugItem = new ItemStack(Material.BEDROCK);
                    meta = debugItem.getItemMeta();
                    meta.displayName(Component.text("debug"));
                    meta.lore(List.of(Component.text("debug: " + slot.isDebug())));

                    meta.getPersistentDataContainer().set(Key.ACTION, PersistentDataType.STRING, "TOGGLE_DEBUG");

                    debugItem.setItemMeta(meta);
                    inventory.setItem(3, debugItem);

                    ItemStack resetItem = new ItemStack(Material.BARRIER);
                    meta = resetItem.getItemMeta();
                    meta.displayName(Component.text("reset"));

                    meta.getPersistentDataContainer().set(Key.ACTION, PersistentDataType.STRING, "RESET");

                    resetItem.setItemMeta(meta);
                    inventory.setItem(4, resetItem);

                    ItemStack DestroyItem = new ItemStack(Material.TNT);
                    meta = DestroyItem.getItemMeta();
                    meta.displayName(Component.text("destroy"));

                    meta.getPersistentDataContainer().set(Key.ACTION, PersistentDataType.STRING, "DESTROY");

                    DestroyItem.setItemMeta(meta);
                    inventory.setItem(8, DestroyItem);

                    ItemStack item = new ItemStack(Material.PAPER);
                    meta = item.getItemMeta();
                    meta.displayName(Component.text("info"));
                    meta.lore(List.of(
                            Component.text("uuid: " + entity.getUniqueId()),
                            Component.text("flag: " + (slot.getFlag() != null ? slot.getFlag() : "null")),
                            Component.text("setting_id: " + slot.getSettingId()),
                            Component.text("setting: " + (slot.getSetting() != null ? slot.getSetting() : "null")),
                            Component.text("debug: " + slot.isDebug()),
                            Component.text("status: " + slot.getStatus()),
                            Component.text("totalPayIn: " + slot.getStats().totalPayIn),
                            Component.text("totalPayOut: " + slot.getStats().totalPayOut),
                            Component.text("totalBonus: " + slot.getStats().totalBonusCount),
                            Component.text("totalBonusPayOut: " + slot.getStats().totalBonusPayOut),
                            Component.text("totalBigBonus: " + slot.getStats().totalBigBonusCount),
                            Component.text("totalGameCount: " + slot.getStats().totalGameCount)
                    ));

                    item.setItemMeta(meta);
                    inventory.setItem(7, item);
                }
            }
            case JUGGLER -> {
                // TODO: Implement Juggler ConfigMenu
            }
        }
    }

    public ConfigMenu(Plugin plugin, Entity entity) {
        super(plugin, 9, Component.text("create slot"));
        this.entity = entity;
        this.slotEntity = null;
        Inventory inventory = getInventory();

        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("create new"));

        meta.getPersistentDataContainer().set(Key.ACTION, PersistentDataType.STRING, "CREATE");

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
