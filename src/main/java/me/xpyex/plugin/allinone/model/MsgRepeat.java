package me.xpyex.plugin.allinone.model;

import me.xpyex.plugin.allinone.core.Model;
import net.mamoe.mirai.contact.User;

public class MsgRepeat extends Model {
    @Override
    public void register() throws Throwable {
        registerCommand(User.class, (source, sender, label, args) -> {
            if (args.length == 0) {
                source.sendMessage("参数不足，复读个🔨");
                source.sendMessage("后面填点东西，晚五秒发给你");
                return;
            }
            runTaskLater(() -> source.sendMessage(String.join(" ", args)), 5);
        }, "repeat");
    }
}
