package rlnt.networkutilities.proxy.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import rlnt.networkutilities.proxy.NetworkUtilities;
import rlnt.networkutilities.proxy.utils.Communication;
import rlnt.networkutilities.proxy.plugin.PluginLogger;
import rlnt.networkutilities.proxy.utils.Config;

import java.util.Collection;
import java.util.HashSet;

@SuppressWarnings("UnstableApiUsage")
public class PluginChannelListener implements Listener {

    private PluginLogger logger = NetworkUtilities.getInstance().getLogger();
    private Configuration messages = NetworkUtilities.getInstance().getMessages().getSection("firstJoinNotifications");

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPluginMessage(PluginMessageEvent event) {

        if (!event.getTag().equals("networkutilities")) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());

        String type = in.readUTF();

        if (type.equals("firstJoin")) {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(in.readUTF());

            // send first join notification to player
            if (Config.messageEnabled(messages, "player")) {
                if (Config.messageEmpty(messages, "player")) {
                    Communication.sendPlayerMessage(player, Config.getMessage(messages, "player")
                            .replace("%player%", player.getName())
                    );
                } else if (Config.messageEnabled(messages, "network") && Config.messageEmpty(messages, "network")) {
                    Communication.sendPlayerMessage(player, Config.getMessage(messages, "network")
                            .replace("%player%", player.getName())
                    );
                }
            }

            // send first join notification to network
            if (Config.messageEnabled(messages, "network") && Config.messageEmpty(messages, "network")) {
                Collection<ProxiedPlayer> playerList = new HashSet<>(ProxyServer.getInstance().getPlayers());
                playerList.remove(player);

                Communication.sendGroupMessage(playerList, Config.getMessage(messages, "network")
                        .replace("%player%", player.getName())
                );
            }
        } else {
            logger.warning("&c  > &eReceived an unhandled message from PluginMessageEvent!");
        }
    }
}
