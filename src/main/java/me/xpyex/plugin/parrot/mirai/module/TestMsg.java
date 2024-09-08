package me.xpyex.plugin.parrot.mirai.module;

import java.util.Arrays;
import java.util.UUID;
import me.xpyex.plugin.parrot.mirai.api.MessageBuilder;
import me.xpyex.plugin.parrot.mirai.core.command.CommandExecutor;
import me.xpyex.plugin.parrot.mirai.core.mirai.ParrotContact;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.utils.MsgUtil;
import me.xpyex.plugin.parrot.mirai.utils.Util;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.event.events.MessageEvent;

@SuppressWarnings("unused")
public class TestMsg extends Module {
    static {
        Util.OWNER_ID = 1723275529L;
    }

    @Override
    public void register() {
        listenEvent(MessageEvent.class, event -> {
            if (getPlainText(event.getMessage()).equalsIgnoreCase("test")) {
                autoSendMsg(event, "test");
            }
        });
        listenEvent(MessageEvent.class, event -> {
            if (getPlainText(event.getMessage()).equalsIgnoreCase("test")) {
                autoSendMsg(event, "test2, 第二个监听器也成功注册了！");
            }
        });
        registerCommand(Contact.class, (source, sender, label, args) -> {
            new MessageBuilder()
                .plus("这是全局反馈器")
                .plus("这是一个测试命令捏")
                .plus("你执行的命令是: " + label)
                .plus("你填入的参数是: " + Arrays.toString(args))
                .send(source);
        }, "testCmd");
        registerCommand(Contact.class, (source, sender, label, args) -> {
            new MessageBuilder()
                .plus("这是全局反馈器")
                .plus("这是一个测试命令捏")
                .plus("你执行的命令是: " + label)
                .plus("你填入的参数是: " + Arrays.toString(args))
                .send(source);
        }, "test2");
        registerCommand(Group.class, ((source, sender, label, args) ->
                                          source.sendMessage("这是群反馈器")
        ), "testCmd");
        registerCommand(User.class, ((source, sender, label, args) ->
                                         source.sendMessage("这是私聊反馈器")
        ), "testCmd");
        registerCommand(Contact.class, (source, sender, label, args) -> {
            System.gc();
            source.sendMessage("已执行");
        }, "gc");
        executeOnce(BotOnlineEvent.class, event -> {
            MsgUtil.sendMsgToOwner("已启动");
        });
        registerCommand(Contact.class, new CommandExecutor<>() {
            @Override
            public void execute(ParrotContact<Contact> source, ParrotContact<User> sender, String label, String... args) {
                if (source.isGroup()) {
                    if (source.getContactAsGroup().getBotPermission().getLevel() > sender.getContactAsMember().getPermission().getLevel()) {
                        getEvent(source).ifPresent(event -> {
                            Mirai.getInstance().recallMessage(Util.getBot(), event.getSource());
                        });
                    }
                }
                source.sendMessage("这段文本长度为: " + String.join(" ", args).length());
            }
        }, "length");
        UUID taskUUID = runTaskTimer(() ->
                                         info("这是一条测试消息的亲"),
            10);
        runTaskLater(() -> {
            shutdownRepeatTask(taskUUID);
            info("已停止定时任务 " + taskUUID);
        }, 20);
    }
}
