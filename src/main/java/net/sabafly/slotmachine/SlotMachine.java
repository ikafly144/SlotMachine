package net.sabafly.slotmachine;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import me.lucko.commodore.CommodoreProvider;
import net.milkbowl.vault.economy.Economy;
import net.sabafly.slotmachine.commands.CommodoreHandler;
import net.sabafly.slotmachine.commands.SlotMachineCommand;
import net.sabafly.slotmachine.configuration.Configurations;
import net.sabafly.slotmachine.configuration.Transformations;
import net.sabafly.slotmachine.game.MedalBank;
import net.sabafly.slotmachine.game.ScreenManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public final class SlotMachine extends JavaPlugin {

    private final Logger logger = getLogger();
    private CommodoreHandler commodoreHandler;
    private static Economy econ = null;
    private static Configurations config = null;
    private static boolean isFloodgate = false;

    @Override
    public void onEnable() {
        try {
            reloadPluginConfig();
            MedalBank.load();
        } catch (ConfigurateException e) {
            logger.severe("failed to load config");
            e.printStackTrace();
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        logger.info(String.format("enabled (version %s)", getDescription().getVersion()));
        if (!setupEconomy() ) {
            logger.severe("disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (CommodoreProvider.isSupported()) {
            this.commodoreHandler = new CommodoreHandler(this);
            taskChainFactory = BukkitTaskChainFactory.create(this);
            new SlotMachineCommand(this);

            getServer().getPluginManager().registerEvents(new EventListener(this),this);
            getServer().getPluginManager().registerEvents(new ScreenManager(),this);

            if (this.getServer().getPluginManager().getPlugin("maps")==null) {
                this.getServer().getPluginManager().disablePlugin(this);
                return;
            }

            if (this.getServer().getPluginManager().getPlugin("Floodgate") != null) {
                isFloodgate = true;
            }

            newChain().delay(1).async(() -> {
                try {
                    ScreenManager.load(new File(getDataFolder(),"data"), Bukkit.getScheduler());
                } catch (ConfigurateException e) {
                    throw new RuntimeException(e);
                }
            }).execute();
        }
    }

    @Override
    public void onDisable() {

        try {
            ScreenManager.save(new File(getDataFolder(),"data"));
        } catch (Exception e) {
            logger.warning("failed to save map");
            throw new RuntimeException(e);
        }

        logger.info(String.format("disabled (version %s)", getDescription().getVersion()));
    }

    public CommodoreHandler commodoreHandler() { return commodoreHandler; }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    private static TaskChainFactory taskChainFactory;
    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }
    public static <T> TaskChain<T> newSharedChain(String name) {
        return taskChainFactory.newSharedChain(name);
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Configurations getPluginConfig() {
        return config;
    }

    public void reloadPluginConfig() throws ConfigurateException {
        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(new File(getPlugin().getDataFolder(), "config.yml").toPath())
                .indent(2)
                .headerMode(HeaderMode.PRESET)
                .nodeStyle(NodeStyle.BLOCK)
                .build();
        CommentedConfigurationNode node = Transformations.updateNode(loader.load());
        loader.save(node);
        config = node.get(Configurations.class, new Configurations());
    }

    public static SlotMachine getPlugin() {
        return JavaPlugin.getPlugin(SlotMachine.class);
    }

    public static boolean isFloodgate() {
        return isFloodgate;
    }

}
