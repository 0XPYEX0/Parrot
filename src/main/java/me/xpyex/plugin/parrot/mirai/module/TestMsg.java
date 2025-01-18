package me.xpyex.plugin.parrot.mirai.module;

import java.util.UUID;
import me.xpyex.plugin.parrot.api.MessageBuilder;
import me.xpyex.plugin.parrot.mirai.core.command.CommandArguments;
import me.xpyex.plugin.parrot.mirai.core.command.CommandExecutor;
import me.xpyex.plugin.parrot.mirai.core.command.CommandNode;
import me.xpyex.plugin.parrot.mirai.core.reachable.ParrotContact;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.utils.MsgUtil;
import me.xpyex.plugin.parrot.mirai.utils.Util;
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
        registerCommand(Contact.class,
            CommandNode.of((source, sender, args) -> {
                new MessageBuilder()
                    .plus("这是全局反馈器")
                    .plus("这是一个测试命令捏")
                    .plus("你执行的命令是: " + args.buildLabels())
                    .plus("你填入的参数是: " + args.buildArguments())
                    .send(source);
            }).child(CommandNode.of((source, sender, arguments) -> {
                    new MessageBuilder("革新时代！现在是全新CommandNode命令处理！")
                        .plus("命令本体: " + arguments.buildLabels())
                        .plus("剩余参数: " + arguments.buildArguments())
                        .send(source);
                }), "nodeCheck", "node"
            ),
            "test2");
        registerCommand(Contact.class, CommandNode.of((source, sender, arguments) -> {
            new MessageBuilder()
                .plus("这是全局反馈器")
                .plus("这是一个测试命令捏")
                .plus("你执行的命令是: " + arguments.buildLabels())
                .plus("你填入的参数是: " + arguments.buildArguments())
                .send(source);
        }), "testCmd");
        registerCommand(Group.class, CommandNode.of((source, sender, arguments) ->
                                                        source.sendMessage("这是群反馈器")
        ), "testCmd");
        registerCommand(User.class, CommandNode.<User>of().setExecutor((source, sender, arguments) -> {
                source.sendMessage("这是私聊反馈器");
            }
        ), "testCmd");
        registerCommand(Contact.class, CommandNode.of((source, sender, arguments) -> {
            System.gc();
            source.sendMessage("已执行");
        }).permission("BotManager.gc"), "gc");
        executeOnce(BotOnlineEvent.class, event -> {
            MsgUtil.sendMsgToOwner("已启动");
        });
        registerCommand(Contact.class, CommandNode.of(new CommandExecutor<>() {
            @Override
            public void execute(ParrotContact<Contact> source, ParrotContact<User> sender, CommandArguments arguments) {
                if (source.isGroup()) {
                    if (source.getContactAsGroup().getBotPermission().getLevel() > sender.getContactAsMember().getPermission().getLevel()) {
                        getEvent(source).ifPresent(event -> {
                            recall(event.getSource());
                        });
                    }
                }
                source.sendMessage("这段文本长度为: " + arguments.buildWholeCommand().length());
            }
        }), "length");
        UUID taskUUID = runTaskTimer(() ->
                                         info("这是一条测试消息的亲"),
            10);
        runTaskLater(() -> {
            shutdownRepeatTask(taskUUID);
            info("已停止定时任务 " + taskUUID);
        }, 20);
    }
}
