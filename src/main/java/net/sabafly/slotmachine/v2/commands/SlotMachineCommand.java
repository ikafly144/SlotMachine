package net.sabafly.slotmachine.v2.commands;

import com.mojang.brigadier.Command;
import io.papermc.paper.brigadier.TagParseCommandSyntaxException;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import net.sabafly.slotmachine.v2.game.MachineManager;
import net.sabafly.slotmachine.v2.game.machine.Juggler;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public class SlotMachineCommand implements LifecycleEventHandler<ReloadableRegistrarEvent<Commands>> {

    @Override
    public void run(@NotNull ReloadableRegistrarEvent<Commands> event) {
        var command = event.registrar();
        command.register(
                Commands.literal("slotmachine")
                        .then(Commands.literal("create")
                                .then(Commands.argument("entity", ArgumentTypes.entity())
                                        .executes(context -> {
                                            var entity = context.getArgument("entity", EntitySelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                            if (!(entity instanceof ItemFrame)) throw new TagParseCommandSyntaxException("Entity must be an item frame");
                                            if (entity.getFacing().getModY() != 0) throw new TagParseCommandSyntaxException("Entity must be placed on a wall");
                                            try {
                                                MachineManager.createMachine(Juggler.create(), entity);
                                            } catch (SerializationException e) {
                                                throw new RuntimeException(e);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .build(),
                List.of("sm", "slot")
        );
    }
}
