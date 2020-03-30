package rlnt.networkutilities.proxy.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import rlnt.networkutilities.proxy.utils.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HubCmd extends Command {

    // config entries
    private static Configuration options = Config.getOptions().getSection("commands").getSection("hub");
    private static Configuration messages = Config.getMessages().getSection("commands").getSection("hub");

    private static String permission = getCommandPermission();
    private String targetServer = options.getString("targetServer");
    private ServerInfo target = Server.getServerByName(targetServer);

    public HubCmd() {
        super(getCommandName(), permission, getCommandAlias());
    }

    // TODO: rewrite the Hub command to the console can use it as well and player with networkutilities.command.hub_command.other
    // TODO: can teleport other players to the hub
    // TODO: rewrite so parameter all can be used to teleport everyone
    // TODO: rewrite so parameter serer can be used to teleport a server to the hub

    // TODO: implementieren dass die teleportieren Spieler auch Nachrichten erhalten

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
        if (args == null || args.length == 0) {
            subcommand = null;
        } else if (args[0].equals("?") || args[0].equals("help")) {
            subcommand = "help";
        } else {
            subcommand = args[0];
        }

        Configuration helpText = messages.getSection("helpText");
        Configuration hubAlready = messages.getSection("hubAlready");
        Configuration hubOffline = messages.getSection("hubOffline");
        Configuration hubServer = messages.getSection("server");
        Configuration hubSuccess = messages.getSection("success");
        Configuration hubFailed = messages.getSection("fail");

        // sub command logic
        if (subcommand == null) {
            // nothing was entered, teleport to hub requested
            // if player -> teleport to hub
            // if nonplayer -> show help text
            if (isPlayer) {
                // a player entered the command and should be teleported
                if (Player.hasPermission(player, permission + ".self")) {
                    // player has permission to use the command
                    String currentServer = player.getServer().getInfo().getName();
                    if (currentServer.equals(targetServer)) {
                        // player is already on the hub server
                        Communication.playerCfgMsg(player, hubAlready, "player", null);
                    } else {
                        // player is sent to hub
                        // check if the target server is online
                        target.ping((pingResult, pingError) -> {
                            if (pingError == null) {
                                // hub is online, connect player
                                player.connect(target, (connectResult, connectError) -> {
                                    if (connectError == null) {
                                        // successfully connected
                                        Communication.playerCfgMsg(player, hubSuccess, "player", null);
                                    } else {
                                        // connection failed
                                        Communication.playerCfgMsg(player, hubFailed, "player", null);
                                    }
                                });
                            } else {
                                // hub is not online
                                Communication.playerCfgMsg(player, hubOffline, "player", null);
                            }
                        });
                    }
                }
            } else {
                // a nonplayer entered the command e.g. console
                // send help text
                Communication.senderCfgMsg(sender, helpText, "nonplayer", null);
            }
        } else if (args.length > 1) {
            // undefined amount of parameters was entered, display help
            if (isPlayer) {
                // player entered the command
                Communication.playerCfgMsg(player, helpText, "player", null);
            } else {
                // nonplayer entered the command
                Communication.senderCfgMsg(sender, helpText, "nonplayer", null);
            }
        } else if (Player.getPlayerNames().contains(subcommand)) {
            // another player's username was entered, send player to hub
            if (isPlayer) {
                // check permission if command sender was a player
                if (!Player.hasPermission(player, permission + ".other")) return;
            }

            ProxiedPlayer targetPlayer = Player.getPlayerByName(subcommand);
            String currentServer = targetPlayer.getServer().getInfo().getName();
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("{player}", targetPlayer.getName());
            if (currentServer.equals(targetServer)) {
                // target player is already on the hub server
                if (isPlayer) {
                    Communication.playerCfgMsg(player, hubAlready, "other", placeholders);
                } else {
                    Communication.senderCfgMsg(sender, hubAlready, "nonplayer", placeholders);
                }
            } else {
                // target player is sent to hub
                // check if the target server is online
                target.ping((pingResult, pingError) -> {
                    if (pingError == null) {
                        // hub is online, connect target player
                        targetPlayer.connect(target, (connectResult, connectError) -> {
                            if (connectError == null) {
                                // successfully connected
                                if (isPlayer) {
                                    Communication.playerCfgMsg(player, hubSuccess, "other", placeholders);
                                } else {
                                    Communication.senderCfgMsg(sender, hubSuccess, "nonplayer", placeholders);
                                }
                            } else {
                                // connection failed
                                if (isPlayer) {
                                    Communication.playerCfgMsg(player, hubFailed, "other", placeholders);
                                } else {
                                    Communication.senderCfgMsg(sender, hubFailed, "nonplayer", placeholders);
                                }
                            }
                        });
                    } else {
                        // hub is not online
                        if (isPlayer) {
                            Communication.playerCfgMsg(player, hubOffline, "other", placeholders);
                        } else {
                            Communication.senderCfgMsg(sender, hubOffline, "nonplayer", placeholders);
                        }
                    }
                });
            }
        } else if (Server.getServerNames().contains(subcommand)) {
            // a server's name was entered, send players to hub
            if (isPlayer) {
                // check permission if command sender was a player
                if (!Player.hasPermission(player, permission + ".server")) return;
            }

            if (subcommand.equals(targetServer)) {
                // entered hub as server
                if (isPlayer) {
                    Communication.playerCfgMsg(player, hubAlready, "server", null);
                } else {
                    Communication.senderCfgMsg(sender, hubAlready, "server", null);
                }
                return;
            }

            Collection<ProxiedPlayer> players = Player.getPlayersByServer(subcommand);
            ServerInfo server = Server.getServerByName(subcommand);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("{server}", server.getName());

            // check if the target server is online
            target.ping((pingResult, pingError) -> {
                if (pingError == null) {
                    // hub is online
                    // check if the origin server has players
                    if (players.isEmpty()) {
                        // server is empty
                        if (isPlayer) {
                            Communication.playerCfgMsg(player, hubServer, "empty", placeholders);
                        } else {
                            Communication.senderCfgMsg(sender, hubServer, "empty", placeholders);
                        }
                        return;
                    }

                    // connect target players
                    AtomicInteger connectSuccess = new AtomicInteger();
                    AtomicInteger connectFailed = new AtomicInteger();
                    for (ProxiedPlayer p : players) {
                        p.connect(target, (connectResult, connectError) -> {
                            if (connectError == null) {
                                // successfully connected
                                connectSuccess.getAndIncrement();
                                // TODO: send message to targeted player that he was moved to hub
                            } else {
                                connectFailed.getAndIncrement();
                                // TODO: send message to targeted playeer that moving to hub failed
                            }
                        });
                    }

                    // message player about result
                    if (connectFailed.intValue() == 0) {
                        // all connects were successful
                        if (isPlayer) {
                            Communication.playerCfgMsg(player, hubSuccess, "server", placeholders);
                        } else {
                            Communication.senderCfgMsg(sender, hubSuccess, "server", placeholders);
                        }
                    } else if (connectSuccess.intValue() == 0) {
                        // all connects failed
                        if (isPlayer) {
                            Communication.playerCfgMsg(player, hubFailed, "server", placeholders);
                        } else {
                            Communication.senderCfgMsg(sender, hubFailed, "server", placeholders);
                        }
                    } else {
                        // partial transfer
                        if (isPlayer) {
                            Communication.playerCfgMsg(player, hubServer, "partial", placeholders);
                        } else {
                            Communication.senderCfgMsg(sender, hubServer, "partial", placeholders);
                        }
                    }
                } else {
                    // hub is not online
                    if (isPlayer) {
                        Communication.playerCfgMsg(player, hubOffline, "server", placeholders);
                    } else {
                        Communication.senderCfgMsg(sender, hubOffline, "server", placeholders);
                    }
                }
            });
        } else if (subcommand.equals("all") || subcommand.equals("network")) {
            // network was entered, send network to hub
            // TODO: code it
        } else {
            // help, ? or an undefined sub command was entered, show help
            // if player -> show player help text
            // if nonplayer -> show nonplayer help text
            if (isPlayer) {
                // player entered the command
                Communication.playerCfgMsg(player, helpText, "player", null);
            } else {
                // nonplayer entered the command
                Communication.senderCfgMsg(sender, helpText, "nonplayer", null);
            }
        }
    }

    /**
     * Will return the command name which lets the player use the command
     * by the String set in the config.
     *
     * @return the command name
     */
    private static String getCommandName() {
        String commandName = options.getString("commandName");
        if (commandName == null || commandName.isEmpty()) {
            return "hub";
        } else {
            return commandName;
        }
    }

    /**
     * Will return the command permission which the player needs to use the
     * command. The permission will be changed by the command name.
     *
     * @return the command permission or null if disabled
     */
    private static String getCommandPermission() {
        if (!options.getBoolean("permissionRequired", true)) return null;

        String commandName = options.getString("commandName");
        if (commandName == null || commandName.isEmpty()) {
            return "networkutilities.command.hub";
        } else {
            return "networkutilities.command." + commandName;
        }
    }

    /**
     * Will return the command alias which has the same execution
     * functionality like the command name.
     *
     * @return the command alias
     */
    private static String[] getCommandAlias() {
        List<String> commandAliases = options.getStringList("commandAliases");
        if (commandAliases.isEmpty()) {
            return null;
        } else {
            return commandAliases.toArray(new String[0]);

        }
    }
}
