package net.sabafly.slotmachine.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.milkbowl.vault.economy.Economy;
import net.sabafly.slotmachine.SlotMachine;
import net.sabafly.slotmachine.v2.game.slot.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.atomic.AtomicInteger;

public class ExchangeMenu extends ParaInventory {
    private final Player player;
    public ExchangeMenu(Player player) {
        super(SlotMachine.getPlugin(), 4 * 9, MiniMessage.miniMessage().deserialize(SlotMachine.getMessages().exchangeMenu));
        this.player = player;
    }

    public boolean close() {
        AtomicInteger deposit = new AtomicInteger();
        this.getInventory().iterator().forEachRemaining(itemStack -> {
            if (itemStack == null) return;
            ItemMeta meta = itemStack.getItemMeta();
            if (meta == null) return;
            String type = meta.getPersistentDataContainer().get(Key.TYPE, PersistentDataType.STRING);
            if (type == null || !type.equals("PAY_OUT")) return;
            String size = meta.getPersistentDataContainer().get(Key.SIZE, PersistentDataType.STRING);
            if (size == null) return;
            switch (size) {
                case "LARGE" -> deposit.addAndGet(SlotMachine.getPluginConfig().prize.largeSell * itemStack.getAmount());
                case "MEDIUM" -> deposit.addAndGet(SlotMachine.getPluginConfig().prize.mediumSell * itemStack.getAmount());
                case "SMALL" -> deposit.addAndGet(SlotMachine.getPluginConfig().prize.smallSell * itemStack.getAmount());
            }
            getInventory().remove(itemStack);
        });
        Economy eco = SlotMachine.getEconomy();
        player.sendMessage(MiniMessage.miniMessage().deserialize(SlotMachine.getMessages().payOut, TagResolver.builder().tag("price", Tag.inserting(Component.text(eco.format(deposit.get())))).build()));
        eco.depositPlayer(player, deposit.get());
        if (!getInventory().isEmpty()) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(SlotMachine.getMessages().itemTakeOut));
            return true;
        }
        return false;
    }

}
