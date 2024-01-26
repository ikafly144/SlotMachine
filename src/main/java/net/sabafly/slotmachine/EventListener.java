package net.sabafly.slotmachine;

import dev.cerus.maps.api.MapScreen;
import dev.cerus.maps.api.event.PlayerClickScreenEvent;
import dev.cerus.maps.plugin.map.MapScreenRegistry;
import dev.cerus.maps.util.Vec2;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.sabafly.slotmachine.game.Machine;
import net.sabafly.slotmachine.game.ScreenManager;
import net.sabafly.slotmachine.game.Slot;
import net.sabafly.slotmachine.game.slot.SlotRegistry;
import net.sabafly.slotmachine.inventory.ConfigMenu;
import net.sabafly.slotmachine.inventory.ExchangeMenu;
import net.sabafly.slotmachine.inventory.PrizeMenu;
import net.sabafly.slotmachine.game.slot.SlotRegistry.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

public class EventListener implements Listener {

    private final SlotMachine plugin;
    private final Logger logger;

    public EventListener(SlotMachine plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!event.getPlayer().hasPermission("slotmachine.admin")) return;

        Entity entity = event.getRightClicked();
        if (!(List.of(EntityType.ITEM_FRAME,EntityType.GLOW_ITEM_FRAME).contains(entity.getType()))) return;

        if (Bukkit.getEntity(entity.getUniqueId())==null) {
            event.setCancelled(true);
            return;
        }

        final ConfigMenu menu;
        if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.BLAZE_ROD) {
            event.setCancelled(true);
            menu = new ConfigMenu(this.plugin, entity);
            event.getPlayer().openInventory(menu.getInventory());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();


        if (inventory.getHolder(false) instanceof ConfigMenu configMenu) {

            event.setCancelled(true);

            if (!event.getWhoClicked().hasPermission("slotmachine.admin")) return;

            Inventory clickedInventory = event.getClickedInventory();
            if (clickedInventory == null || !(clickedInventory.getHolder(false) instanceof ConfigMenu)) return;

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;

            ItemMeta clickedMeta = clicked.getItemMeta();
            if (clickedMeta == null) return;

            String action = clickedMeta.getPersistentDataContainer().getOrDefault(SlotRegistry.Key.ACTION, PersistentDataType.STRING, "NONE");

            if (action.equals("CREATE")) {
                HumanEntity clickedEntity = event.getWhoClicked();
                clickedEntity.sendPlainMessage("created");
                clickedEntity.closeInventory();
                Slot slot = Slot.create(configMenu.getEntity(), WheelSet.JUGGLER, SettingSet.JUGGLER);
                ScreenManager.registerMachine(slot, Bukkit.getScheduler());
                return;
            }

            Machine<?> machine = configMenu.getMachine();
            if (machine == null) return;
            if (machine instanceof Slot slot) {

                switch (action) {
                    case "CANCEL_GAME" -> slot.cancelPlay();
                    case "TOGGLE_FLAG" -> slot.nextFlag();
                    case "TOGGLE_SETTING" -> slot.nextSetting();
                    case "TOGGLE_DEBUG" -> slot.toggleDebug();
                    case "DESTROY" -> {
                        slot.destroy();
                        event.getWhoClicked().closeInventory();
                    }
                    case "NONE" -> {
                        return;
                    }
                    default -> {
                        HumanEntity player = event.getWhoClicked();
                        player.sendPlainMessage("not implemented!!");
                    }
                }
                configMenu.update();
            }
        } else if (inventory.getHolder(false) instanceof PrizeMenu prizeMenu) {

            Inventory clickedInventory = event.getClickedInventory();
            boolean isPlayerInv = (clickedInventory != null && clickedInventory.getType() == InventoryType.PLAYER && (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.MIDDLE));
            if (!isPlayerInv) {
                event.setCancelled(true);
            }
            if (!event.getWhoClicked().hasPermission("slotmachine.purchase")) return;
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;
            ItemMeta clickedMeta = clicked.getItemMeta();
            if (clickedMeta == null) return;
            String action = clickedMeta.getPersistentDataContainer().getOrDefault(SlotRegistry.Key.ACTION, PersistentDataType.STRING, "NONE");
            HumanEntity player = event.getWhoClicked();

            switch (action) {
                case "PURCHASE" -> {
                    if (prizeMenu.buy(clicked)) {
                        ItemStack buyItem = prizeMenu.buyItem(clicked);
                        player.sendMessage(Component.text().append(buyItem.displayName(), Component.text("を購入しました。")));
                        plugin.getComponentLogger().info(player.name(),Component.text().append(buyItem.displayName(), Component.text("を購入しました。")).build());
                        player.getInventory().addItem(buyItem);
                    }
                }
                case "ADD_MEDAL" -> {
                    ItemStack cursor = event.getCursor();
                    ItemMeta cursorMeta = cursor.getItemMeta();
                    if (cursorMeta == null) return;
                    String type = cursorMeta.getPersistentDataContainer().get(SlotRegistry.Key.TYPE, PersistentDataType.STRING);
                    if (type == null || !type.equals("TICKET")) return;
                    String date = cursorMeta.getPersistentDataContainer().get(SlotRegistry.Key.DATE, PersistentDataType.STRING);
                    if (date == null) return;
                    if (!date.equals(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))) {
                        Long unixTime = cursorMeta.getPersistentDataContainer().get(SlotRegistry.Key.UNIX_TIME, PersistentDataType.LONG);
                        if (unixTime == null) return;
                        // 三時間以内かどうか
                        if (unixTime + 60 * 60 * 3 < System.currentTimeMillis() / 1000) return;
                    }
                    Long coin = cursorMeta.getPersistentDataContainer().get(SlotRegistry.Key.COIN, PersistentDataType.LONG);
                    if (coin == null) return;
                    prizeMenu.addMedal(coin);

                    player.setItemOnCursor(null);
                }
                case "NONE" -> {
                    return;
                }
                default -> player.sendPlainMessage("not implemented!!");
            }

            player.playSound(Sound.sound(Key.key("ui.button.click"), Sound.Source.MASTER, 1, 1));

        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder(false) instanceof PrizeMenu prizeMenu) {
            prizeMenu.close(event.getPlayer());
        } else if (inventory.getHolder(false) instanceof ExchangeMenu exchangeMenu) {
            if (exchangeMenu.close())
                SlotMachine.newChain().delay(10).sync(() -> event.getPlayer().openInventory(exchangeMenu.getInventory())).execute();
        }
    }

    private final Map<UUID, Integer> lastClick = new HashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onScreenClick(PlayerClickScreenEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("slotmachine.play")) return;

        if (lastClick.containsKey(player.getUniqueId())) {
            if (Bukkit.getCurrentTick() - lastClick.get(player.getUniqueId()) <= 10) {
                event.setCancelled(true);
                return;
            }
        }
        lastClick.put(player.getUniqueId(), Bukkit.getCurrentTick());
        final MapScreen screen = event.getClickedScreen();
        Vec2 clickPos = event.getClickPos();
        if (!(ScreenManager.getScreenMap().get(screen) instanceof Slot slot)) return;

        if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.BLAZE_ROD) {
            if (!player.hasPermission("slotmachine.admin")) return;

            event.setCancelled(true);
            ConfigMenu menu = new ConfigMenu(this.plugin, slot);

            SlotMachine.newChain().sync(() -> player.openInventory(menu.getInventory())).execute();
            return;
        }

        if (!event.isLeftClick()) return;

        slot.onClick(player, clickPos);

        event.setCancelled(true);
    }

}
