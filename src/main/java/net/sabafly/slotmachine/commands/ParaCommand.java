package net.sabafly.slotmachine.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import net.sabafly.slotmachine.SlotMachine;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@Deprecated
public abstract class ParaCommand implements CommandExecutor, TabCompleter {

    public ParaCommand(@NotNull final SlotMachine plugin, @NotNull final String name, String... alias) {
        final PluginCommand command = plugin.getCommand(name);
        if (command==null) return;

        command.setExecutor(this);
        command.setTabCompleter(this);
        command.setAliases(Arrays.stream(alias).toList());
        plugin.commodoreHandler().register(command, node());
    }

    public abstract void run(
        @NotNull CommandSender sender,
        @NotNull String label,
        @NotNull String[] args
    );

    public abstract LiteralCommandNode<?> node();

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, final @NotNull String[] args) {
        run(sender, label, args);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, final @NotNull String[] args) {
        return null;
    }

}
