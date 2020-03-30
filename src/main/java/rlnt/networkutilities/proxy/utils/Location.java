package rlnt.networkutilities.proxy.utils;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public enum Location {
    ;

    // hashmap to store the current server for each player
    private static final Map<UUID, ServerInfo> playerLocation = new HashMap<>();

    /**
     * Will return a player's location from the storage.
     *
     * @param player the player that is checked
     * @return the server the player is on
     */
    public static ServerInfo getPlayerLocation(ProxiedPlayer player) {
        return playerLocation.get(player.getUniqueId());
    }

    /**
     * Will update a player's location in the storage
     * and return the last value.
     *
     * @param player the player that is updated
     * @return the last stored server or null if no entry was found
     */
    public static ServerInfo updatePlayerLocation(ProxiedPlayer player) {
        return playerLocation.put(player.getUniqueId(), player.getServer().getInfo());
    }

    /**
     * Will remove a player's location from the storage.
     *
     * @param player the player that is checked
     */
    public static void removePlayerLocation(ProxiedPlayer player) {
        playerLocation.remove(player.getUniqueId());
    }
}
