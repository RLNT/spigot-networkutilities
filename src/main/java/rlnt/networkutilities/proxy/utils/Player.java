package rlnt.networkutilities.proxy.utils;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import rlnt.networkutilities.proxy.NetworkUtilities;

import java.util.*;

public enum Player {
    ;

    // store the instance so all functions can use it
    private static NetworkUtilities instance;

    /**
     * Will set the instance when it's available.
     *
     * @param instance the plugin instance
     */
    public static void setInstance(NetworkUtilities instance) {
        Player.instance = instance;
    }

    /**
     * Will check if a player has a specific permission.
     * Returns true if yes and if no, the player will get
     * a message and false is returned.
     *
     * @param player the player to check the permission for
     * @param permission the permission to check
     * @return true if the player has the permission, false if not
     */
    public static boolean hasPermission(ProxiedPlayer player, String permission) {
        if (player.hasPermission(permission)) {
            return true;
        } else {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("{permission}", permission);
            Configuration section = instance.getMessages().getConfig().getSection("general");
            Communication.playerCfgMsg(player, section, "noPermission", placeholders);
            return false;
        }
    }

    /**
     * Will return all player names that are currently
     * connected to the network.
     *
     * @return all player names from the network
     */
    public static Set<String> getPlayerNames() {
        Collection<ProxiedPlayer> network = ProxyServer.getInstance().getPlayers();
        Set<String> usernames = new HashSet<>();
        for (ProxiedPlayer player : network) {
            usernames.add(player.getName());
        }
        return usernames;
    }

    /**
     * Will return a player object by the username.
     *
     * @param name the name to get the player from
     * @return the player object
     */
    public static ProxiedPlayer getPlayerByName(String name) {
        return instance.getProxy().getPlayer(name);
    }

    /**
     * Will return a collection with all players on
     * a specific server in the network.
     *
     * @param server the server name to get the players from
     * @return the players of the server
     */
    public static Collection<ProxiedPlayer> getPlayersByServer(String server) {
        return ProxyServer.getInstance().getServerInfo(server).getPlayers();
    }
}
