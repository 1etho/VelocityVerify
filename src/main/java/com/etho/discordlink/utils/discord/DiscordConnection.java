package com.etho.discordlink.utils.discord;

import com.etho.discordlink.Discordlink;
import com.etho.discordlink.VerifyCode;
import com.etho.discordlink.eventhandler.VerifyEvents;
import com.etho.discordlink.eventhandler.events.VerifyFailEvent;
import com.etho.discordlink.eventhandler.events.VerifySuccessEvent;
import com.etho.discordlink.utils.Config;
import com.etho.discordlink.utils.sql.SqlConnection;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.simpleyaml.configuration.file.FileConfiguration;

import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.stream.Collectors;

public class DiscordConnection extends ListenerAdapter {
    static DiscordConnection instance;

    private JDA jda;

    public JDA getJda() {
        return this.jda;
    }

    public void init(){
        try {
            if (instance == null) {
                instance = new DiscordConnection();
                instance.jda = JDABuilder.createDefault(Config.DEFAULT.getString("bot-token"))
                        .addEventListeners(this)
                        .build();
                instance.jda.awaitReady();
                Discordlink.logger().info("Initialized discord bot bridge.");
            }
        } catch (InterruptedException | LoginException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        User sender = e.getAuthor();
        Message message = e.getMessage();
        MessageChannel channel = e.getChannel();
        if (sender.isBot()) return;
        if (Config.DEFAULT.getStringList("channels").contains(channel.getId())) {
            String msg = message.getContentRaw();
            String[] args = msg.split(" ");
            FileConfiguration config = Config.MESSAGES;
            if (msg.startsWith("verify")) {
                if (args.length > 1) {
                    switch (args[1].toLowerCase()) {
                        case "help":
                            verifyHelp(sender, message, channel);
                            break;
                        case "unlink":
                            if (args.length > 2) {
                                String name = args[2];
                                String discordTag = SqlConnection.getVerifiedDiscord(name);
                                if (discordTag == null) {
                                    message.reply(new EmbedBuilder()
                                            .setTitle(config.getString("discord.verify-unlink.incorrect.title"))
                                            .setDescription(config.getString("discord.verify-unlink.incorrect.description")
                                                    .replace("%account%", name))
                                            .setThumbnail(config.getString("discord.verify-unlink.incorrect.image"))
                                            .build()).queue();
                                    return;
                                }
                                if (discordTag.equals(sender.getId())) {
                                    List<String> names = SqlConnection.getVerifiedUsernames(sender.getId());
                                    if (names == null) {
                                        message.reply(new EmbedBuilder()
                                                .setTitle(config.getString("discord.verify-unlink.incorrect.title"))
                                                .setDescription(config.getString("discord.verify-unlink.incorrect.description")
                                                        .replace("%account%", name))
                                                .setThumbnail(config.getString("discord.verify-unlink.incorrect.image"))
                                                .build()).queue();
                                        return;
                                    } else {
                                        if (names.contains(name)) {
                                            SqlConnection.removeVerified(name);
                                            message.reply(new EmbedBuilder()
                                                    .setTitle(config.getString("discord.verify-unlink.success.title"))
                                                    .setDescription(config.getString("discord.verify-unlink.success.description")
                                                            .replace("%account%", name))
                                                    .setThumbnail(config.getString("discord.verify-unlink.success.image"))
                                                    .build()).queue();
                                            return;
                                        }
                                    }
                                } else {
                                    message.reply(new EmbedBuilder()
                                            .setTitle(config.getString("discord.verify-unlink.incorrect.title"))
                                            .setDescription(config.getString("discord.verify-unlink.incorrect.description")
                                                    .replace("%account%", name))
                                            .setThumbnail(config.getString("discord.verify-unlink.incorrect.image"))
                                            .build()).queue();
                                }
                            }
                            break;
                        case "accounts":
                            List<String> names = SqlConnection.getVerifiedUsernames(sender.getId());
                            if (names == null) {
                                message.reply(new EmbedBuilder()
                                        .setTitle(config.getString("discord.verified-accounts.title"))
                                        .setDescription(config.getString("discord.verified-accounts.description-empty"))
                                        .setThumbnail(config.getString("discord.verified-accounts.image"))
                                        .build()).queue();
                            } else {
                                message.reply(new EmbedBuilder()
                                        .setTitle(config.getString("discord.verified-accounts.title"))
                                        .setDescription(config.getString("discord.verified-accounts.description")
                                                .replace("%accounts%",
                                                        names.stream().map(n -> n = config.getString("discord.verified-accounts.format")
                                                                .replace("%name%", n)).collect(Collectors.joining("\n"))))
                                        .setThumbnail(config.getString("discord.verified-accounts.image"))
                                        .build()).queue();
                            }
                            break;
                        default:
                            // Must be a linking request
                            if (args.length > 2) {
                                String name = args[1];
                                String code = args[2];
                                for (VerifyCode c : new ArrayList<>(Discordlink.getCodes())) {
                                    if (c.getPlayerName().equals(name)) {
                                        if (SqlConnection.isVerified(name)) {
                                            message.reply(new EmbedBuilder()
                                                    .setTitle(config.getString("discord.verify.already-linked.title"))
                                                    .setDescription(config.getString("discord.verify.already-linked.description"))
                                                    .setThumbnail(config.getString("discord.verify.already-linked.image"))
                                                    .build()).queue();
                                            return;
                                        }
                                        VerifyCode.Result result = c.attempt(name, code, sender.getId());
                                        switch (result) {
                                            case SUCCESS:
                                                message.reply(new EmbedBuilder()
                                                        .setTitle(config.getString("discord.verify.success.title"))
                                                        .setDescription(config.getString("discord.verify.success.description")
                                                                .replace("%account%", name))
                                                        .setThumbnail(config.getString("discord.verify.success.image"))
                                                        .build()).queue();
                                                VerifyEvents.fire(new VerifySuccessEvent(c, e));
                                                return;
                                            case INCORRECT: case EXPIRED:
                                                message.reply(new EmbedBuilder()
                                                        .setTitle(config.getString("discord.verify.failure.title"))
                                                        .setDescription(config.getString("discord.verify.failure.description")
                                                                .replace("%account%", name))
                                                        .setThumbnail(config.getString("discord.verify.failure.image"))
                                                        .build()).queue();
                                                VerifyEvents.fire(new VerifyFailEvent(c, e));
                                                return;
                                        }
                                    } else {
                                        verifyHelp(sender, message, channel);
                                    }
                                }
                                message.reply(new EmbedBuilder()
                                        .setTitle(config.getString("discord.verify.failure.title"))
                                        .setDescription(config.getString("discord.verify.failure.description")
                                                .replace("%account%", name))
                                        .setThumbnail(config.getString("discord.verify.failure.image"))
                                        .build()).queue();
                                VerifyEvents.fire(new VerifyFailEvent(null, e));
                            } else {
                                verifyHelp(sender, message, channel);
                            }
                    }
                } else {
                    verifyHelp(sender, message, channel);
                }
            }
        }
    }

    public void verifyHelp(User sender, Message message, MessageChannel channel) {
        FileConfiguration config = Config.MESSAGES;
        message.reply(new EmbedBuilder()
                .setTitle(config.getString("discord.verify-help.title"))
                .setDescription(config.getString("discord.verify-help.description"))
                .setThumbnail(config.getString("discord.verify-help.image")).build()).queue();
    }

    public void close() {
        if (jda != null) {
            jda.shutdownNow();
        }
        instance = null;
    }


    public static DiscordConnection get() {
        return instance;
    }
}
