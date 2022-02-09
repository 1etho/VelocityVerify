package com.etho.discordlink.eventhandler.events;

import com.etho.discordlink.VerifyCode;
import com.velocitypowered.api.proxy.Player;

public class vEvent {
    private VerifyCode code;

    public vEvent(VerifyCode c) {
        this.code = c;
    }

    public Player getPlayer() {
        return code.getPlayer();
    }

    public VerifyCode getCode() {
        return code;
    }

}
