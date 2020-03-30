package rlnt.networkutilities.spigot.listeners;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import rlnt.networkutilities.spigot.NetworkUtilities;

public class ServerNotifications implements Listener {

    ConfigurationSection options = NetworkUtilities.getInstance().getConfig().getConfigurationSection("serverNotifications");

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // disable join message if enabled in the config
        if (options.getBoolean("disableJoinMessages", false))
            event.setJoinMessage(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // disable quit message if enabled in the config
        if (options.getBoolean("disableQuitMessages", false))
            event.setQuitMessage(null);
    }
}
