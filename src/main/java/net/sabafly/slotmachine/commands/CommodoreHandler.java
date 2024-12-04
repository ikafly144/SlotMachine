package net.sabafly.slotmachine.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import net.sabafly.slotmachine.SlotMachine;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

@Deprecated(forRemoval = true)
public class CommodoreHandler {

    private final SlotMachine plugin;
    private Commodore commodore = null;

    public CommodoreHandler(@NotNull final SlotMachine plugin) {
        this.plugin = plugin;

        if (CommodoreProvider.isSupported())
            this.commodore = CommodoreProvider.getCommodore(plugin);
    }

    public void register(@NotNull final PluginCommand command, @NotNull LiteralCommandNode<?> node) {
        if (commodore==null) return;

        commodore.register(command, node);
    }

}
