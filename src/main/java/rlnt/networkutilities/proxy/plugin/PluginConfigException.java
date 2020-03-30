package rlnt.networkutilities.proxy.plugin;

public class PluginConfigException extends Exception {
    private static final long serialVersionUID = 1L;

    public PluginConfigException(String message) {
        super(message);
    }

    public PluginConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
