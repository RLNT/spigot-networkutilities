package rlnt.networkutilities.spigot;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import rlnt.networkutilities.spigot.listeners.FirstJoin;
import rlnt.networkutilities.spigot.listeners.PluginChannel;
import rlnt.networkutilities.spigot.listeners.ServerNotifications;
import rlnt.networkutilities.spigot.utils.Config;
import rlnt.networkutilities.spigot.utils.PluginLogger;
import rlnt.networkutilities.spigot.utils.Utils;

import java.util.Objects;

// TODO: reload command (messages only to prevent clashing with hub server option)

public final class NetworkUtilities extends JavaPlugin {

    private static NetworkUtilities instance;
    private PluginLogger logger = new PluginLogger(this);

    private YamlConfiguration config;

    private String configVersion = "1.0.0";
    private int configKeys = 3;

    @Override
    public void onEnable() {
        instance = this;

        try {
            logger.info("&6 <============================================>");
            logger.info("&6  > &fThe plugin is starting...");
            logger.info("&6  > &5Author: &fRLNT");
            logger.info("&6  > &5Version: &f" + getDescription().getVersion());
            logger.info("&6 <============================================>");
            logger.info("&6  > &fLoading configs...");
            configs();
            logger.info("&a  > &fConfigs loaded...");
            logger.info("&6  > &fLoading listeners...");
            listeners();
            logger.info("&a  > &fListeners loaded...");
            logger.info("&6 <============================================>");

            if (config.getBoolean("isHubServer", false)) {
                logger.info("&6  > &5This is the hub/lobby server!");
                logger.info("&6 <============================================>");
            }
        } catch (Error e) {
            logger.severe(e.toString());
            Utils.disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {}

    private void configs() {
        try {
            config = new Config("config.yml", this).getConfig();

            if (config.getKeys(false).isEmpty()) {
                logger.warning("&c  > &eYour &cconfig.yml &eis empty!");
                logger.info("&6  > &fMake sure your configuration is up to date.");
            }
            if (config.getKeys(false).size() != configKeys || !Objects.equals(config.getString("configVersion"), configVersion)) {
                logger.warning("&c  > &eYour &cconfig.yml &eseems to be outdated!");
                logger.info("&6  > &fMake sure your configuration is up to date.");
            }
        } catch (Error e) {
            throw new Error(e);
        }
    }

    private void listeners() {
        getServer().getPluginManager().registerEvents(new ServerNotifications(), this);

        getServer().getPluginManager().registerEvents(new FirstJoin(), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PluginChannel());
    }

    public static NetworkUtilities getInstance() {
        return instance;
    }

    @Override
    public PluginLogger getLogger() {
        return logger;
    }

    public YamlConfiguration getConfig() {
        return config;
    }
}
