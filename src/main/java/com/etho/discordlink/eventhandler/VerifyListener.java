package com.etho.discordlink.eventhandler;

import com.etho.discordlink.eventhandler.events.CodeExpireEvent;
import com.etho.discordlink.eventhandler.events.VerifyFailEvent;
import com.etho.discordlink.eventhandler.events.VerifySuccessEvent;

public interface VerifyListener {
    // Fired when a code expires
    default void codeExpire(CodeExpireEvent e) {
    }
    // Fired when a code is verified
    default void verifySuccess(VerifySuccessEvent e) {
    }
    // Fired when a code is tried but failed
    default void verifyFail(VerifyFailEvent e) {
    }
}
