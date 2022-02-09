package com.etho.discordlink.listener;

import com.etho.discordlink.Discordlink;
import com.etho.discordlink.VerifyCode;
import com.etho.discordlink.utils.Chat;
import com.etho.discordlink.utils.Config;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;

import java.util.stream.Collectors;

public class ChatListener {

    @Subscribe
    public void onPlayerChat(PlayerChatEvent e) {
            if (Config.DEFAULT.getBoolean("catch-code")) {
                for (String str : Discordlink.getCodes().stream().map(VerifyCode::getCode).collect(Collectors.toList())) {
                    if (e.getMessage().contains(str)) {
                        e.setResult(PlayerChatEvent.ChatResult.denied());
                        e.getPlayer().sendMessage(Chat.color(Config.MESSAGES.getString("catch-code-message")));
                    }
                }
            }
    }
}
