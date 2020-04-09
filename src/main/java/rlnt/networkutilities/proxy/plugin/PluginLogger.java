package rlnt.networkutilities.proxy.plugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;
import rlnt.networkutilities.proxy.utils.General;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class PluginLogger extends Logger {

    public PluginLogger(Plugin plugin) {
        super(plugin.getDescription().getName(), null);
        setParent(plugin.getProxy().getLogger());
    }

    // override log method to translate alternate color codes
    @Override
    public void log(LogRecord record) {
        record.setMessage(ChatColor.translateAlternateColorCodes('&', record.getMessage()) + ChatColor.RESET);
        super.log(record);
    }
}
