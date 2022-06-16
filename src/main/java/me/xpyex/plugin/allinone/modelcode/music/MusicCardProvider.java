package me.xpyex.plugin.allinone.modelcode.music;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Message;

@FunctionalInterface
public interface MusicCardProvider {
    public Message process(MusicInfo mi, Contact ct) throws Exception;
}