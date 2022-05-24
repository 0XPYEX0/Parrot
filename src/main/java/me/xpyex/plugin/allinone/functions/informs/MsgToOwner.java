package me.xpyex.plugin.allinone.functions.informs;

import me.xpyex.plugin.allinone.functions.botchecker.BotChecker;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.PlainText;

public class MsgToOwner {
    public static void Execute(MessageEvent event) {
        if (Util.isGroupEvent(event)) {
            return;
        }
        if (event.getSender().getId() == 1723275529L || event.getSender().getId() == event.getBot().getId()) {
            return;
        }
        Util.sendFriendMsg(1723275529L, new PlainText("是否好友: " + Util.isFriendEvent(event) + "\n验证中: " + BotChecker.answers.containsKey(event.getSender().getId()) + "\n" + event.getSender().getNick() + "(" + event.getSender().getId() + ") :\n\n").plus(event.getMessage()));
    }
    public static void sendMsgToOwner(String msg) {
        Util.sendFriendMsg(1723275529L, msg);
    }
}
