package net.sabafly.slotmachine.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.sabafly.slotmachine.SlotMachine;
import net.sabafly.slotmachine.inventory.ExchangeMenu;
import net.sabafly.slotmachine.inventory.PrizeMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;

import java.util.List;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class SlotMachineCommand extends ParaCommand {

    public SlotMachineCommand(final @NotNull SlotMachine plugin) {
        super(plugin, "slotmachine", "sm");
    }

    @Override
    public void run(@NotNull final CommandSender sender, @NotNull final String label, final @NotNull String[] args) {

        Player player;
        if (sender instanceof Player) player = (Player) sender;
        else {
            player = null;
        }
        final boolean isPlayer = player != null;

        if (args.length == 0) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>Help"));
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>- <white>/slotmachine reload"));
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("slotmachine.admin")) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>You don't have permission"));
                return;
            }
            SlotMachine plugin = SlotMachine.getPlugin();
            try {
                plugin.reloadPluginConfig();
            } catch (ConfigurateException e) {
                plugin.getLogger().throwing(ConfigurateException.class.getName(), "reload", e);
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>Config reload failed"));
                return;
            }
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>Config reloaded"));
            return;
        }

        if (args[0].equalsIgnoreCase("prize_menu")) {
            if (args.length == 2 && (player == null || player.hasPermission("slotmachine.admin"))) {
                Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                    if (onlinePlayer.getName().equalsIgnoreCase(args[1])) {
                        onlinePlayer.openInventory(new PrizeMenu(onlinePlayer, 1).getInventory());
                    }
                });
                return;
            }
            if (!isPlayer) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>You must be a player to use this command"));
                return;
            }
            player.openInventory(new PrizeMenu(player, 1).getInventory());
            return;
        }

        if (args[0].equalsIgnoreCase("exchange")) {
            if (args.length == 2 && (player == null || player.hasPermission("slotmachine.admin"))) {
                Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                    if (onlinePlayer.getName().equalsIgnoreCase(args[1])) {
                        onlinePlayer.openInventory(new ExchangeMenu(onlinePlayer).getInventory());
                    }
                });
                return;
            }
            if (!isPlayer) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>You must be a player to use this command"));
                return;
            }
            player.openInventory(new ExchangeMenu(player).getInventory());
            return;
        }

        if (args[0].equalsIgnoreCase("medal")) {
            if (args.length < 2) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>Unknown command"));
                return;
            }

            if (isPlayer && !player.hasPermission("slotmachine.admin")) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>You don't have permission"));
                return;
            }

            switch (args[1].toLowerCase()) {
                case "add" -> {
                    if (args.length != 4) {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>Unknown command"));
                        return;
                    }
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>Player not found"));
                        return;
                    }
                    int amount = Integer.parseInt(args[3]);
                    SlotMachine.getPlugin().getMapManager().addMedal(target, amount);
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>Added " + amount + " medals to " + target.getName()));
                }
                case "remove" -> {
                    if (args.length != 4) {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>Unknown command"));
                        return;
                    }
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>Player not found"));
                        return;
                    }
                    int amount = Integer.parseInt(args[3]);
                    SlotMachine.getPlugin().getMapManager().removeMedal(target, amount);
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>Removed " + amount + " medals from " + target.getName()));
                }
                case "set" -> {
                    if (args.length != 4) {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>Unknown command"));
                        return;
                    }
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>Player not found"));
                        return;
                    }
                    int amount = Integer.parseInt(args[3]);
                    SlotMachine.getPlugin().getMapManager().setMedal(target, amount);
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>Set " + target.getName() + "'s medals to " + amount));
                }
                case "get" -> {
                    if (args.length != 3) {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>Unknown command"));
                        return;
                    }
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>Player not found"));
                        return;
                    }
                    long amount = SlotMachine.getPlugin().getMapManager().getMedal(target);
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>" + target.getName() + "'s medals: " + amount));
                }
                case "list" -> {
                    if (args.length != 2) {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>Unknown command"));
                        return;
                    }
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>Medal list"));
                    SlotMachine.getPlugin().getMapManager().getMedalMap().forEach((uuid, amount) -> {
                        Player target = Bukkit.getPlayer(uuid);
                        if (target == null) return;
                        sender.sendMessage(MiniMessage.miniMessage().deserialize("  <white>" + target.getName() + "'s medals: " + amount));
                    });
                }
            }
            return;
        }

        if (args[0].equalsIgnoreCase("rng")) {
            if (args.length != 3) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>Unknown command"));
                return;
            }
            long bound = Long.parseLong(args[1]);
            long count = Long.parseLong(args[2]);
            RandomGenerator rng = RandomGeneratorFactory.of("Random").create();
            TextComponent.Builder txt = Component.text();
            for (int i = 0; i < count; i++) {
                txt.append(Component.text(rng.nextLong(bound)).appendNewline());
            }
            sender.sendMessage(txt.build());
            return;
        }

        if (isPlayer) player.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>Unknown command"));
        else sender.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>SlotMachine <gray>- <white>Unknown command"));
    }

    @Override
    public @Nullable List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final @NotNull String[] args) {
        Player player = null;
        if (sender instanceof Player) player = (Player) sender;
        final boolean isPlayer = player != null;

        if (args.length == 1 && !isPlayer) return List.of("help", "reload");

        if (args.length == 2 && args[0].equalsIgnoreCase("test")) return List.of("false", "true");

        return List.of();
    }

    @Override
    public LiteralCommandNode<?> node() {
        return LiteralArgumentBuilder.literal("slotmachine")
                .then(LiteralArgumentBuilder.literal("reload"))
                .then(LiteralArgumentBuilder
                        .literal("medal")
                        .then(LiteralArgumentBuilder
                                .literal("add")
                                .then(RequiredArgumentBuilder.argument("player", StringArgumentType.string())
                                        .then(RequiredArgumentBuilder
                                                .argument("amount", IntegerArgumentType.integer(0)))))
                        .then(LiteralArgumentBuilder
                                .literal("remove")
                                .then(RequiredArgumentBuilder.argument("player", StringArgumentType.string())
                                        .then(RequiredArgumentBuilder
                                                .argument("amount", IntegerArgumentType.integer(0)))))
                        .then(LiteralArgumentBuilder
                                .literal("set")
                                .then(RequiredArgumentBuilder.argument("player", StringArgumentType.string())
                                        .then(RequiredArgumentBuilder
                                                .argument("amount", IntegerArgumentType.integer(0)))))
                        .then(LiteralArgumentBuilder
                                .literal("get")
                                .then(RequiredArgumentBuilder.argument("player", StringArgumentType.string())))
                        .then(LiteralArgumentBuilder
                                .literal("list"))
                )
                .then(LiteralArgumentBuilder.
                        literal("rng")
                        .then(RequiredArgumentBuilder.argument("bound", LongArgumentType.longArg(0))
                                .then(RequiredArgumentBuilder.argument("count", LongArgumentType.longArg(0)))))
                .build();
    }

}
