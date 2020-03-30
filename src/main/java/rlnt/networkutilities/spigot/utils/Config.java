package rlnt.networkutilities.spigot.utils;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import rlnt.networkutilities.spigot.NetworkUtilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class Config {

    private YamlConfiguration config;

    public Config(String name, JavaPlugin plugin) {

        PluginLogger logger = NetworkUtilities.getInstance().getLogger();

        File folder = plugin.getDataFolder();
        File file = new File(folder, name);
        config = new YamlConfiguration();

        // check if the config folder already exists
        if (!folder.exists()) {
            logger.info("&6  > &ePlugin folder wasn't created yet! &5Creating...");

            // check if config folder creation is successful
            if (!file.getParentFile().mkdir()) {
                logger.severe("&c  > &4Plugin folder couldn't be created! &5Disabling the plugin...");
                throw new Error("Plugin folder couldn't be created!");
            }
        }

        // check if the config file already exists
        if (!file.exists()) {
            logger.info("&6  > &c" + name + " &ewasn't found! &5Extracting...");

            try {
                Files.copy(Objects.requireNonNull(plugin.getResource("spigot/" + name)), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.severe("&c  > &e" + name + " &4couldn't be extracted! &5Disabling the plugin...");
                throw new Error(e);
            }
        }

        // load the config
        try {
            config.load(file);
        } catch (InvalidConfigurationException | IOException e) {
            logger.severe("&c  > &e" + name + " &4couldn't be loaded! &5Disabling the plugin...");
            throw new Error(e);
        }
    }

    public YamlConfiguration getConfig() {
        return config;
    }
}
