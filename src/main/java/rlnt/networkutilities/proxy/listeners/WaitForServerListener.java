package rlnt.networkutilities.proxy.listeners;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import rlnt.networkutilities.proxy.utils.Communication;
import rlnt.networkutilities.proxy.utils.Config;
import rlnt.networkutilities.proxy.utils.Location;

import java.util.HashMap;
import java.util.Map;

public class WaitForServerListener implements Listener {

    private Configuration messages = Config.getMessages().getSection("waitForServer");

    @EventHandler(priority = EventPriority.HIGHEST)
    public void waitForServer(ServerConnectEvent event) {

        if (event.isCancelled()) return;

        // variables
        ProxiedPlayer player = event.getPlayer();
        ServerInfo lastServer = Location.getPlayerLocation(player);
        ServerInfo targetServer = event.getTarget();

        // check if target server is online
        targetServer.ping((pingResult, pingError) -> {
            if (pingError != null) {
                // server is offline
                // placeholder logic
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("{player}", player.getName());
                placeholders.put("{server}", targetServer.getName());

                // check if last server is defined
                if (lastServer == null) {
                    // player tries to connect to the network
                    Communication.playerCfgKick(player, messages, "kick", placeholders);
                } else {
                    // player is on the network and tries to connect to an offline server
                    Communication.playerCfgMsg(player, messages, "player", placeholders);
                }

                event.setCancelled(true);
            }
        });
    }
}
