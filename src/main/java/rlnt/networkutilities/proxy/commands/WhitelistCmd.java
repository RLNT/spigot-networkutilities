package rlnt.networkutilities.proxy.commands;

import com.google.common.collect.ImmutableMap;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import rlnt.networkutilities.proxy.api.ApiException;
import rlnt.networkutilities.proxy.api.Minecraft;
import rlnt.networkutilities.proxy.utils.Communication;
import rlnt.networkutilities.proxy.utils.Config;
import rlnt.networkutilities.proxy.utils.Player;
import rlnt.networkutilities.proxy.utils.Whitelist;

import java.util.List;
import java.util.UUID;

public class WhitelistCmd extends Command {

	// config entries
	private static Configuration options = Config.getOptions().getSection("commands").getSection("whitelist");
	private static Configuration messages = Config.getMessages().getSection("commands").getSection("whitelist");

	public WhitelistCmd() {
		super("whitelist", getCommandPermission(), getCommandAlias());
	}

	/**
	 * Will return the command permission which the player needs to use the
	 * command.
	 *
	 * @return the command permission or null if disabled
	 */
	private static String getCommandPermission() {
		if (options.getBoolean("permissionRequired", true)) {
			return "networkutilities.command.whitelist";
		} else {
			return null;
		}
	}

	/**
	 * Will return the command alias which has the same execution
	 * functionality like the command name.
	 *
	 * @return the command alias
	 */
	private static String[] getCommandAlias() {
		if (options.getStringList("commandAlias").isEmpty()) {
			return null;
		} else {
			List<String> aliasesList = options.getStringList("commandAliases");
			return aliasesList.toArray(new String[0]);
		}
	}

	private static UUID getUuidFromPlayerOrUuid(String text) {
		try {
			return UUID.fromString(text);
		} catch (IllegalArgumentException ignored) {
		}

		try {
			return Minecraft.getUuid(text);
		} catch (ApiException e) {
			return null;
		}
	}

	private static boolean checkPermission(CommandSender sender, String subpermission) {
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer player = (ProxiedPlayer) sender;

			String permission = getCommandPermission();
			if (permission == null) {
				return true;
			}

			// check permission
			return Player.hasPermission(player, permission + "." + subpermission);
		}

		// sender is probably console -> always allow
		return true;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		// check if the command sender is a player
		boolean isPlayer;
		ProxiedPlayer player;
		if (sender instanceof ProxiedPlayer) {
			isPlayer = true;
			player = (ProxiedPlayer) sender;
		} else {
			isPlayer = false;
			player = null;
		}

		if (checkPermission(sender, "")) {
			return;
		}

		// define the sub command
		String subcommand;
		if (args == null || args.length == 0 || args[0].equals("?") || args[0].equals("help")) {
			subcommand = null;
		} else {
			subcommand = args[0];
		}

		Configuration helpText = messages.getSection("helpText");

		// java is retarded and does not have scoped switchs
		UUID id;

		// sub command logic
		if (subcommand == null) {
			// nothing, help or ? has been entered, display help text

			if (isPlayer) {
				// player used the command
				Communication.playerCfgMsg(player, helpText, "player");
			} else {
				// nonplayer used the command
				Communication.senderCfgMsg(sender, helpText, "nonplayer");
			}

			return;
		}


		switch (subcommand) {
			case "check":
				// todo: make text clickable to remove or add the player to the whitelist

				if (checkPermission(sender, "check")) {
					break;
				}

				Configuration checkMessages = messages.getSection("check");
				if (args.length < 2) {
					Communication.playerCfgMsg(player, checkMessages, "missing");
					break;
				}

				id = getUuidFromPlayerOrUuid(args[1]);
				if (id == null) {
					Communication.playerCfgMsg(player, checkMessages, "invalid");
					break;
				}

				if (Whitelist.isWhitelisted(id)) {
					Communication.playerCfgMsg(player, checkMessages, "whitelisted", ImmutableMap.of("{uuid}", id.toString()));
				} else {
					Communication.playerCfgMsg(player, checkMessages, "notwhitelisted", ImmutableMap.of("{uuid}", id.toString()));
				}
				break;
			case "list":
				// todo: make pageable

				if (checkPermission(sender, "list")) {
					break;
				}

				Configuration listMessages = messages.getSection("list");
				Communication.playerCfgMsg(player, listMessages, "list",
						ImmutableMap.of("{entries}", String.join("\n", Whitelist.getWhitelist()))
				);
				break;
			case "add":

				if (checkPermission(sender, "add")) {
					break;
				}

				Configuration addMessages = messages.getSection("add");
				if (args.length < 2) {
					Communication.playerCfgMsg(player, addMessages, "missing");
					break;
				}

				id = getUuidFromPlayerOrUuid(args[1]);
				if (id == null) {
					Communication.playerCfgMsg(player, addMessages, "invalid");
					break;
				}

				if (Whitelist.addWhitelist(id)) {
					Communication.playerCfgMsg(player, addMessages, "success");
				} else {
					Communication.playerCfgMsg(player, addMessages, "duplicate");
				}

				break;
			case "delete":
			case "remove":

				if (checkPermission(sender, "remove")) {
					break;
				}

				Configuration removeMessages = messages.getSection("remove");
				if (args.length < 2) {
					Communication.playerCfgMsg(player, removeMessages, "missing");
					break;
				}

				id = getUuidFromPlayerOrUuid(args[1]);
				if (id == null) {
					Communication.playerCfgMsg(player, removeMessages, "invalid");
					break;
				}

				if (Whitelist.removeWhitelist(id)) {
					Communication.playerCfgMsg(player, removeMessages, "success");
				} else {
					Communication.playerCfgMsg(player, removeMessages, "duplicate");
				}

				break;
			default:
				// player used unknown subcommand
				Communication.playerCfgMsg(player, helpText, "player");
		}
	}
}
