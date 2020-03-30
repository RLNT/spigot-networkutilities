package rlnt.networkutilities.proxy.utils;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum Server {
    ;

    /**
     * Will return all server names that are registered
     * in the network.
     *
     * @return all server names from the network
     */
    public static Set<String> getServerNames() {
        Map<String, ServerInfo> servers = ProxyServer.getInstance().getConfig().getServers();
        return new HashSet<>(servers.keySet());
    }

    /**
     * Will return a server object by the name.
     *
     * @param server the name to get the server from
     * @return the server object
     */
    public static ServerInfo getServerByName(String server) {
        return ProxyServer.getInstance().getServerInfo(server);
    }
}
