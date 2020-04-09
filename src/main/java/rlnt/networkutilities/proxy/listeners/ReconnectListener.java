package rlnt.networkutilities.proxy.listeners;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import rlnt.networkutilities.proxy.NetworkUtilities;
import rlnt.networkutilities.proxy.utils.Communication;
import rlnt.networkutilities.proxy.utils.Config;
import rlnt.networkutilities.proxy.utils.General;
import rlnt.networkutilities.proxy.utils.Server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class ReconnectListener implements Listener {

    private Logger logger = NetworkUtilities.getInstance().getLogger();

    // config entries
    private Configuration options = Config.getOptions();
    private Configuration messages = Config.getMessages().getSection("reconnectOnCrash");

    private ServerInfo fallbackServer = Server.getServerByName(options.getString("hubServer"));

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerKickEvent(ServerKickEvent event) {
        CountDownLatch doneSignal = new CountDownLatch(1);
        ServerInfo server = event.getKickedFrom();

        // ignore event if player got kicked from fallback server
        if (server == fallbackServer) return;

        // check if the server the player was kicked from is still online
        AtomicBoolean cancelEvent = new AtomicBoolean(false);
        server.ping((pingResult, pingError) -> {
            // cancel event if server is online
            if (pingError == null) {
                doneSignal.countDown();
                cancelEvent.set(true);
            }
        });
        if (cancelEvent.get()) return;

        // check if fallback server is online
        fallbackServer.ping((pingResult, pingError) -> {
            // cancel event if server is offline, kick player with reason
            if (pingError == null) {
                if (Config.messageEmpty(messages, "offline")) {
                    event.setKickReasonComponent(General.colorize("&4Couldn't reconnect you because the fallback server is down!"));
                } else {
                    event.setKickReasonComponent(General.colorize(Config.getMessage(messages, "offline")));
                }

                doneSignal.countDown();
                cancelEvent.set(true);
            }
        });
        if (cancelEvent.get()) return;

        ProxiedPlayer player = event.getPlayer();

        // try to connect player to fallback server
        player.connect(fallbackServer, (connectResult, connectError) -> {
            if (connectError == null) {
                // reconnect successful, send player message
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("{server}", fallbackServer.getName());
                Communication.playerCfgMsg(player, messages, "success", placeholders);
            } else {
                // reconnect failed, kick player with reason
                if (Config.messageEmpty(messages, "failed")) {
                    event.setKickReasonComponent(General.colorize("&4Couldn't reconnect you to the fallback server!"));
                } else {
                    event.setKickReasonComponent(General.colorize(Config.getMessage(messages, "failed")));
                }

                doneSignal.countDown();
                return;
            }

            // cancel event
            event.setCancelServer(null);
            event.setCancelled(true);

            // count down latch
            doneSignal.countDown();
        }, ServerConnectEvent.Reason.LOBBY_FALLBACK);

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
