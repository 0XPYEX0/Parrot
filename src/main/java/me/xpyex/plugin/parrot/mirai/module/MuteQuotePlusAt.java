package me.xpyex.plugin.parrot.mirai.module;

import me.xpyex.plugin.parrot.mirai.core.module.Module;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.QuoteReply;

public class MuteQuotePlusAt extends Module {
    @Override
    public void register() throws Throwable {
        listenEvent(GroupMessageEvent.class, event -> {
            if (event.getGroup().getBotPermission().getLevel() > event.getPermission().getLevel()) {  //Bot权限高于Sender
                if (event.getMessage().get(QuoteReply.Key) != null) {  //消息有回复
                    if (event.getMessage().get(At.Key) != null) {  //消息有At
                        event.getSender().mute(10 * 60);  //十分钟
                        event.getGroup().sendMessage("能不能回复的时候不@人啊你妈的");
                    }
                }
            }
        });
    }
}
