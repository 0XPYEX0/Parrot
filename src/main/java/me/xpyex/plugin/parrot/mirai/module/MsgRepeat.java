package me.xpyex.plugin.parrot.mirai.module;

import me.xpyex.plugin.parrot.mirai.core.command.CommandNode;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import net.mamoe.mirai.contact.User;

public class MsgRepeat extends Module {
    @Override
    public void register() throws Throwable {
        registerCommand(User.class, CommandNode.of((source, sender, arguments) -> {
            if (!arguments.hasMoreArg()) {
                source.sendMessage("参数不足，复读个🔨");
                source.sendMessage("后面填点东西，晚五秒发给你");
                return;
            }
            runTaskLater(() -> source.sendMessage(String.join(" ", arguments.getArguments())), 5);
        }), "repeat");
    }
}
