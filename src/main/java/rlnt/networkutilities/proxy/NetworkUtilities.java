package rlnt.networkutilities.proxy;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import rlnt.networkutilities.proxy.commands.HubCmd;
import rlnt.networkutilities.proxy.commands.WhitelistCmd;
import rlnt.networkutilities.proxy.listeners.ConNotifiesListener;
import rlnt.networkutilities.proxy.listeners.PluginChannelListener;
import rlnt.networkutilities.proxy.listeners.WaitForServerListener;
import rlnt.networkutilities.proxy.listeners.WhitelistListener;
import rlnt.networkutilities.proxy.plugin.PluginConfig;
import rlnt.networkutilities.proxy.plugin.PluginConfigException;
import rlnt.networkutilities.proxy.plugin.PluginLogger;
import rlnt.networkutilities.proxy.utils.Config;
import rlnt.networkutilities.proxy.utils.General;
import rlnt.networkutilities.proxy.utils.Player;
import rlnt.networkutilities.proxy.utils.Whitelist;

// TODO: UUID based Whitelist/Blacklist
// TODO: automatic reconnect
// TODO: reload command (messages only to prevent clashing with hub server option)
// TODO: block commands

public class NetworkUtilities extends Plugin {

    private static NetworkUtilities instance;
    private PluginLogger logger;
    private PluginManager pm;

    private PluginConfig options = null;
    private PluginConfig messages = null;
    private PluginConfig whitelist = null;

    private Configuration config;

    private String optionsVersion = "1.0.0";
    private int optionsKeys = 5;

    private String messagesVersion = "1.0.0";
    private int messagesKeys = 5;

    @Override
    public void onEnable() {
        instance = this;
        logger = new PluginLogger(instance);
        pm = getProxy().getPluginManager();

        Config.setInstance(instance);
        General.setInstance(instance);
        Player.setInstance(instance);

        try {
            logger.info("&6 <============================================>");
            logger.info("&6  > &fThe plugin is starting...");
            logger.info("&6  > &5Author: &fRLNT");
            logger.info("&6  > &5Version: &f" + getDescription().getVersion());
            logger.info("&6 <============================================>");
            logger.info("&6  > &fLoading configs...");
            configs();
            logger.info("&a  > &fConfigs loaded...");
            logger.info("&6  > &fLoading commands...");
            commands();
            logger.info("&a  > &fCommands loaded...");
            logger.info("&6  > &fLoading listeners...");
            listeners();
            logger.info("&a  > &fListeners loaded...");
            logger.info("&6 <============================================>");
        } catch (PluginConfigException e) {
            logger.severe(e.toString());
            General.disablePlugin();
        }
    }

    @Override
    public void onDisable() {
        getProxy().unregisterChannel("networkutilities");
    }

    private void configs() throws PluginConfigException {
        options = new PluginConfig("config.yml", optionsVersion, optionsKeys);
        config = options.getConfig();
        messages = new PluginConfig("messages.yml", messagesVersion, messagesKeys);
        if (config.getBoolean("uuidBasedWhitelist", false)) {
            whitelist = new PluginConfig("whitelist.yml");
            Whitelist.setWhitelist(whitelist.getConfig().getStringList("whitelist"));
        }
    }

    private void commands() {
        if (config.getSection("commands").getSection("hub").getBoolean("enabled", false)) {
            String targetServer = config.getSection("commands").getSection("hub").getString("targetServer");
            if (targetServer == null || targetServer.isEmpty()) {
                logger.warning("&c  > &eThe hub command has no target server defined! It won't be enabled.");
            } else {
                pm.registerCommand(this, new HubCmd());
            }
        }

        if (config.getSection("commands").getSection("whitelist").getBoolean("enabled", false))
            pm.registerCommand(this, new WhitelistCmd());
    }

    private void listeners() {
        getProxy().registerChannel("networkutilities");

        if (config.getBoolean("firstJoinNotifications", false))
            pm.registerListener(this, new PluginChannelListener());

        if (config.getBoolean("connectionNotifications", false))
            pm.registerListener(this, new ConNotifiesListener());

        if (config.getBoolean("waitForServer", false))
            pm.registerListener(this, new WaitForServerListener());

        if (whitelist != null)
            pm.registerListener(this, new WhitelistListener());
    }

    public static NetworkUtilities getInstance() {
        return instance;
    }

    @Override
    public PluginLogger getLogger() {
        return logger;
    }

    public PluginConfig getOptions() {
        return options;
    }

    public PluginConfig getMessages() {
        return messages;
    }

    public PluginConfig getWhitelist() {
        return whitelist;
    }
}
