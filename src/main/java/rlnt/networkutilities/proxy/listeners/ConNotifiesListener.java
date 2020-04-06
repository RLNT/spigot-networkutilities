package rlnt.networkutilities.proxy.listeners;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import rlnt.networkutilities.proxy.utils.Communication;
import rlnt.networkutilities.proxy.utils.Config;
import rlnt.networkutilities.proxy.utils.Location;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ConNotifiesListener implements Listener {

    // config entries
    private Configuration messages = Config.getMessages().getSection("connectionNotifications");

    @EventHandler(priority = EventPriority.HIGHEST)
    public void serverConnect(ServerSwitchEvent event) {

        ProxiedPlayer player = event.getPlayer();
        Collection<ProxiedPlayer> playerList = new HashSet<>(ProxyServer.getInstance().getPlayers());
        playerList.remove(player);

        // save last server the player was on and update the map
        // if the key had a value already, it's stored to oldServer
        ServerInfo oldServer = Location.updatePlayerLocation(player);

        if (oldServer == null) {
            // player connected to the network

            // messages
            Configuration networkJoin = messages.getSection("networkJoin");

            // placeholder logic
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("{player}", player.getName());
            placeholders.put("{server}", player.getServer().getInfo().getName());

            // network join notification for the player
            if (!Config.messageEmpty(networkJoin, "player")) {
                Communication.playerCfgMsg(player, networkJoin, "player", placeholders);
            } else {
                Communication.playerCfgMsg(player, networkJoin, "network", placeholders);
            }

            // network join notification for the network
            Communication.groupCfgMsg(playerList, networkJoin, "network", placeholders);
        } else {
            // player switched the server
            ServerInfo newServer = player.getServer().getInfo();

            // messages
            Configuration serverSwitch = messages.getSection("serverSwitch");

            // placeholder logic
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("{player}", player.getName());
            placeholders.put("{old}", oldServer.getName());
            placeholders.put("{new}", newServer.getName());

            // server switch notification for the player
            if (!Config.messageEmpty(serverSwitch, "player")) {
                Communication.playerCfgMsg(player, serverSwitch, "player", placeholders);
            } else {
                Communication.playerCfgMsg(player, serverSwitch, "network", placeholders);
            }

            // server switch notification for the network
            // old server
            Collection<ProxiedPlayer> oldServerPlayers = new HashSet<>(oldServer.getPlayers());
            oldServerPlayers.remove(player);
            Communication.groupCfgMsg(oldServerPlayers, serverSwitch, "oldServer", placeholders);

            playerList.removeAll(oldServer.getPlayers());

            // new server
            Collection<ProxiedPlayer> newServerPlayers = new HashSet<>(newServer.getPlayers());
            newServerPlayers.remove(player);
            Communication.groupCfgMsg(newServerPlayers, serverSwitch, "newServer", placeholders);

            playerList.removeAll(newServer.getPlayers());

            // rest of the network
            Communication.groupCfgMsg(playerList, serverSwitch, "network", placeholders);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDisconnect(PlayerDisconnectEvent event) {

        ProxiedPlayer player = event.getPlayer();

        // get last server the player was on
        ServerInfo server = Location.getPlayerLocation(player);
        if (server == null) return;

        // remove player from map since they disconnected
        Location.removePlayerLocation(player);

        // placeholder logic
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{player}", player.getName());
        placeholders.put("%server%", server.getName());

        // send disconnect broadcast to the network
        Communication.networkCfgMsg(messages, "networkQuit", placeholders);
    }
}
