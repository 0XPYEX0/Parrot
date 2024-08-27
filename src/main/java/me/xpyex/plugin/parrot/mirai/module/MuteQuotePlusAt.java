package me.xpyex.plugin.parrot.mirai.module;

import me.xpyex.plugin.parrot.mirai.core.module.Module;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.QuoteReply;

public class MuteQuotePlusAt extends Module {
    @Override
    public void register() throws Throwable {
        listenEvent(GroupMessageEvent.class, event -> {
            if (event.getGroup().getBotPermission().getLevel() > event.getPermission().getLevel()) {  //Bot权限高于Sender
                QuoteReply quote = event.getMessage().get(QuoteReply.Key);
                if (quote != null) {  //消息有回复
                    if (event.getMessage().get(At.Key) != null) {  //消息有At
                        if (!event.getMessage().contentToString().contains("@" + quote.getSource().getFromId())) return;  //如果回复时@的不是消息的发送者，那可能是刻意在@其他人，不应处理

                        event.getSender().mute(10 * 60);  //十分钟
                        event.getGroup().sendMessage("能不能回复的时候不@人啊你妈的");
                        Mirai.getInstance().recallMessage(event.getBot(), event.getSource());  //撤回
                        MessageChain origin = event.getMessage();
                        origin.removeIf(msg -> msg.contentToString().startsWith("[mirai:at"));  //移除At部分的内容
                        event.getGroup().sendMessage(
                            new ForwardMessageBuilder(event.getGroup())
                                .add(event.getGroup().getBotAsMember(), new PlainText("原消息如下"))
                                .add(event.getSender(), origin)
                                .build()
                        );
                    }
                }
            }
        });
    }
}
