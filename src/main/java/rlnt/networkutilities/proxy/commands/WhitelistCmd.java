package rlnt.networkutilities.proxy.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import rlnt.networkutilities.proxy.utils.Communication;
import rlnt.networkutilities.proxy.utils.Config;
import rlnt.networkutilities.proxy.utils.Player;

import java.util.List;

public class WhitelistCmd extends Command {

    // config entries
    private static Configuration options = Config.getCommandCfg().getSection("whitelist");
    private static Configuration messages = Config.getCommandMsg().getSection("whitelist");

    public WhitelistCmd() {
        super("whitelist", getCommandPermission(), getCommandAlias());
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

        // define the sub command
        String subcommand;
        if (args == null || args.length == 0 || args[0].equals("?") || args[0].equals("help")) {
            subcommand = null;
        } else {
            subcommand = args[0];
        }

        // sub command logic
        if (subcommand == null) {
            // nothing, help or ? has been entered, display help text
            Configuration helpText = messages.getSection("helpText");

            if (isPlayer) {
                // player used the command
                if (Player.hasPermission(player, "networkutilities.command.whitelist")) {
                    // player has permission
                    Communication.playerCfgMsg(player, helpText, "player");
                }
            } else {
                // nonplayer used the command
                Communication.senderCfgMsg(sender, helpText, "nonplayer");
            }
        } else {
            switch (subcommand) {
                case "check":
                    // check sub command was sent

                    break;
                case "add":
                    // do stuff
                    break;
                case "remove":
                    // do stuff
                    break;
            }
        }
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
}
