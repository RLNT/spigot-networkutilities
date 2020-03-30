package rlnt.networkutilities.spigot.utils;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class PluginLogger extends Logger {

    public PluginLogger(JavaPlugin plugin) {
        super(plugin.getDescription().getName(), null);
        setParent(plugin.getServer().getLogger());
    }

    // override log method to translate alternate color codes
    @Override
    public void log(LogRecord record) {
        record.setMessage(Utils.colorize(record.getMessage()) + ChatColor.RESET);
        super.log(record);
    }
}
