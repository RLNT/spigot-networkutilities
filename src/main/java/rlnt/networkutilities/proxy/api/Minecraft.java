package rlnt.networkutilities.proxy.api;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public enum Minecraft {
    ;

    private static final int cacheLifetime = 30;
    private static final TimeUnit cacheLifetimeUnit = TimeUnit.MINUTES;
    private static final int cacheSize = 1000;
    private static final int timeout = 300;

    private static final Cache<String, UUID> usernameUuidCache = CacheBuilder.newBuilder()
            .maximumSize(cacheSize)
            .expireAfterAccess(cacheLifetime, cacheLifetimeUnit)
            .build();
    private static final Cache<UUID, String> uuidUsernameCache = CacheBuilder.newBuilder()
            .maximumSize(cacheSize)
            .expireAfterAccess(cacheLifetime, cacheLifetimeUnit)
            .build();

    static class Profile {
        public String error;
        public String errorMessage;
        public String id;
        public String name;
    }

    /**
     * Will convert a Java UUID to an ID the Mojang
     * API can parse.
     *
     * @param uuid the player UUID
     * @return the parsable ID
     */
    private static String getIdFromUuid(UUID uuid) {
        return uuid.toString().replace("-", "");
    }

    /**
     * Will convert an ID the Mojang API can parse
     * to a Java UUID.
     *
     * @param id a parsable ID
     * @return the UUID
     */
    private static UUID getUuidFromId(String id) {
        BigInteger bigInteger = new BigInteger(id, 16);
        return new UUID(bigInteger.shiftRight(64).longValue(), bigInteger.longValue());
    }

    /**
     * Performs a GET request for a {@link URL}.
     *
     * @param url the url for the request
     * @return the body parsed as {@link JsonObject}
     */
    private static Profile httpGet(URL url) throws ApiException {
        // open new connection
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new ApiException("Opening connection", e);
        }

        // set request method
        try {
            conn.setRequestMethod("GET");
        } catch (ProtocolException e) {
            throw new ApiException("Setting request method", e);
        }

        // set timeouts
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);

        // check status
        try {
            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new ApiException("Server returned: " + status);
            }
        } catch (IOException e) {
            throw new ApiException("Checking return status", e);
        }

        Profile profile;

        try {
            profile = new Gson().fromJson(new InputStreamReader(conn.getInputStream()), Profile.class);
        } catch (IOException e) {
            throw new ApiException("Reading input stream", e);
        }

        // check if API returned an error
        if (profile.error != null) {
            throw new ApiException("Get minecraft API: " + profile.error + ": " + profile.errorMessage);
        }

        return profile;
    }

    /**
     * Get the username of a player by UUID.
     *
     * @param uuid {@link UUID} of the player
     * @return the player name as {@link String} or null if UUID was not found
     */
    public static String getUsername(UUID uuid) throws ApiException {
        // check cache
        String username = uuidUsernameCache.getIfPresent(uuid);
        if (username != null) return username;

        // not in cache
        String id = getIdFromUuid(uuid);

        URL url;
        try {
            url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + id);
        } catch (MalformedURLException e) {
            throw new ApiException("Create new URL", e);
        }

        Profile profile;
        try {
            profile = httpGet(url);
        } catch (Error e) {
            throw new ApiException("Get user profile", e);
        }

        username = profile.name;

        // update caches
        usernameUuidCache.put(username, uuid);
        uuidUsernameCache.put(uuid, username);

        return username;
    }

    public static UUID getUuid(String username) throws ApiException {
        // check cache
        UUID uuid = usernameUuidCache.getIfPresent(username);
        if (uuid != null) return uuid;

        URL url;
        try {
            url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
        } catch (MalformedURLException e) {
            throw new ApiException("Create new URL", e);
        }

        Profile profile;
        try {
            profile = httpGet(url);
        } catch (Error e) {
            throw new ApiException("Get user profile", e);
        }

        uuid = getUuidFromId(profile.id);

        // update caches
        uuidUsernameCache.put(uuid, username);
        usernameUuidCache.put(username, uuid);

        return uuid;
    }
}
