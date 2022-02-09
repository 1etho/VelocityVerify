package com.etho.discordlink.utils;

import com.etho.discordlink.Discordlink;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Config {
    public static YamlConfiguration DEFAULT;
    public static String DEFAULT_PATH = Discordlink.getDataFolder() + "/config.yml";
    public static YamlConfiguration MESSAGES;
    public static String MESSAGES_PATH = Discordlink.getDataFolder() + "/messages.yml";

    public static void init() {
        DEFAULT = get(DEFAULT_PATH, "config.yml");
        MESSAGES = get(MESSAGES_PATH, "messages.yml");
    }

    public static void save() throws IOException {

    }

    public static YamlConfiguration get(String path) {
        return get(path, null);
    }

    public static YamlConfiguration get(String path, String def) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                if (def != null) {
                    Discordlink.get().saveResource(def, false);
                } else {
                    file.createNewFile();
                }
                Discordlink.logger().info("Created " + path);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        return yml;
    }
}
