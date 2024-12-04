package net.sabafly.slotmachine.v2.game.interaction;

import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public class SimpleOpenInv extends SimpleButton {

    SimpleOpenInv(int x, int y, int w, int h, InventoryHolder holder) {
        super(x, y, w, h, (pos, p) -> {
            p.openInventory(holder.getInventory());
        });
    }

}
