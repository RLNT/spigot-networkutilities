package rlnt.networkutilities.spigot.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import rlnt.networkutilities.spigot.NetworkUtilities;
import rlnt.networkutilities.spigot.utils.PluginLogger;

@SuppressWarnings("UnstableApiUsage")
public class PluginChannel implements PluginMessageListener {

    private PluginLogger logger = NetworkUtilities.getInstance().getLogger();

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {

        if (!channel.equals("BungeeCord")) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);

        if (in.readUTF().equals("networkutilities")) {
            logger.warning("&c  > &eReceived an unhandled message from PluginMessageEvent!");
        }
    }
}
