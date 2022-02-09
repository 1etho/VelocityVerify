package com.etho.discordlink;

import com.etho.discordlink.eventhandler.VerifyEvents;
import com.etho.discordlink.eventhandler.events.VerifySuccessEvent;
import com.etho.discordlink.utils.Chat;
import com.etho.discordlink.utils.Config;
import com.etho.discordlink.utils.discord.RoleSynchronization;
import com.etho.discordlink.utils.sql.SqlConnection;
import com.loohp.interactivechat.libs.org.apache.commons.lang3.RandomStringUtils;
import com.velocitypowered.api.proxy.Player;

import java.lang.management.PlatformLoggingMXBean;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class VerifyCode {
    private String code;
    private int timer;
    private boolean isVerified;
    private boolean isExpired;
    private String player;
    private Player p;

    public VerifyCode(String player, String code, Player p) {
        this(player, code);
        this.p = p;
    }

    private VerifyCode(String player, String code) {
        this.code = code;
        this.player = player;
        this.timer = 0;
    }

    public void task() {
        timer++;
        if (timer > Config.DEFAULT.getInt("verify-expire")) {
            isExpired = true;
            if (Config.MESSAGES.getBoolean("on-code-expire-message")) {
                p.sendMessage(Chat.color(Config.MESSAGES.getString("on-code-expire")));
            }
        }
    }

    public Result attempt(String playerName, String codeAttempt, String tag) {
        if (isExpired) return Result.EXPIRED;
        if (isVerified) return Result.EXPIRED;
        if (timer > Config.DEFAULT.getInt("verify-expire")) {
            isExpired = true;
        }
        if (playerName.equals(player)) {
            if (codeAttempt.equals(code)) {
                //LINK DISCORD AND MC ACCOUNT
                verify(tag);
                Discordlink.remove(this);
                return Result.SUCCESS;
            } else {
                return Result.INCORRECT;
            }
        } else {
            return Result.INCORRECT;
        }
    }

    public void verify(String tag) {
        SqlConnection.addVerified(p.getUniqueId(), p.getUsername(), tag);
        isVerified = true;
        for (String line : Config.MESSAGES.getStringList("on-verify")) {
            p.sendMessage(Chat.color(line));
        }
        if (Config.DEFAULT.getBoolean("role-sync.enable")) {
            RoleSynchronization.update(p, tag);
        }
    }

    public enum Result {
        SUCCESS,
        EXPIRED,
        INCORRECT
    }

    public boolean isInvalid() {
        return isVerified || isExpired || timer > Config.DEFAULT.getInt("verify-expire");
    }

    public static String generate() {
        return RandomStringUtils.random(7, true, true);
    }

    public String getPlayerName() {
        return player;
    }

    public Player getPlayer() {
        return p;
    }

    public String getCode() {
        return code;
    }

    public int getTimer() {
        return timer;
    }

    public int getTimeTillExpired() {
        return Config.DEFAULT.getInt("verify-expire") - timer;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public boolean isExpired() {
        return isExpired;
    }
}
