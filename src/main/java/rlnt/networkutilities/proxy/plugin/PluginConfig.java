package rlnt.networkutilities.proxy.plugin;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import rlnt.networkutilities.proxy.NetworkUtilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class PluginConfig {

    private PluginLogger logger;

    private Configuration config;
    private File folder;
    private File file;
    private String name;
    private String version;
    private int keys;

    public PluginConfig(String name) throws PluginConfigException {
        new PluginConfig(name, null, 0);
    }

    public PluginConfig(String name, String version, int keys) throws PluginConfigException {
        logger = NetworkUtilities.getInstance().getLogger();
        this.name = name;

        folder = NetworkUtilities.getInstance().getDataFolder();
        file = new File(folder, name);

        this.version = version;
        this.keys = keys;

        load();
    }

    public Configuration getConfig() {
        return config;
    }

    public void reload() throws PluginConfigException {
        load();
    }

    private void load() throws PluginConfigException {
        // check if the config folder already exists
        if (!folder.exists()) {
            logger.info("&6  > &ePlugin folder wasn't created yet! &5Creating...");

            // check if config folder creation is successful
            if (!file.getParentFile().mkdir()) {
                logger.severe("&c  > &4Plugin folder couldn't be created! &5Disabling the plugin...");
                throw new PluginConfigException("Plugin folder couldn't be created!");
            }
        }

        // check if the config file already exists
        if (!file.exists()) {
            logger.info("&6  > &c" + name + " &ewasn't found! &5Extracting...");

            try {
                Files.copy(NetworkUtilities.getInstance().getResourceAsStream("bungee/" + name), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.severe("&c  > &e" + name + " &4couldn't be extracted! &5Disabling the plugin...");
                throw new PluginConfigException("Extraction from JAR failed: ", e);
            }
        }

        // load the config
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch(IOException e) {
            logger.severe("&c  > &e" + name + " &4couldn't be loaded! &5Disabling the plugin...");
            throw new PluginConfigException("Loading config" + name, e);
        }

        // check if config is valid
        if (config.getKeys().isEmpty()) {
            logger.severe("&c  > &eYour &c" + name + " &eis empty!");
            logger.info("&6  > &fMake sure the file is up to date.");
            throw new PluginConfigException("Config is empty");
        }

        if (keys > 0 && config.getKeys().size() != keys) {
            logger.severe("&c  > &eYour &c" + name + " &eseems to have an invalid amount of keys!");
            logger.info("&6  > &fMake sure your file is up to date and complete.");
            throw new PluginConfigException("Config has wrong amount of keys");
        }

        if (version != null && !Objects.equals(config.getString("configVersion"), version)) {
            logger.severe("&c  > &eYour &c" + name + " &eseems to be outdated!");
            logger.info("&6  > &fMake sure your file is up to date.");
            throw new PluginConfigException("Config is outdated or invalid");
        }
    }
}
