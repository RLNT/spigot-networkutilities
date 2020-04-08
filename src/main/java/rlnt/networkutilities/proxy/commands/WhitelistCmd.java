package rlnt.networkutilities.proxy.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import rlnt.networkutilities.proxy.api.ApiException;
import rlnt.networkutilities.proxy.api.Minecraft;
import rlnt.networkutilities.proxy.plugin.PluginConfigException;
import rlnt.networkutilities.proxy.utils.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

// TODO: permission required option kann weg, aus config entfernen und aus allen commands
// TODO: whitelist save when plugin disabling

public class WhitelistCmd extends Command {

    // config entries
    private static Configuration options = Config.getOptions().getSection("commands").getSection("whitelist");
    private static Configuration messages = Config.getMessages().getSection("commands").getSection("whitelist");

    public WhitelistCmd() {
        super("whitelist", "networkutilities.command.whitelist", getCommandAlias());
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

        BiConsumer<Configuration, String> message = (section, token) -> {
            if (isPlayer) {
                Communication.playerCfgMsg(player, section, token);
            } else {
                Communication.senderCfgMsg(sender, section, token);
            }
        };

        // define the sub command
        String subcommand;
        if (args == null || args.length == 0 || args[0].equals("?")) {
            subcommand = null;
        } else {
            subcommand = args[0];
        }

        Configuration helpText = messages.getSection("help");

        // sub command logic
        if (subcommand == null) {
            // nothing or ? has been entered, display help text
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
            case "check": {
                // TODO: make text clickable to add or remove the player to/from the whitelist
                if (isPlayer) {
                    // player entered the command
                    if (!Player.hasPermission(player, "networkutilities.command.whitelist.check")) break;
                }

                Configuration check = messages.getSection("check");
                Consumer<String> msg = token -> message.accept(check, token);

                // check command length - check needs 1 argument
                if (args.length < 2) {
                    // argument is missing
                    msg.accept("missing");
                    break;
                } else if (args.length > 2) {
                    // too many arguments
                    msg.accept("toomany");
                    break;
                }

                // get uuid from input
                String input = args[1];
                UUID uuid;
                if (General.isUuid(input)) {
                    uuid = UUID.fromString(input);

                    // check if the uuid points to a player name
                    try {
                        Minecraft.getUsername(uuid);
                    } catch (ApiException e) {
                        msg.accept("invalidUuid");
                        break;
                    }
                } else {
                    // check if the player name points to a uuid
                    try {
                        uuid = Minecraft.getUuid(input);
                    } catch (ApiException e) {
                        msg.accept("invalidName");
                        break;
                    }
                }

                // placeholder logic
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("{uuid}", uuid.toString());

                // check if whitelisted
                if (Whitelist.isWhitelisted(uuid)) {
                    // requested player is whitelisted
                    msg.accept("whitelisted");
                    if (isPlayer) {
                        Communication.playerCfgMsg(player, check, "whitelisted", placeholders);
                    } else {
                        Communication.senderCfgMsg(sender, check, "whitelisted", placeholders);
                    }
                } else {
                    if (isPlayer) {
                        Communication.playerCfgMsg(player, check, "notWhitelisted", placeholders);
                    } else {
                        Communication.senderCfgMsg(sender, check, "notWhitelisted", placeholders);
                    }
                }

                break;
            }
            case "list": {
                // TODO: make the list pageable
                // TODO: make GUI for list
                if (isPlayer) {
                    // player entered the command
                    if (!Player.hasPermission(player, "networkutilities.command.whitelist.list")) break;
                }

                Configuration list = messages.getSection("list");
                Consumer<String> msg = token -> message.accept(list, token);

                // check command length - list needs no argument
                if (args.length > 2) {
                    // to many arguments
                    msg.accept("toomany");
                    break;
                }

                // placeholder logic
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("{whitelist}", String.join("\n", Whitelist.getWhitelist()));

                // list the whitelist
                if (isPlayer) {
                    Communication.playerCfgMsg(player, list, "player", placeholders);
                } else {
                    Communication.senderCfgMsg(sender, list, "console", placeholders);
                }

                break;
            }
            case "add": {
                if (isPlayer) {
                    // player entered the command
                    if (!Player.hasPermission(player, "networkutilities.command.whitelist.add")) break;
                }

                Configuration add = messages.getSection("add");
                Consumer<String> msg = token -> message.accept(add, token);

                // check command length - add needs 1 argument
                if (args.length < 2) {
                    // argument is missing
                    msg.accept("missing");
                    break;
                } else if (args.length > 2) {
                    // too many arguments
                    msg.accept("missing");
                    break;
                }

                // get uuid from input
                String input = args[1];
                UUID uuid;
                if (General.isUuid(input)) {
                    uuid = UUID.fromString(input);

                    // check if the uuid points to a player name
                    try {
                        Minecraft.getUsername(uuid);
                    } catch (ApiException e) {
                        msg.accept("invalidUuid");
                        break;
                    }
                } else {
                    // check if the player name points to a uuid
                    try {
                        uuid = Minecraft.getUuid(input);
                    } catch (ApiException e) {
                        msg.accept("invalidName");
                        break;
                    }
                }

                // placeholder logic
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("{uuid}", uuid.toString());

                // check if player is whitelisted already
                if (Whitelist.isWhitelisted(uuid)) {
                    // player is already whitelisted
                    if (isPlayer) {
                        Communication.playerCfgMsg(player, add, "already", placeholders);
                    } else {
                        Communication.senderCfgMsg(sender, add, "already", placeholders);
                    }
                    break;
                }

                // add to whitelist
                try {
                    if (Whitelist.add(uuid)) {
                        // added successfully
                        if (isPlayer) {
                            Communication.playerCfgMsg(player, add, "success", placeholders);
                        } else {
                            Communication.senderCfgMsg(sender, add, "success", placeholders);
                        }
                    } else {
                        // failed to add
                        if (isPlayer) {
                            Communication.playerCfgMsg(player, add, "failed", placeholders);
                        } else {
                            Communication.senderCfgMsg(sender, add, "failed", placeholders);
                        }
                    }
                } catch (PluginConfigException e) {
                    e.printStackTrace();
                }

                break;
            }
            case "remove":
            case "delete": {
                if (isPlayer) {
                    // player entered the command
                    if (!Player.hasPermission(player, "networkutilities.command.whitelist.remove")) break;
                }

                Configuration remove = messages.getSection("remove");
                Consumer<String> msg = token -> message.accept(remove, token);

                // check command length - remove needs 1 argument
                if (args.length < 2) {
                    // argument is missing
                    msg.accept("missing");
                    break;
                } else if (args.length > 2) {
                    // too many arguments
                    msg.accept("toomany");
                    break;
                }

                // get uuid from input
                String input = args[1];
                UUID uuid;
                if (General.isUuid(input)) {
                    uuid = UUID.fromString(input);

                    // check if the uuid points to a player name
                    try {
                        Minecraft.getUsername(uuid);
                    } catch (ApiException e) {
                        msg.accept("invalidUuid");
                        break;
                    }
                } else {
                    // check if the player name points to a uuid
                    try {
                        uuid = Minecraft.getUuid(input);
                    } catch (ApiException e) {
                        msg.accept("invalidName");
                        break;
                    }
                }

                // placeholder logic
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("{uuid}", uuid.toString());

                // check if player is unwhitelisted already
                if (!Whitelist.isWhitelisted(uuid)) {
                    // player is already unwhitelisted
                    if (isPlayer) {
                        Communication.playerCfgMsg(player, remove, "already", placeholders);
                    } else {
                        Communication.senderCfgMsg(sender, remove, "already", placeholders);
                    }
                    break;
                }

                // remove from whitelist
                try {
                    if (Whitelist.remove(uuid)) {
                        // added successfully
                        if (isPlayer) {
                            Communication.playerCfgMsg(player, remove, "success", placeholders);
                        } else {
                            Communication.senderCfgMsg(sender, remove, "success", placeholders);
                        }
                    } else {
                        // failed to add
                        if (isPlayer) {
                            Communication.playerCfgMsg(player, remove, "failed", placeholders);
                        } else {
                            Communication.senderCfgMsg(sender, remove, "failed", placeholders);
                        }
                    }
                } catch (PluginConfigException e) {
                    e.printStackTrace();
                }

                break;
            }
        }
    }
}
