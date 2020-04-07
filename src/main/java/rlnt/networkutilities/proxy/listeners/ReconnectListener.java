package rlnt.networkutilities.proxy.listeners;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import rlnt.networkutilities.proxy.NetworkUtilities;
import rlnt.networkutilities.proxy.utils.Communication;
import rlnt.networkutilities.proxy.utils.Config;
import rlnt.networkutilities.proxy.utils.Server;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class ReconnectListener implements Listener {

    private Logger logger = NetworkUtilities.getInstance().getLogger();

    // config entries
    private Configuration options = Config.getOptions();
    private Configuration messages = Config.getMessages().getSection("commands").getSection("reconnect");

    private ServerInfo fallbackServer = Server.getServerByName(options.getString("hubServer"));

    private static BaseComponent[] translateToComponent(String s) {
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', s));
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerKickEvent(ServerKickEvent event) {
        // if there is a kick reason, the kick cannot be because of a crash -> ignore the event
        if (event.getKickReasonComponent().length > 0) return;

        CountDownLatch doneSignal = new CountDownLatch(1);

        ServerInfo server = event.getKickedFrom();

        // ignore event if player got kicked from fallback server
        if (server == fallbackServer) return;

        // check if the server the player was kicked from is still online
        server.ping((result, pingError) -> {
            // ignore event if server is online
            if (pingError == null) {
                doneSignal.countDown();
                return;
            }

            // try to connect player to fallback server
            event.getPlayer().connect(fallbackServer, (connected, connectError) -> {
                if (connectError != null) {
                    event.setKickReasonComponent(translateToComponent(
                        "The server you were on could not be reached.\n" +
                            "Reconnecting to &c" + fallbackServer.getName() + "&e failed: " +
                            connectError.toString()
                    ));

                    logger.warning("Could not reconnect player");

                    doneSignal.countDown();
                    return;
                }

                // send message to player
                Communication.playerCfgMsg(event.getPlayer(), messages, "reconnected");

                // cancel event
                event.setCancelServer(null);
                event.setCancelled(true);

                // count down latch
                doneSignal.countDown();
            }, ServerConnectEvent.Reason.LOBBY_FALLBACK);
        });

        // wait for at most one minute
        try {
            if (!doneSignal.await(1, TimeUnit.MINUTES)) {
                // latch timed out
                logger.warning("&c  > &eTask timed out while waiting for countdown latch handling a kick event for player &c" + event.getPlayer().getName() + "&e!");
            }
        } catch (InterruptedException e) {
            logger.warning("&c  > &eThread was interrupted while waiting for the countdown latch handling a kick event for player &c" + event.getPlayer().getName() + "&e!");
            e.printStackTrace();
        }
    }
}
