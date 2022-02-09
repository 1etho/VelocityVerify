package com.etho.discordlink.utils.discord;

import com.etho.discordlink.Discordlink;
import com.etho.discordlink.utils.Chat;
import com.etho.discordlink.utils.Config;
import com.etho.discordlink.utils.sql.SqlConnection;
import com.velocitypowered.api.proxy.Player;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.platform.PlayerAdapter;

import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public class RoleSynchronization {
    public static void update() {
        Discordlink.logger().info("Syncing ranks for " + Discordlink.server().getAllPlayers().size() + " players.");
        PlayerAdapter<Player> adapter = LuckPermsProvider.get().getPlayerAdapter(Player.class);
        for (Player p : Discordlink.server().getAllPlayers()) {
            String dc = SqlConnection.getVerifiedDiscord(p.getUniqueId());
            if (dc != null) {
                CachedMetaData data = adapter.getUser(p).getCachedData().getMetaData();
                String group = data.getPrimaryGroup();
                update(p, group, dc);
            }
        }
    }

    public static void update(Player p, String discordId) {
        PlayerAdapter<Player> adapter = LuckPermsProvider.get().getPlayerAdapter(Player.class);
        if (p.isActive()) {
            CachedMetaData data = adapter.getUser(p).getCachedData().getMetaData();
            String group = data.getPrimaryGroup();
            update(p, group, discordId);
        }
    }

    public static void update(Player p, String group, String discordId) {
        if (Config.DEFAULT.getString("role-sync.mapping." + group) != null) {
            long roleid = Config.DEFAULT.getLong("role-sync.mapping."+group);
            Role r = DiscordConnection.get().getJda().getRoleById(roleid);
            User u = DiscordConnection.get().getJda().getUserById(discordId);
            Guild g = DiscordConnection.get().getJda().getGuildById(Config.DEFAULT.getLong("role-sync.server-id"));
            if (u != null) {
                if (g != null) {
                    if (g.getMember(u) != null) {
                        if (!(g.getMember(u).getRoles().stream().map(Role::getId).collect(Collectors.toList()).contains(r.getId()))) {
                            g.addRoleToMember(g.getMember(u), r).queue();
                            if (p.isOnlineMode()) {
                                System.out.println(5);
                                p.sendMessage(Chat.color(Config.MESSAGES.getString("on-sync-rank")
                                        .replace("%rank%", r.getName())
                                        .replace("%role%", r.getName())
                                        .replace("%role_color%", ("&#" + Integer.toHexString(r.getColor().getRGB()).substring(2)))));
                            }
                        }
                    }
                }
            }
        }
    }
}
