package rlnt.networkutilities.proxy.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import rlnt.networkutilities.proxy.utils.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class HubCmd extends Command {

    // config entries
    private static Configuration options = Config.getOptions().getSection("commands").getSection("hub");
    private Configuration messages = Config.getMessages().getSection("commands").getSection("hub");

    private static String permission = getCommandPermission();
    private String targetServer = Config.getOptions().getString("hubServer");
    private ServerInfo target = Server.getServerByName(targetServer);
    private Set<String> networkServers = Server.getServerNames();

    public HubCmd() {
        super(getCommandName(), permission, getCommandAlias());
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
        String commandName = options.getString("commandName");
        if (commandName == null || commandName.isEmpty()) {
            return "nwutils.command.hub";
        } else {
            return "nwutils.command." + commandName;
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
            return new String[0];
        } else {
            return commandAliases.toArray(new String[0]);
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

        // define the sub command
        String subcommand;
        if (args == null || args.length == 0) {
            subcommand = null;
        } else if (args[0].equals("?")) {
            subcommand = "?";
        } else {
            subcommand = args[0];
        }

        // messages
        Configuration help = messages.getSection("help");

        // sub command logic
        if (subcommand == null) {
            // nothing was entered, teleport to hub requested
            // if player -> teleport to hub
            // if nonplayer -> nonplayer can't be teleported -> show help
            if (isPlayer) {
                // check permission
                if (!Player.hasPermission(player, permission + ".self")) return;

                // messages
                Configuration self = messages.getSection("self");

                // variables
                String currentServer = player.getServer().getInfo().getName();

                // check if player already is in the hub
                if (currentServer.equals(targetServer)) {
                    // player is already on the hub server
                    Communication.playerCfgMsg(player, self, "server");
                    return;
                }

                // connect player to hub
                target.ping((pingResult, pingError) -> {
                    if (pingError == null) {
                        // hub is online, connect player
                        player.connect(target, (connectResult, connectError) -> {
                            if (connectError == null) {
                                // successfully connected
                                Communication.playerCfgMsg(player, self, "success");
                            } else {
                                // connection failed
                                Communication.playerCfgMsg(player, self, "failed");
                            }
                        });
                    } else {
                        // hub is not online
                        Communication.playerCfgMsg(player, self, "offline");
                    }
                });
            } else {
                // nonplayer sent the command, show help
                Communication.senderCfgMsg(sender, help, "console");
            }
        } else if (Player.getPlayerNames().contains(subcommand)) {
            // another player's username was entered, send player to hub
            if (isPlayer && !Player.hasPermission(player, permission + ".other")) return;

            // messages
            Configuration other = messages.getSection("other");
            Configuration toTarget = other.getSection("target");

            // check args
            if (args.length > 1) {
                if (isPlayer) {
                    Communication.playerCfgMsg(player, other, "toomany");
                } else {
                    Communication.senderCfgMsg(sender, other, "toomany");
                }
            }

            // variables
            ProxiedPlayer targetPlayer = Player.getPlayerByName(subcommand);
            String currentServer = targetPlayer.getServer().getInfo().getName();

            // placeholder logic
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("{player}", targetPlayer.getName());
            placeholders.put("{server}", currentServer);

            // check if targeted player is already in the hub
            if (currentServer.equals(targetServer)) {
                // targeted player is already on the hub server
                if (isPlayer) {
                    Communication.playerCfgMsg(player, other, "server", placeholders);
                } else {
                    Communication.senderCfgMsg(sender, other, "server", placeholders);
                }
                return;
            }

            // connect targeted player to hub
            target.ping((pingResult, pingError) -> {
                if (pingError == null) {
                    // hub is online, connect targeted player
                    // target placeholder logic
                    Map<String, String> targetPlaceholders = new HashMap<>(placeholders);
                    targetPlaceholders.put("{player}", targetPlayer.getName());
                    if (isPlayer) {
                        targetPlaceholders.put("{executor}", player.getName());
                    } else {
                        targetPlaceholders.put("{executor}", "console");
                    }

                    targetPlayer.connect(target, (connectResult, connectError) -> {
                        if (connectError == null) {
                            // successfully connected
                            if (isPlayer) {
                                Communication.playerCfgMsg(player, other, "success", placeholders);
                            } else {
                                Communication.senderCfgMsg(sender, other, "success", placeholders);
                            }
                            // send message to targeted player
                            Communication.playerCfgMsg(targetPlayer, toTarget, "success", targetPlaceholders);
                        } else {
                            // connection failed
                            if (isPlayer) {
                                Communication.playerCfgMsg(player, other, "failed", placeholders);
                            } else {
                                Communication.senderCfgMsg(sender, other, "failed", placeholders);
                            }
                            // send message to targeted player
                            Communication.playerCfgMsg(targetPlayer, toTarget, "failed", targetPlaceholders);
                        }
                    }, ServerConnectEvent.Reason.COMMAND);
                } else {
                    // hub is not online
                    if (isPlayer) {
                        Communication.playerCfgMsg(player, other, "offline", placeholders);
                    } else {
                        Communication.senderCfgMsg(sender, other, "offline", placeholders);
                    }
                }
            });
        } else if (networkServers.contains(subcommand)) {
            // a server's name was entered, send server players to hub
            if (isPlayer && !Player.hasPermission(player, permission + ".server")) return;

            // messages
            Configuration server = messages.getSection("server");
            Configuration toTarget = server.getSection("target");

            // check args
            if (args.length > 1) {
                if (isPlayer) {
                    Communication.playerCfgMsg(player, server, "toomany");
                } else {
                    Communication.senderCfgMsg(sender, server, "toomany");
                }
            }

            // check if origin is equal to target
            if (subcommand.equals(targetServer)) {
                // entered hub as server
                if (isPlayer) {
                    Communication.playerCfgMsg(player, server, "server");
                } else {
                    Communication.senderCfgMsg(sender, server, "server");
                }
                return;
            }

            // variables
            Collection<ProxiedPlayer> players = Player.getPlayersByServer(subcommand);
            ServerInfo currentServer = Server.getServerByName(subcommand);

            // placeholder logic
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("{server}", currentServer.getName());

            // check if the origin server is online
            AtomicBoolean originOnline = new AtomicBoolean(false);
            currentServer.ping((result, error) -> {
                if (error == null) {
                    originOnline.set(true);
                } else {
                    originOnline.set(false);
                }
            });
            if (!originOnline.get()) {
                if (isPlayer) {
                    Communication.playerCfgMsg(player, server, "originOffline", placeholders);
                } else {
                    Communication.senderCfgMsg(sender, server, "originOffline", placeholders);
                }
                return;
            }

            // check if the origin server has players
            if (players.isEmpty()) {
                if (isPlayer) {
                    Communication.playerCfgMsg(player, server, "empty", placeholders);
                } else {
                    Communication.senderCfgMsg(sender, server, "empty", placeholders);
                }
                return;
            }

            // connect server players to hub
            target.ping((pingResult, pingError) -> {
                if (pingError == null) {
                    // hub is online, connect players
                    AtomicInteger connectSuccess = new AtomicInteger();
                    AtomicInteger connectFailed = new AtomicInteger();

                    // target placeholder logic
                    Map<String, String> targetPlaceholders = new HashMap<>(placeholders);
                    if (isPlayer) {
                        targetPlaceholders.put("{executor}", player.getName());
                    } else {
                        targetPlaceholders.put("{executor}", "console");
                    }

                    // connect players
                    for (ProxiedPlayer p : players) {
                        // add target placeholder
                        targetPlaceholders.put("{player}", p.getName());
                        p.connect(target, (connectResult, connectError) -> {
                            if (connectError == null) {
                                // successfully connected
                                connectSuccess.getAndIncrement();
                                Communication.playerCfgMsg(p, toTarget, "success", targetPlaceholders);
                            } else {
                                connectFailed.getAndIncrement();
                                Communication.playerCfgMsg(p, toTarget, "failed", targetPlaceholders);
                            }
                        });
                    }

                    // add placeholders
                    placeholders.put("{success}", connectSuccess.toString());
                    placeholders.put("{failed}", connectFailed.toString());

                    // message player about result
                    if (connectFailed.intValue() == 0) {
                        // all connects were successful
                        if (isPlayer) {
                            Communication.playerCfgMsg(player, server, "success", placeholders);
                        } else {
                            Communication.senderCfgMsg(sender, server, "success", placeholders);
                        }
                    } else if (connectSuccess.intValue() == 0) {
                        // all connects failed
                        if (isPlayer) {
                            Communication.playerCfgMsg(player, server, "failed", placeholders);
                        } else {
                            Communication.senderCfgMsg(sender, server, "failed", placeholders);
                        }
                    } else {
                        // partial transfer
                        if (isPlayer) {
                            Communication.playerCfgMsg(player, server, "partial", placeholders);
                        } else {
                            Communication.senderCfgMsg(sender, server, "partial", placeholders);
                        }
                    }
                } else {
                    // hub is not online
                    if (isPlayer) {
                        Communication.playerCfgMsg(player, server, "hubOffline", placeholders);
                    } else {
                        Communication.senderCfgMsg(sender, server, "hubOffline", placeholders);
                    }
                }
            });
        } else if (subcommand.equals("all") || subcommand.equals("network")) {
            // network was entered, send network players to hub
            if (isPlayer && !Player.hasPermission(player, permission + ".network")) return;

            // messages
            Configuration network = messages.getSection("network");
            Configuration toTarget = network.getSection("target");

            // check args
            if (args.length > 1) {
                if (isPlayer) {
                    Communication.playerCfgMsg(player, network, "toomany");
                } else {
                    Communication.senderCfgMsg(sender, network, "toomany");
                }
            }

            // variables
            Collection<ProxiedPlayer> players = Player.getNetworkPlayers();
            players.removeAll(Player.getPlayersByServer(targetServer));

            // check if network has players
            if (players.size() <= 1) {
                // no player or only the sender was found in the network
                if (isPlayer) {
                    Communication.playerCfgMsg(player, network, "empty");
                } else {
                    Communication.senderCfgMsg(sender, network, "empty");
                }
            }

            // send players to hub
            target.ping((pingResult, pingError) -> {
                if (pingError == null) {
                    // hub is online
                    AtomicInteger connectSuccess = new AtomicInteger();
                    AtomicInteger connectFailed = new AtomicInteger();

                    // target placeholder logic
                    Map<String, String> targetPlaceholders = new HashMap<>();
                    if (isPlayer) {
                        targetPlaceholders.put("{executor}", player.getName());
                    } else {
                        targetPlaceholders.put("{executor}", "console");
                    }

                    // connect target players
                    for (ProxiedPlayer p : players) {
                        // add target placeholders
                        targetPlaceholders.put("{player}", p.getName());
                        targetPlaceholders.put("{server}", p.getServer().getInfo().getName());
                        p.connect(target, (connectResult, connectError) -> {
                            if (connectError == null) {
                                // successfully connected
                                connectSuccess.getAndIncrement();
                                Communication.playerCfgMsg(p, toTarget, "success", targetPlaceholders);
                            } else {
                                connectFailed.getAndIncrement();
                                Communication.playerCfgMsg(p, toTarget, "failed", targetPlaceholders);
                            }
                        });
                    }

                    // message player about result
                    if (connectFailed.intValue() == 0) {
                        // all connects were successful
                        if (isPlayer) {
                            Communication.playerCfgMsg(player, network, "success");
                        } else {
                            Communication.senderCfgMsg(sender, network, "success");
                        }
                    } else if (connectSuccess.intValue() == 0) {
                        // all connects failed
                        if (isPlayer) {
                            Communication.playerCfgMsg(player, network, "failed");
                        } else {
                            Communication.senderCfgMsg(sender, network, "failed");
                        }
                    } else {
                        // partial transfer
                        if (isPlayer) {
                            Communication.playerCfgMsg(player, network, "partial");
                        } else {
                            Communication.senderCfgMsg(sender, network, "partial");
                        }
                    }
                } else {
                    // hub is not online
                    if (isPlayer) {
                        Communication.playerCfgMsg(player, network, "offline");
                    } else {
                        Communication.senderCfgMsg(sender, network, "offline");
                    }
                }
            });
        } else {
            // '?' or an unregistered sub command was entered, show help
            if (isPlayer) {
                Communication.playerCfgMsg(player, help, "player");
            } else {
                Communication.senderCfgMsg(sender, help, "console");
            }
        }
    }
}
