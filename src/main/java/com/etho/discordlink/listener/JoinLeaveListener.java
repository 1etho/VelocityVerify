package com.etho.discordlink.listener;

import com.etho.discordlink.utils.discord.RoleSynchronization;
import com.etho.discordlink.utils.sql.SqlConnection;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;

import java.util.UUID;

public class JoinLeaveListener {

    @Subscribe
    public void onPlayerJoin(ServerConnectedEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        String name = SqlConnection.getVerifiedUsername(uuid);
        if (name != null) {
            if (!p.getUsername().equals(name)) {
                SqlConnection.updateUsername(name, p.getUsername());
            }
            RoleSynchronization.update(p, SqlConnection.getVerifiedDiscord(p.getUniqueId()), true);
        }
    }
}
