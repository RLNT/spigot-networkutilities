package rlnt.networkutilities.proxy.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public enum Whitelist {
    ;

    // hashset to store the uuid based network whitelist
    private static Set<String> whitelist = new HashSet<>();

    /**
     * Will return the current whitelist.
     *
     * @return the network whitelist
     */
    public static Set<String> getWhitelist() {
        return whitelist;
    }

    /**
     * Will overwrite the current whitelist.
     *
     * @param newWhitelist the new whitelist
     */
    public static void setWhitelist(List<String> newWhitelist) {
        whitelist.addAll(newWhitelist);
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
    public static boolean addWhitelist(UUID uuid) {
        // TODO: save the whitelist to file after adding
        return whitelist.add(uuid.toString());
    }

    /**
     * Will remove a player's UUID from the whitelist.
     *
     * @param uuid the player's UUID
     * @return boolean if the UUID was removed
     */
    public static boolean removeWhitelist(UUID uuid) {
        // TODO: save the whitelist to file after removing
        return whitelist.remove(uuid.toString());
    }
}
