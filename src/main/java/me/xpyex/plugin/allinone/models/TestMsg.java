package me.xpyex.plugin.allinone.models;

import me.xpyex.plugin.allinone.core.Model;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;

public class TestMsg extends Model {

    public TestMsg() {
        super();
    }

    @Override
    public void register() {
        listenEvent(FriendMessageEvent.class, (e) -> {
            MessageEvent event = (MessageEvent) e;
            if (Util.getPlainText(event.getMessage()).equalsIgnoreCase("test")) {
                Util.getRealSender(event).sendMessage("test");
            }
        });
    }

    @Override
    public String getName() {
        return "TestMsg";
        //
    }
}
