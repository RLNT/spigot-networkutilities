package rlnt.networkutilities.proxy.utils;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.util.Collection;
import java.util.Map;

public enum Communication {
    ;

    /**
     * Will send a colorized message to a player when
     * the message is not empty.
     *
     * @param player the player to send the message to
     * @param message the message to send
     */
    public static void playerMsg(ProxiedPlayer player, String message) {
        if (message == null || message.isEmpty()) return;
        player.sendMessage(new TextComponent(General.colorize(message)));
    }

    /**
     * Will kick a player with a colorized message.
     * Replaces the message with a placeholder if
     * it is empty: &cYou have been kicked!
     *
     * @param player the player to kick
     * @param message the kick message
     */
    public static void playerKickMsg(ProxiedPlayer player, String message) {
        if (message == null || message.isEmpty()) {
            player.disconnect(new TextComponent(General.colorize("&cYou have been kicked!")));
        } else {
            player.disconnect(new TextComponent(General.colorize(message)));
        }
    }

    /**
     * Will send a colorized message from the config to a
     * player when the message is enabled and not empty.
     *
     * @param player the player to send the message to
     * @param section the configuration section
     * @param type the type of the message
     */
    public static void playerCfgMsg(ProxiedPlayer player, Configuration section, String type) {
        playerCfgMsg(player, section, type, null);
    }

    /**
     * Will send a colorized message from the config to a
     * player when the message is enabled and not empty.
     *
     * @param player the player to send the message to
     * @param section the configuration section
     * @param type the type of the message
     * @param placeholders the placeholders to replace in the message
     */
    public static void playerCfgMsg(ProxiedPlayer player, Configuration section, String type, Map<String, String> placeholders) {
        if (!Config.messageEnabled(section, type)) return;
        if (Config.messageEmpty(section, type)) return;

        if (placeholders == null || placeholders.isEmpty()) {
            playerMsg(player, Config.getMessage(section, type));
        } else {
            String message = Config.getMessage(section, type);
            for (String placeholder : placeholders.keySet()) {
                message = message.replace(placeholder, placeholders.get(placeholder));
            }
            playerMsg(player, message);
        }
    }

    /**
     * Will send a colorized message to a command
     * sender when the message is not empty.
     * Mostly used if the console sent a command.
     *
     * @param sender the command sender to send the message to
     * @param message the message to send
     */
    public static void senderMsg(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) return;
        sender.sendMessage(new TextComponent(General.colorize(message)));
    }

    /**
     * Will send a colorized message from the config to a
     * sender when the message is enabled and not empty.
     *
     * @param sender the command sender to send the message to
     * @param section the configuration section
     * @param type the type of the message
     */
    public static void senderCfgMsg(CommandSender sender, Configuration section, String type) {
        senderCfgMsg(sender, section, type, null);
    }

    /**
     * Will send a colorized message from the config to a
     * sender when the message is enabled and not empty.
     *
     * @param sender the command sender to send the message to
     * @param section the configuration section
     * @param type the type of the message
     * @param placeholders the placeholders to replace in the message
     */
    public static void senderCfgMsg(CommandSender sender, Configuration section, String type, Map<String, String> placeholders) {
        if (!Config.messageEnabled(section, type)) return;
        if (Config.messageEmpty(section, type)) return;

        if (placeholders == null || placeholders.isEmpty()) {
            senderMsg(sender, Config.getMessage(section, type));
        } else {
            String message = Config.getMessage(section, type);
            for (String placeholder : placeholders.keySet()) {
                message = message.replace(placeholder, placeholders.get(placeholder));
            }
            senderMsg(sender, message);
        }
    }

    /**
     * Will send a colorized message to a given group
     * of players if the group and the message are
     * not empty.
     *
     * @param group the group of player to send the message to
     * @param message the message to send
     */
    public static void groupMsg(Collection<ProxiedPlayer> group, String message) {
        if (group.isEmpty()) return;
        if (message == null || message.isEmpty()) return;
        for (ProxiedPlayer player : group) {
            player.sendMessage(new TextComponent(General.colorize(message)));
        }
    }

    /**
     * Will send a colorized message to the whole
     * network if the message is not empty.
     *
     * @param message the message to send
     */
    public static void networkMsg(String message) {
        if (message == null || message.isEmpty()) return;

        Collection<ProxiedPlayer> network = ProxyServer.getInstance().getPlayers();
        for (ProxiedPlayer player : network) {
            player.sendMessage(new TextComponent(General.colorize(message)));
        }
    }
}
