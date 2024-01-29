package me.xpyex.plugin.allinone.modulecode.music;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Message;

@FunctionalInterface
public interface MusicCardProvider {
    Message process(MusicInfo mi, Contact ct) throws Exception;
}