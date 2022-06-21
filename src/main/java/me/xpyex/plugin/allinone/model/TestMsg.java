package me.xpyex.plugin.allinone.model;

import java.util.Arrays;
import me.xpyex.plugin.allinone.core.Model;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Stranger;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;

public class TestMsg extends Model {

    @Override
    public void register() {
        listenEvent(MessageEvent.class, (event) -> {
            if (Util.getPlainText(event.getMessage()).equalsIgnoreCase("test")) {
                Util.getRealSender(event).sendMessage("test");
            }
        });
        registerCommand(Contact.class, ((source, sender, label, args) ->
                source.sendMessage("这是全局反馈器\n" +
                        "这是一个测试命令捏\n" +
                        "你执行的命令是: " + label + "\n" +
                        "你填入的参数是: " + Arrays.toString(args))
        ), "testCmd");
        registerCommand(Group.class, ((source, sender, label, args) ->
                source.sendMessage("这是群反馈器")
        ), "testCmd");
        registerCommand(User.class, ((source, sender, label, args) ->
                source.sendMessage("这是私聊反馈器")
        ), "testCmd");
    }
}
