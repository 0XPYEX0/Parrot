package me.xpyex.plugin.parrot.mirai.module;

import me.xpyex.plugin.parrot.mirai.core.module.Module;
import net.mamoe.mirai.contact.User;

public class MsgRepeat extends Module {
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
