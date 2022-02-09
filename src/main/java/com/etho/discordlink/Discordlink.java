package com.etho.discordlink;

import com.etho.discordlink.command.VerifyCommand;
import com.etho.discordlink.listener.ChatListener;
import com.etho.discordlink.utils.Config;
import com.etho.discordlink.utils.DependencyChecker;
import com.etho.discordlink.utils.discord.DiscordConnection;
import com.etho.discordlink.utils.discord.RoleSynchronization;
import com.etho.discordlink.utils.sql.JDBC;
import com.etho.discordlink.utils.sql.SqlConnection;
import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.luckperms.api.LuckPermsProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "discordlink",
        name = "Discordlink",
        version = "1.0",
        description = "Link Minecraft and Discord accounts together.",
        authors = {"etho"}
)
public class Discordlink {
    private List<VerifyCode> codes = new ArrayList<>();

    private Logger logger;
    private ProxyServer server;
    private File datafolder;
    private boolean luckperms;
    private ScheduledTask expireTaskId;
    private ScheduledTask roleSyncTaskId;
    static Discordlink instance;

    @Inject
    public Discordlink(ProxyServer server, Logger logger) {
        this.logger = logger;
        this.server = server;
        instance = this;
        this.datafolder = getDataFolder();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        if (DependencyChecker.luckperms()) {
            logger.info("LuckPerms is installed! " + LuckPermsProvider.get().getPluginMetadata().getVersion());
            luckperms = true;
        } else {
            logger.info("LuckPerms Not installed, some features will be unavailable.");
        }
        Config.init();
        SqlConnection.init();
        new DiscordConnection().init();
        server.getEventManager().register(this, new ChatListener());
        server.getCommandManager().register("verify", new VerifyCommand());
        repeating();
    }

    public void repeating() {
        if (expireTaskId != null) expireTaskId.cancel();
        if (roleSyncTaskId != null) roleSyncTaskId.cancel();
        expireTaskId = server.getScheduler().buildTask(this, () -> {
            for (VerifyCode c : new ArrayList<>(codes)) {
                c.task();
                if (c.isInvalid()) {
                    codes.remove(c);
                }
            }
        }).repeat(Config.DEFAULT.getInt("code-expire"), TimeUnit.SECONDS).schedule();

        if (Config.DEFAULT.getBoolean("role-sync.enable")
                && Config.DEFAULT.getInt("role-sync.rank-sync-auto") > 0) {
            logger.info("Role sync initializing with period of " + Config.DEFAULT.getInt("role-sync.rank-sync-auto"));
            roleSyncTaskId = server.getScheduler().buildTask(this, RoleSynchronization::update)
                    .repeat(Config.DEFAULT.getInt("role-sync.rank-sync-auto"), TimeUnit.SECONDS).schedule();
        } else {
            logger.info("Role sync disabled!");
        }
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent e) {
        try {
            SqlConnection.get().close();
            DiscordConnection.get().close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static List<VerifyCode> getCodes() {
        return instance.codes;
    }

    public static void addCode(String playerName, String code, Player p) {
        instance.codes.add(new VerifyCode(playerName, code, p));
    }

    public static void remove(VerifyCode code) {
        instance.codes.remove(code);
    }

    public static void remove(String playerName) {
        for (VerifyCode code : getCodes()) {
            if (code.getPlayerName().equalsIgnoreCase(playerName)) {
                instance.codes.remove(code);
                return;
            }
        }
    }

    public static Discordlink get() {
        return instance;
    }

    public static File getDataFolder() {
        File dataFolder = instance.datafolder;
        if (dataFolder == null) {
            String path = "plugins/discordlink/";
            try {
                dataFolder = new File(path);
                dataFolder.mkdir();
                return dataFolder;
            } catch (Exception e) {
                return null;
            }
        } else {
            return dataFolder;
        }
    }

    public void saveResource(@NotNull String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath);
        System.out.println(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found");
        }

        File outFile = new File(instance.datafolder, resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(instance.datafolder, resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } else {

            }
        } catch (IOException ex) {

        }
    }

    public InputStream getResource(@NotNull String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }

        try {
            URL url = getClass().getResource(filename);
            if (url == null) {
                return null;
            }

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }

    public static Logger logger() {
        return instance.logger;
    }

    public static ProxyServer server() {
        return instance.server;
    }
}
