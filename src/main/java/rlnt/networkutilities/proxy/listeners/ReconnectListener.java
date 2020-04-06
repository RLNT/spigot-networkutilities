package rlnt.networkutilities.proxy.listeners;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import rlnt.networkutilities.proxy.NetworkUtilities;
import rlnt.networkutilities.proxy.utils.Config;
import rlnt.networkutilities.proxy.utils.Server;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ReconnectListener implements Listener {

    // TODO: send player a message that he was reconnected
    // TODO: send player a kick message that he could not be reconnected

    private Logger logger = NetworkUtilities.getInstance().getLogger();

    // config entries
    private Configuration options = Config.getOptions();

    private ServerInfo fallbackServer = Server.getServerByName(options.getString("hubServer"));

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerKickEvent(ServerKickEvent event) {
        CountDownLatch doneSignal = new CountDownLatch(1);

        ServerInfo server = event.getKickedFrom();
        ServerInfo cancelServer = event.getCancelServer();

        // check if cancel server is the fallback server
        if (cancelServer == fallbackServer) return;

        // check if the server the player was kicked from is still online
        server.ping((result, error) -> {
            if (error == null) {
                // server is online
                doneSignal.countDown();
                return;
            }

            // set the cancel server from the config
            if (cancelServer == null) {
                event.setCancelServer(fallbackServer);
            } else {
                if (cancelServer == server) {
                    // cancel server is the same that the player was kicked from
                    event.setCancelServer(fallbackServer);
                }
            }

            // cancel event so the player is sent to the cancel server
            event.setCancelled(true);

            // count down latch
            doneSignal.countDown();
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
