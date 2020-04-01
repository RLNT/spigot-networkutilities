package rlnt.networkutilities.proxy.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import rlnt.networkutilities.proxy.NetworkUtilities;
import rlnt.networkutilities.proxy.api.ApiException;
import rlnt.networkutilities.proxy.api.Minecraft;

import java.net.Proxy;
import java.net.ProxySelector;
import java.util.*;
import java.util.regex.Pattern;

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
     * Will check if a String is convertable to a valid UUID.
     *
     * @param input the uuid string
     * @return true if the string is valid, else false
     */
    public static boolean isUuid(String input) {
        Pattern p = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[34][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
        return p.matcher(input).matches();
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
