package com.etho.discordlink.eventhandler.events;

import com.etho.discordlink.VerifyCode;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class VerifySuccessEvent extends vEvent {
    private MessageReceivedEvent e;
    public VerifySuccessEvent(VerifyCode c, MessageReceivedEvent e) {
        super(c);
        this.e = e;
    }

    public MessageReceivedEvent getMessage() {
        return e;
    }
}
