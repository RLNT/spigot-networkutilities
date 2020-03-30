package rlnt.networkutilities.proxy.listeners;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import rlnt.networkutilities.proxy.utils.Whitelist;

import java.util.UUID;

public class WhitelistListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogin(LoginEvent event) {

		if (event.isCancelled()) return;

		PendingConnection connection = event.getConnection();
		UUID uuid = connection.getUniqueId();

		if (!connection.isOnlineMode() || uuid == null) {
			event.setCancelled(true);
			event.setCancelReason(new TextComponent("get kicked for cracked account"));
			return;
		}

		if (!Whitelist.isWhitelisted(uuid)) {
			event.setCancelled(true);
			event.setCancelReason(new TextComponent("get kicked for not being whitelisted"));
		}
	}
}
