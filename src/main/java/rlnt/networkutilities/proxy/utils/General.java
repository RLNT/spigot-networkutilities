package rlnt.networkutilities.proxy.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import rlnt.networkutilities.proxy.NetworkUtilities;

import java.net.Proxy;
import java.net.ProxySelector;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum General {
    ;

    // store the instance so all functions can use it
    private static NetworkUtilities instance;

    /**
     * Will set the instance when it's available.
     *
     * @param instance the plugin instance
     */
    public static void setInstance(NetworkUtilities instance) {
        General.instance = instance;
    }

    /**
     * Will colorize a given message by alternate color codes.
     *
     * @param message the message that should be colorized
     * @return the colorized message
     */
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Will disable the plugin.
     */
    public static void disablePlugin() {
        instance.getProxy().getPluginManager().unregisterCommands(instance);
        instance.getProxy().getPluginManager().unregisterListeners(instance);
        instance.getProxy().unregisterChannel("networkutilities");
    }
}
