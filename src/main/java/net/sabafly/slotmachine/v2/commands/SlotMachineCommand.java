package net.sabafly.slotmachine.v2.commands;

import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import net.sabafly.slotmachine.v2.game.juggler.JugglerMachine;
import org.bukkit.entity.ItemFrame;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SlotMachineCommand implements LifecycleEventHandler<ReloadableRegistrarEvent<Commands>> {

    @Override
    public void run(@NotNull ReloadableRegistrarEvent<Commands> event) {
        var command = event.registrar();
        command.register(
                Commands.literal("slotmachine")
                        .then(
                                Commands.argument("itemframe", ArgumentTypes.entity())
                                        .executes(context -> {
                                            ItemFrame itemFrame = (ItemFrame) context.getArgument("itemframe", EntitySelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                            MapDisplay.fillItemFrames(itemFrame, JugglerMachine.class);
                                            return Command.SINGLE_SUCCESS;
                                        })
                        )
                        .build(),
                List.of("sm", "slot")
        );
    }
}
