package me.xpyex.plugin.parrot.mirai.module;

import lombok.experimental.ExtensionMethod;
import me.xpyex.plugin.parrot.mirai.core.command.argument.ArgParser;
import me.xpyex.plugin.parrot.mirai.core.command.argument.UserParser;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.module.core.PermManager;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.QuoteReply;

@ExtensionMethod(ArgParser.class)
public class MuteQuotePlusAt extends Module {
    public MuteQuotePlusAt() {
        this.DEFAULT_DISABLED = true;
    }

    @Override
    public void register() throws Throwable {
        listenEvent(GroupMessageEvent.class, event -> {
            if (event.getGroup().getBotPermission().getLevel() > event.getPermission().getLevel()) {  //Bot权限高于Sender
                QuoteReply quote = event.getMessage().get(QuoteReply.Key);
                if (quote != null) {  //消息有回复
                    if (event.getMessage().get(At.Key) != null) {  //消息有At
                        if (!event.getMessage().contentToString().contains("@" + quote.getSource().getFromId()))
                            return;  //如果回复时@的不是消息的发送者，那可能是刻意在@其他人，不应处理
                        if (PermManager.hasPerm(event.getSender(), getName() + ".bypass", null))
                            return;

                        event.getSender().mute(30);  //30s
                        event.getGroup().sendMessage("能不能回复的时候不@人啊你妈的");
                        recall(event.getSource());  //撤回
                        event.getGroup().sendMessage(
                            new ForwardMessageBuilder(event.getGroup())
                                .add(UserParser.class.of().parse(quote.getSource().getFromId()).orElse(event.getGroup().getBotAsMember()), quote.getSource().getOriginalMessage())
                                //回复的那条信息，发送的用户可能已经退群了。所以如果get不到就默认为Bot
                                .add(event.getGroup().getBotAsMember(), new PlainText("原消息如下"))
                                .add(event.getSender(), event.getMessage())
                                .build()
                        );
                    }
                }
            }
        });
    }
}
