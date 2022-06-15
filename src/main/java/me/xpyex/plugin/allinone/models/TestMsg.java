package me.xpyex.plugin.allinone.models;

import java.util.Arrays;
import me.xpyex.plugin.allinone.core.Model;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.event.events.MessageEvent;

public class TestMsg extends Model {

    @Override
    public void register() {
        listenEvent(MessageEvent.class, (event) -> {
            if (Util.getPlainText(event.getMessage()).equalsIgnoreCase("test")) {
                Util.getRealSender(event).sendMessage("test");
            }
        });
        registerCommand((source, sender, label, args) -> {
            source.sendMessage("这是一个测试命令捏");
            source.sendMessage("你执行的命令是: " + label);
            source.sendMessage("你填入的参数是: " + Arrays.toString(args));
        }, "testCmd");
    }

    @Override
    public String getName() {
        return "TestMsg";
        //
    }
}
