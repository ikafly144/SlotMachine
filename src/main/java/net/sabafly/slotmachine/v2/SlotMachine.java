package net.sabafly.slotmachine.v2;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.sabafly.slotmachine.v2.commands.SlotMachineCommand;
import net.sabafly.slotmachine.v2.game.MachineManager;
import net.sabafly.slotmachine.v2.game.data.DataFolderLoader;
import net.sabafly.slotmachine.v2.game.locale.LocaleLoader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public class SlotMachine extends JavaPlugin {

    private static Logger LOGGER;

    private final String[] VISUAL_ART = {
            "  ______   __              __      __       __                      __        __                     ",
            " /      \\ /  |            /  |    /  \\     /  |                    /  |      /  |                    ",
            "/$$$$$$  |$$ |  ______   _$$ |_   $$  \\   /$$ |  ______    _______ $$ |____  $$/  _______    ______  ",
            "$$ \\__$$/ $$ | /      \\ / $$   |  $$$  \\ /$$$ | /      \\  /       |$$      \\ /  |/       \\  /      \\ ",
            "$$      \\ $$ |/$$$$$$  |$$$$$$/   $$$$  /$$$$ | $$$$$$  |/$$$$$$$/ $$$$$$$  |$$ |$$$$$$$  |/$$$$$$  |",
            " $$$$$$  |$$ |$$ |  $$ |  $$ | __ $$ $$ $$/$$ | /    $$ |$$ |      $$ |  $$ |$$ |$$ |  $$ |$$    $$ |",
            "/  \\__$$ |$$ |$$ \\__$$ |  $$ |/  |$$ |$$$/ $$ |/$$$$$$$ |$$ \\_____ $$ |  $$ |$$ |$$ |  $$ |$$$$$$$$/ ",
            "$$    $$/ $$ |$$    $$/   $$  $$/ $$ | $/  $$ |$$    $$ |$$       |$$ |  $$ |$$ |$$ |  $$ |$$       |",
            " $$$$$$/  $$/  $$$$$$/     $$$$/  $$/      $$/  $$$$$$$/  $$$$$$$/ $$/   $$/ $$/ $$/   $$/  $$$$$$$/ "
    };

    @Override
    public void onLoad() {
        LOGGER = getSLF4JLogger();
    }

    @Override
    public void onEnable() {
        LOGGER.info("SlotMachine is starting up...");
        for (String s : VISUAL_ART) {
            LOGGER.info(s);
        }
        final String version = getPluginMeta().getVersion();
        LOGGER.info("==============================================");
        LOGGER.info("VERSION: {}", version);
        LOGGER.info("AUTHOR: {}", getPluginMeta().getAuthors());
        LOGGER.info("SOURCE CODE: {}", getPluginMeta().getWebsite());
        LOGGER.info("==============================================");
        if (version.contains("alpha") || version.contains("beta")) {
            LOGGER.warn("This version is not stable, please be careful!");
            LOGGER.warn("If you find any bugs, please report them to the developer.");
            LOGGER.warn("You can also join the discord server to get help.");
        }

        reload();

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, new SlotMachineCommand());
        MachineManager.init(this);

        LOGGER.info("SlotMachine has been enabled!");

        Bukkit.getGlobalRegionScheduler().runDelayed(this, (task) -> MachineManager.loadMachines(),1);
    }

    public void reload() {
        DataFolderLoader.init(getDataPath());
        LocaleLoader.load();
    }

}
