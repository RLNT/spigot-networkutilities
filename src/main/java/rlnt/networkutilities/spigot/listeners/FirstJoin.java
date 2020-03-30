package rlnt.networkutilities.spigot.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import rlnt.networkutilities.spigot.utils.Utils;

public class FirstJoin implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // send a network broadcast if a player joins for the first time
        Player player = event.getPlayer();

        if (!player.hasPlayedBefore()) {
            Utils.sendFirstJoinBungee(player);
        }
    }
}
