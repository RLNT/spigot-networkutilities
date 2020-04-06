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

public class ServerKickListener implements Listener {

    // config entries
    private Configuration options = Config.getOptions();
    private Configuration messages = Config.getMessages();

    private Logger logger = NetworkUtilities.getInstance().getLogger();
    private ServerInfo fallbackServer = Server.getServerByName(options.getString("hubServer"));

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerKickEvent(ServerKickEvent event) {
        CountDownLatch doneSignal = new CountDownLatch(1);

        event.getKickedFrom().ping((serverPing, e) -> {
            // ping returned something, so we do not care
            if (serverPing != null)  {
                doneSignal.countDown();
                return;
            }

            // this _should_ be impossible
            if (e != null) {
                logger.severe("Ping returned (null, null). This is impossible so wtf did you do?.");
                doneSignal.countDown();
                return;
            }

            // if there is already a cancel server (for whatever reason), check if it's not the server the player came from.
            ServerInfo cancelServer = event.getCancelServer();
            if (cancelServer == null) { // no cancel server set
                event.setCancelServer(fallbackServer);
            } else { // cancel server is set, check if its not the original server
                if (cancelServer == event.getKickedFrom()) {
                    // this should not be possible
                    logger.warning("A player was set to connect to the same server he just came from, that is also unreachable");
                    event.setCancelServer(fallbackServer);
                }
            }

            // cancel event so the player get send to the cancel server
            event.setCancelled(true);

            // count down latch
            doneSignal.countDown();
        });

        // wait for at most one minute
        try {
            if (!doneSignal.await(1, TimeUnit.MINUTES)) {
                // latch timed out
                logger.warning("Timed out waiting for latch when handling a kick event");
            }
        } catch (InterruptedException e) {
            logger.warning("Thread got interupted while waiting for count down latch when handling a kick event");
            e.printStackTrace();
        }
    }
}
