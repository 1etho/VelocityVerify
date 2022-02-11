package com.etho.discordlink.command;

import com.etho.discordlink.Discordlink;
import com.etho.discordlink.VerifyCode;
import com.etho.discordlink.utils.Chat;
import com.etho.discordlink.utils.Config;
import com.etho.discordlink.utils.discord.DiscordConnection;
import com.etho.discordlink.utils.sql.SqlConnection;
import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;

public class VerifyCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (!(source instanceof Player)) {
            if (source.hasPermission("verify.command.reload")) {
                reload();
                source.sendMessage(Component.text("Reloaded the config successfully.").color(NamedTextColor.RED));
            } else {
                source.sendMessage(Component.text("Player only command").color(NamedTextColor.RED));
            }
            return;
        }
        Player p = (Player) source;
        if (p.hasPermission("verify.command")) {
            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "reload":
                        if (p.hasPermission("verify.command.reload")) {
                            reload();
                            p.sendMessage(Chat.color("&cReloaded the config successfully."));
                        } else {
                            p.sendMessage(Chat.color(Config.MESSAGES.getString("no-permission")));
                        }
                        break;
                    case "help":
                        if (p.hasPermission("verify.command.help")) {
                            showHelp(p);
                        } else {
                            p.sendMessage(Chat.color(Config.MESSAGES.getString("no-permission")));
                        }
                        break;
                }
            } else {
                if (p.hasPermission("verify.command")) {
                    if (SqlConnection.isVerified(p.getUsername())) {
                        p.sendMessage(Chat.color(Config.MESSAGES.getString("already-linked")));
                        return;
                    }
                    for (VerifyCode c : new ArrayList<>(Discordlink.getCodes())) {
                        if (c.getPlayerName().equals(p.getUsername())) {
                            Discordlink.removeAll(c.getPlayerName());
                        }
                    }
                    String code = VerifyCode.generate();
                    for (String line : Config.MESSAGES.getStringList("verify")) {
                        if (line.contains("%code%")) {
                            if (Config.DEFAULT.getBoolean("verify-whole-command")) {
                                p.sendMessage(Chat.color(line.replace("%code%", code))
                                        .clickEvent(ClickEvent.suggestCommand("verify " + p.getUsername() + " " + code)));
                            } else {
                                p.sendMessage(Chat.color(line.replace("%code%", code))
                                        .clickEvent(ClickEvent.openUrl(code)));
                            }
                            Discordlink.addCode(p.getUsername(), code, p);
                        } else {
                            p.sendMessage(Chat.color(line));
                        }
                    }
                } else {
                    p.sendMessage(Chat.color(Config.MESSAGES.getString("no-permission")));
                }
            }
        } else {
            p.sendMessage(Chat.color(Config.MESSAGES.getString("no-permission")));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (invocation.source().hasPermission("verify.command")) {
            if (args.length == 1) {
                List<String> c = new ArrayList<>();
                if ("reload".startsWith(args[0].toLowerCase()) && invocation.source().hasPermission("verify.command.reload")) c.add("reload");
                if ("help".startsWith(args[0].toLowerCase()) && invocation.source().hasPermission("verify.command.help")) c.add("help");
                return c;
            }
        }
        return ImmutableList.of();
    }

    public void showHelp(Player p) {
        for (String line : Config.MESSAGES.getStringList("verify-help")) {
            p.sendMessage(Chat.color(line));
        }
    }

    public void reload() {
        Config.init();
        DiscordConnection.get().close();
        new DiscordConnection().init();
        Discordlink.get().repeating();
    }
}
