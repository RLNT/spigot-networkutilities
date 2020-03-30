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
import rlnt.networkutilities.proxy.NetworkUtilities;
import rlnt.networkutilities.proxy.utils.Communication;
import rlnt.networkutilities.proxy.utils.Config;
import rlnt.networkutilities.proxy.utils.Location;

import java.util.*;

public class ConNotifiesListener implements Listener {

    private Configuration messages = NetworkUtilities.getInstance().getMessages().getSection("connectionNotifications");
    private Configuration networkJoin = messages.getSection("networkJoin");
    private Configuration serverSwitch = messages.getSection("serverSwitch");
    private Configuration networkQuit = messages.getSection("networkQuit");

    @EventHandler(priority = EventPriority.HIGHEST)
    public void serverConnect(ServerSwitchEvent event) {

        ProxiedPlayer player = event.getPlayer();
        Collection<ProxiedPlayer> playerList = new HashSet<>(ProxyServer.getInstance().getPlayers());
        playerList.remove(player);

        // save last server the player was on and update the map
        ServerInfo oldServer = Location.updatePlayerLocation(player);

        if (oldServer == null) {
            // player connected to the network

            // network join notification for the player
            if (Config.messageEnabled(networkJoin, "player")) {
                if (Config.messageEmpty(networkJoin, "player")) {
                    Communication.sendPlayerMessage(player, Config.getMessage(networkJoin, "player")
                            .replace("%player%", player.getName())
                            .replace("%server%", player.getServer().getInfo().getName())
                    );
                } else if (Config.messageEnabled(networkJoin, "network") && Config.messageEmpty(networkJoin, "network")) {
                    Communication.sendPlayerMessage(player, Config.getMessage(networkJoin, "network")
                            .replace("%player%", player.getName())
                            .replace("%server%", player.getServer().getInfo().getName())
                    );
                }
            }

            // network join notification for the network
            if (Config.messageEnabled(networkJoin, "network") && Config.messageEmpty(networkJoin, "network")) {
                Communication.sendGroupMessage(playerList, Config.getMessage(networkJoin, "network")
                        .replace("%player%", player.getName())
                        .replace("%server%", player.getServer().getInfo().getName())
                );
            }
        } else {
            // player switched the server
            ServerInfo newServer = player.getServer().getInfo();

            // server switch notification for the player
            if (Config.messageEnabled(serverSwitch, "player")) {
                if (Config.messageEmpty(serverSwitch, "player")) {
                    Communication.sendPlayerMessage(player, Config.getMessage(serverSwitch, "player")
                            .replace("%player%", player.getName())
                            .replace("%old%", oldServer.getName())
                            .replace("%new%", newServer.getName())
                    );
                } else if (Config.messageEnabled(serverSwitch, "network") && Config.messageEmpty(serverSwitch, "network")) {
                    Communication.sendPlayerMessage(player, Config.getMessage(serverSwitch, "network")
                            .replace("%player%", player.getName())
                            .replace("%old%", oldServer.getName())
                            .replace("%new%", newServer.getName())
                    );
                }
            }

            // server switch notification for the network
            if (Config.messageEnabled(serverSwitch, "network")) {
                // server switch notification for the old server
                if (Config.messageEmpty(serverSwitch, "oldServer")) {
                    Collection<ProxiedPlayer> oldServerPlayers = new HashSet<>(oldServer.getPlayers());
                    oldServerPlayers.remove(player);

                    Communication.sendGroupMessage(oldServerPlayers, Config.getMessage(serverSwitch, "oldServer")
                            .replace("%player%", player.getName())
                            .replace("%old%", oldServer.getName())
                            .replace("%new%", newServer.getName())
                    );

                    playerList.removeAll(oldServer.getPlayers());
                }

                // server switch notification for the new server
                if (Config.messageEmpty(serverSwitch, "newServer")) {
                    Collection<ProxiedPlayer> newServerPlayers = new HashSet<>(newServer.getPlayers());
                    newServerPlayers.remove(player);

                    Communication.sendGroupMessage(newServerPlayers, Config.getMessage(serverSwitch, "newServer")
                            .replace("%player%", player.getName())
                            .replace("%old%", oldServer.getName())
                            .replace("%new%", newServer.getName())
                    );

                    playerList.removeAll(newServer.getPlayers());
                }

                // server switch notification for the rest of the network
                if (Config.messageEmpty(serverSwitch, "network")) {
                    Communication.sendGroupMessage(playerList, Config.getMessage(serverSwitch, "network")
                            .replace("%player%", player.getName())
                            .replace("%old%", oldServer.getName())
                            .replace("%new%", newServer.getName())
                    );
                }
            }
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

        // send disconnect broadcast to the network
        if (networkQuit.getBoolean("enabled", false) && !networkQuit.getString("message").isEmpty()) {
            Communication.sendNetworkMessage(networkQuit.getString("message")
                    .replace("%player%", player.getName())
                    .replace("%server%", server.getName())
            );
        }
    }
}
