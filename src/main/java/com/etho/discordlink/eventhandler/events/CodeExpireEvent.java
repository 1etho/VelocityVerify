package com.etho.discordlink.eventhandler.events;

import com.etho.discordlink.VerifyCode;

public class CodeExpireEvent extends vEvent{
    public CodeExpireEvent(VerifyCode c) {
        super(c);
    }
}
