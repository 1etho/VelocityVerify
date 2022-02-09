package com.etho.discordlink.utils;

import com.etho.discordlink.Discordlink;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.platform.PlayerAdapter;
import org.simpleyaml.configuration.file.FileConfiguration;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chat {
    private static LegacyComponentSerializer serializer = LegacyComponentSerializer.builder().character('&').hexCharacter('#').hexColors().build();
    public static TextComponent color(String s, Player p) {
        s = format(s, p);
        return serializer.deserialize(s);
    }
    public static TextComponent color(String s) {
        return serializer.deserialize(s);
    }
    public static LegacyComponentSerializer getSerializer() {
        return serializer;
    }

    public static String format(String s, Player p) {
        LuckPerms lp = LuckPermsProvider.get();
        PlayerAdapter<Player> playerAdapter = lp.getPlayerAdapter(Player.class);
        CachedMetaData data = playerAdapter.getUser(p).getCachedData().getMetaData();
        String prefix = data.getPrefix();
        String suffix = data.getSuffix();
        String prefixes = String.join(" ", data.getPrefixes().values());
        String suffixes = String.join(" ", data.getSuffixes().values());
        s = s.replace("%prefix%", prefix == null ? "" : prefix)
                .replace("%suffix%", suffix == null ? "" : suffix)
                .replace("%prefixes%", prefixes.length() == 0 ? "" : prefixes)
                .replace("%suffixes%", suffixes.length() == 0 ? "" : suffixes)
                .replace("%username%", p.getUsername());
        return s;
    }

    private static final char ANSI_ESC_CHAR = '\u001B';
    private static final String RGB_STRING = ANSI_ESC_CHAR + "[38;2;%d;%d;%dm";
    private static final Pattern RBG_TRANSLATE = Pattern.compile("&#([A-F0-9]){6}", Pattern.CASE_INSENSITIVE);
    public static String convertConsole(String input) {
        Matcher matcher = RBG_TRANSLATE.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String s = matcher.group().replace("&", "").replace('x', '#');
            Color color = Color.decode(s);
            int red = color.getRed();
            int blue = color.getBlue();
            int green = color.getGreen();
            String replacement = String.format(RGB_STRING, red, green, blue);
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return buffer.toString().replace("&l", "")
                .replace("&o", "")
                .replace("&n", "")
                .replace("&m", "")
                + "\033[0m";
    }

    private static final Pattern pattern = Pattern.compile("&#[a-f0-9]{6}|&[a-f0-9k-o]|&r", Pattern.CASE_INSENSITIVE);
    public static String strip(String s) {
        Matcher match = pattern.matcher(s);
        while (match.find()) {
            String color = s.substring(match.start(), match.end());
            s = s.replace(color, "");
            match = pattern.matcher(s);
        }
        return s;
    }
}
