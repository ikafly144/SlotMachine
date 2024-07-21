package net.sabafly.slotmachine.v2;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.sabafly.slotmachine.v2.commands.SlotMachineCommand;
import org.jetbrains.annotations.NotNull;

public class SlotMachineBootstrapper implements PluginBootstrap {
    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, new SlotMachineCommand());
    }
}
