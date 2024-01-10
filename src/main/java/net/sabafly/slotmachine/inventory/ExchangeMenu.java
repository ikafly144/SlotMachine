package net.sabafly.slotmachine.inventory;

import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import net.sabafly.slotmachine.SlotMachine;
import net.sabafly.slotmachine.game.slot.SlotRegistry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.atomic.AtomicInteger;

public class ExchangeMenu extends ParaInventory {
    private final Player player;
    public ExchangeMenu(Player player) {
        super(SlotMachine.getPlugin(), 4 * 9, Component.text("交換する景品を全て投入"));
        this.player = player;
    }

    public boolean close() {
        AtomicInteger deposit = new AtomicInteger();
        this.getInventory().iterator().forEachRemaining(itemStack -> {
            if (itemStack == null) return;
            ItemMeta meta = itemStack.getItemMeta();
            if (meta == null) return;
            String type = meta.getPersistentDataContainer().get(SlotRegistry.Key.TYPE, PersistentDataType.STRING);
            if (type == null || !type.equals("PAY_OUT")) return;
            String size = meta.getPersistentDataContainer().get(SlotRegistry.Key.SIZE, PersistentDataType.STRING);
            if (size == null) return;
            switch (size) {
                case "LARGE" -> deposit.addAndGet(SlotMachine.getPluginConfig().prize.largeSell * itemStack.getAmount());
                case "MEDIUM" -> deposit.addAndGet(SlotMachine.getPluginConfig().prize.mediumSell * itemStack.getAmount());
                case "SMALL" -> deposit.addAndGet(SlotMachine.getPluginConfig().prize.smallSell * itemStack.getAmount());
            }
            getInventory().remove(itemStack);
        });
        Economy eco = SlotMachine.getEconomy();
        player.sendMessage("払い出し金額: " + deposit.get() + " " + eco.currencyNamePlural());
        SlotMachine.getPlugin().getLogger().info( player.getName() + "払い出し金額: " + deposit.get() + " " + eco.currencyNamePlural());
        eco.depositPlayer(player, deposit.get());
        if (!getInventory().isEmpty()) {
            player.sendMessage("アイテムを取り出してください。");
            return true;
        }
        return false;
    }

}
