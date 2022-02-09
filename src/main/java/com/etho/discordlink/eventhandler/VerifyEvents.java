package com.etho.discordlink.eventhandler;

import com.etho.discordlink.eventhandler.events.CodeExpireEvent;
import com.etho.discordlink.eventhandler.events.VerifyFailEvent;
import com.etho.discordlink.eventhandler.events.VerifySuccessEvent;
import com.etho.discordlink.eventhandler.events.vEvent;

import java.util.*;

public class VerifyEvents {
    private static List<VerifyListener> listeners = new ArrayList<>();
    public static void register(VerifyListener l) {
        listeners.add(l);
    }
    public static void remove(VerifyListener l) {
        listeners.remove(l);
    }
    public static void fire(vEvent e) {
        if (e instanceof CodeExpireEvent) {
            CodeExpireEvent event = (CodeExpireEvent) e;
            listeners.forEach(l -> l.codeExpire(event));
        } else if (e instanceof VerifySuccessEvent) {
            VerifySuccessEvent event = (VerifySuccessEvent) e;
            listeners.forEach(l -> l.verifySuccess(event));
        } else if (e instanceof VerifyFailEvent) {
            VerifyFailEvent event = (VerifyFailEvent) e;
            listeners.forEach(l -> l.verifyFail(event));
        }
    }
}
