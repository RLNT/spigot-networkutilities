package rlnt.networkutilities.spigot.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import rlnt.networkutilities.spigot.NetworkUtilities;

@SuppressWarnings("UnstableApiUsage")
public enum Utils {
    ;

    /**
     * Will colorize a given message by alternate color codes.
     *
     * @param message the message that should be colorized
     * @return the colorized message
     */
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Will disable the plugin.
     *
     * @param plugin the plugin instance
     */
    public static void disablePlugin(JavaPlugin plugin) {
        plugin.getPluginLoader().disablePlugin(plugin);
    }

    /**
     * Will send a message to the proxy instance to trigger
     * the broadcast for the first player join event.
     *
     * @param player the player object
     */
    public static void sendFirstJoinBungee(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("networkutilities");
        out.writeUTF("firstjoin");
        out.writeUTF(player.getName());

        player.sendPluginMessage(NetworkUtilities.getInstance(), "BungeeCord", out.toByteArray());
    }
}
