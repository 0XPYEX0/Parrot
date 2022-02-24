package me.xpyex.plugin.allinone.functions.music;

import net.mamoe.mirai.message.data.MessageChain;
@FunctionalInterface
public interface MusicCardProvider {
    public MessageChain process(MusicInfo mi);
}