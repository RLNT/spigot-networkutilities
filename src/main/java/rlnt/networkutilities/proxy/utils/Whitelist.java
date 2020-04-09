package rlnt.networkutilities.proxy.utils;

import rlnt.networkutilities.proxy.plugin.PluginConfig;
import rlnt.networkutilities.proxy.plugin.PluginConfigException;

import java.util.*;

public enum Whitelist {
    ;

    // hashset to store the uuid based network whitelist
    private static Set<String> whitelist = new HashSet<>();
    private static PluginConfig config;

    /**
     * Save the whitelist
     */
    public static void save() throws PluginConfigException {
        config.getConfig().set("whitelist", whitelist);
        config.save();
    }

    /**
     * Load the whitelist from a {@link PluginConfig}.
     * This overrides the current config but no whitelist entries.
     *
     * @param config the config to load
     */
    public static void load(PluginConfig config) {
        Whitelist.config = config;
        load();
    }

    /**
     * Load the whitelist.
     */
    public static void load() {
        List<String> list = config.getConfig().getStringList("whitelist");
        list.removeIf(Objects::isNull);
        if (!list.isEmpty()) whitelist.addAll(list);
    }

    /**
     * Will return the current whitelist.
     *
     * @return the network whitelist
     */
    public static Set<String> getWhitelist() {
        return whitelist;
    }

    /**
     * Will return if a player is whitelisted.
     *
     * @param uuid the player's UUID
     * @return boolean if the player is whitelisted
     */
    public static boolean isWhitelisted(UUID uuid) {
        return whitelist.contains(uuid.toString());
    }

    /**
     * Will add a player's UUID to the whitelist.
     *
     * @param uuid the player's UUID
     * @return boolean if the UUID was added
     */
    public static boolean add(UUID uuid) throws PluginConfigException {
        if (whitelist.add(uuid.toString())) {
            save();
            return true;
        }
        return false;
    }

    /**
     * Will remove a player's UUID from the whitelist.
     *
     * @param uuid the player's UUID
     * @return boolean if the UUID was removed
     */
    public static boolean remove(UUID uuid) throws PluginConfigException {
        if (whitelist.remove(uuid.toString())) {
            save();
            return true;
        }
        return false;
    }
}
