package rlnt.networkutilities.proxy.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import rlnt.networkutilities.proxy.NetworkUtilities;
import rlnt.networkutilities.proxy.plugin.PluginLogger;
import rlnt.networkutilities.proxy.utils.Communication;
import rlnt.networkutilities.proxy.utils.Config;
import rlnt.networkutilities.proxy.utils.Server;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class PluginChannelListener implements Listener {

    private PluginLogger logger = NetworkUtilities.getInstance().getLogger();

    // config entries
    private Configuration options = Config.getOptions();
    private Configuration messages = Config.getMessages().getSection("firstJoinNotifications");

    // variables
    private ServerInfo hubServer = Server.getServerByName(options.getString("hubServer"));

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPluginMessage(PluginMessageEvent event) {

        if (!event.getTag().equals("networkutilities")) return;
        if (hubServer == null) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String type = in.readUTF();

        if (type.equals("firstJoin")) {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(in.readUTF());

            if (hubServer != player.getServer().getInfo()) return;

            Collection<ProxiedPlayer> playerList = new HashSet<>(ProxyServer.getInstance().getPlayers());
            playerList.remove(player);

            // placeholder logic
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("{player}", player.getName());

            // send first join notification to player
            if (!Config.messageEmpty(messages, "player")) {
                Communication.playerCfgMsg(player, messages, "player", placeholders);
            } else {
                Communication.playerCfgMsg(player, messages, "network", placeholders);
            }

            // send first join notification to network
            Communication.groupCfgMsg(playerList, messages, "network", placeholders);
        } else {
            logger.warning("&c  > &eReceived an unhandled message from PluginMessageEvent!");
        }
    }
}
