package rlnt.networkutilities.proxy.listeners;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import rlnt.networkutilities.proxy.NetworkUtilities;
import rlnt.networkutilities.proxy.utils.Communication;
import rlnt.networkutilities.proxy.utils.Location;

public class WaitForServerListener implements Listener {

    private Configuration messages = NetworkUtilities.getInstance().getMessages().getSection("waitForServer");

    @EventHandler(priority = EventPriority.HIGHEST)
    public void waitForServer(ServerConnectEvent event) {

        if (event.isCancelled()) return;

        ProxiedPlayer player = event.getPlayer();
        ServerInfo server = Location.getPlayerLocation(player);
        ServerInfo target = event.getTarget();

        target.ping((result, error) -> {
            if (error != null) {
                event.setCancelled(true);

                if (server == null) {
                    // player tries to connect to the network
                    Communication.sendPlayerKickMessage(player, messages.getString("kickMessage")
                            .replace("%player%", player.getName())
                            .replace("%server%", target.getName())
                    );
                } else {
                    // player is on the network and tries to connect to an offline server
                    Communication.sendPlayerMessage(player, messages.getString("playerMessage")
                            .replace("%player%", player.getName())
                            .replace("%server%", target.getName())
                    );
                }
            }
        });
    }
}
