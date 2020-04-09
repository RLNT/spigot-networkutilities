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

        TriConsumer<Configuration, String, Map<String, String>> message = (section, token, placeholders) -> {
            if (isPlayer) {
                if (placeholders == null) {
                    Communication.playerCfgMsg(player, section, token);
                } else {
                    Communication.playerCfgMsg(player, section, token, placeholders);
                }
            } else {
                if (placeholders == null) {
                    Communication.senderCfgMsg(sender, section, token);
                } else {
                    Communication.senderCfgMsg(sender, section, token, placeholders);
                }
            }
        };

        // define the sub command
        String subcommand;
        if (args == null || args.length == 0 || args[0].equals("?")) {
            subcommand = null;
        } else {
            subcommand = args[0];
        }

        Configuration help = messages.getSection("help");

        // sub command logic
        if (subcommand == null) {
            // nothing or ? has been entered, display help text
            if (isPlayer) {
                // player used the command
                Communication.playerCfgMsg(player, help, "player");
            } else {
                // nonplayer used the command
                Communication.senderCfgMsg(sender, help, "console");
            }
            return;
        }

        switch (subcommand) {
            case "check": {
                // TODO: make text clickable to add or remove the player to/from the whitelist
                if (isPlayer && !Player.hasPermission(player, "networkutilities.command.whitelist.check")) break;

                Configuration check = messages.getSection("check");
                BiConsumer<String, Map<String, String>> msg = (token, placeholders) -> message.accept(check, token, placeholders);

                // check command length - check needs 1 argument
                if (args.length < 2) {
                    // argument is missing
                    msg.accept("missing", null);
                    break;
                } else if (args.length > 2) {
                    // too many arguments
                    msg.accept("toomany", null);
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
                        msg.accept("invalidUuid", null);
                        break;
                    }
                } else {
                    // check if the player name points to a uuid
                    try {
                        uuid = Minecraft.getUuid(input);
                    } catch (ApiException e) {
                        msg.accept("invalidName", null);
                        break;
                    }
                }

                // placeholder logic
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("{uuid}", uuid.toString());

                // check if whitelisted
                if (Whitelist.isWhitelisted(uuid)) {
                    // requested player is whitelisted
                    msg.accept("whitelisted", placeholders);
                } else {
                    msg.accept("notWhitelisted", placeholders);
                }

                break;
            }
            case "list": {
                // TODO: make the list pageable
                // TODO: make GUI for list
                if (isPlayer && !Player.hasPermission(player, "networkutilities.command.whitelist.list")) break;

                Configuration list = messages.getSection("list");
                BiConsumer<String, Map<String, String>> msg = (token, placeholders) -> message.accept(list, token, placeholders);

                // check command length - list needs no argument
                if (args.length > 2) {
                    // to many arguments
                    msg.accept("toomany", null);
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
                if (isPlayer && !Player.hasPermission(player, "networkutilities.command.whitelist.add")) break;

                Configuration add = messages.getSection("add");
                BiConsumer<String, Map<String, String>> msg = (token, placeholders) -> message.accept(add, token, placeholders);

                // check command length - add needs 1 argument
                if (args.length < 2) {
                    // argument is missing
                    msg.accept("missing", null);
                    break;
                } else if (args.length > 2) {
                    // too many arguments
                    msg.accept("missing", null);
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
                        msg.accept("invalidUuid", null);
                        break;
                    }
                } else {
                    // check if the player name points to a uuid
                    try {
                        uuid = Minecraft.getUuid(input);
                    } catch (ApiException e) {
                        msg.accept("invalidName", null);
                        break;
                    }
                }

                // placeholder logic
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("{uuid}", uuid.toString());

                // check if player is whitelisted already
                if (Whitelist.isWhitelisted(uuid)) {
                    // player is already whitelisted
                    msg.accept("already", placeholders);
                    break;
                }

                // add to whitelist
                try {
                    if (Whitelist.add(uuid)) {
                        // added successfully
                        msg.accept("success", placeholders);
                    } else {
                        // failed to add
                        msg.accept("failed", placeholders);
                    }
                } catch (PluginConfigException e) {
                    e.printStackTrace();
                }

                break;
            }
            case "remove":
            case "delete": {
                if (isPlayer && !Player.hasPermission(player, "networkutilities.command.whitelist.remove")) break;

                Configuration remove = messages.getSection("remove");
                BiConsumer<String, Map<String, String>> msg = (token, placeholders) -> message.accept(remove, token, placeholders);

                // check command length - remove needs 1 argument
                if (args.length < 2) {
                    // argument is missing
                    msg.accept("missing", null);
                    break;
                } else if (args.length > 2) {
                    // too many arguments
                    msg.accept("toomany", null);
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
                        msg.accept("invalidUuid", null);
                        break;
                    }
                } else {
                    // check if the player name points to a uuid
                    try {
                        uuid = Minecraft.getUuid(input);
                    } catch (ApiException e) {
                        msg.accept("invalidName", null);
                        break;
                    }
                }

                // placeholder logic
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("{uuid}", uuid.toString());

                // check if player is unwhitelisted already
                if (!Whitelist.isWhitelisted(uuid)) {
                    // player is already unwhitelisted
                    msg.accept("already", placeholders);
                    break;
                }

                // remove from whitelist
                try {
                    if (Whitelist.remove(uuid)) {
                        // added successfully
                        msg.accept("success", placeholders);
                    } else {
                        // failed to add
                        msg.accept("failed", placeholders);
                    }
                } catch (PluginConfigException e) {
                    e.printStackTrace();
                }
                break;
            }
            default: {
                // unknown sub command was entered
                BiConsumer<String, Map<String, String>> msg = (token, placeholders) -> message.accept(help, token, placeholders);
                msg.accept("unknown", null);
            }
        }
    }
}
