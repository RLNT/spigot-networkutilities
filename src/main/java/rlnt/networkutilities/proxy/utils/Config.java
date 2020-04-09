package rlnt.networkutilities.proxy.utils;

import net.md_5.bungee.config.Configuration;
import rlnt.networkutilities.proxy.NetworkUtilities;
import rlnt.networkutilities.proxy.plugin.PluginConfigException;

public enum Config {
    ;

    // store the instance so all functions can use it
    private static NetworkUtilities instance;

    /**
     * Will set the instance when it's available.
     *
     * @param instance the plugin instance
     */
    public static void setInstance(NetworkUtilities instance) {
        Config.instance = instance;
    }

    /**
     * Will return the current options as a
     * Configuration.
     *
     * @return the options configuration
     */
    public static Configuration getOptions() {
        return instance.getOptions().getConfig();
    }

    /**
     * Will return the current messages as a
     * Configuration.
     *
     * @return the messages configuration
     */
    public static Configuration getMessages() {
        return instance.getMessages().getConfig();
    }

    /**
     * Will reload all configs.
     *
     * @throws PluginConfigException when something in the
     * config loading process goes wrong
     */
    public static void reloadConfig() throws PluginConfigException {
        instance.getOptions().reload();
        instance.getMessages().reload();
        instance.getWhitelist().reload();
    }

    /**
     * Will return the status of the enabled key in a
     * specific configuration section.
     *
     * @param section the section of the config
     * @param type the type of the section
     * @return boolean if the section is enabled
     */
    public static boolean messageDisabled(Configuration section, String type) {
        return !section.getSection(type).getBoolean("enabled", false);
    }

    /**
     * Will return if the message of a specific
     * configuration section is empty.
     *
     * @param section the section of the config
     * @param type the type of the section
     * @return boolean if the message is empty
     */
    public static boolean messageEmpty(Configuration section, String type) {
        return getMessage(section, type).isEmpty();
    }

    /**
     * Will return the message of a specific
     * configuration section.
     *
     * @param section the section of the config
     * @param type the type of the section
     * @return the message
     */
    public static String getMessage(Configuration section, String type) {
        return section.getSection(type).getString("message");
    }
}
