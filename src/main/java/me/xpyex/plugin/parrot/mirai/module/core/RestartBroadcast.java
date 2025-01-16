package me.xpyex.plugin.parrot.mirai.module.core;

import me.xpyex.plugin.parrot.mirai.api.CommandMenu;
import me.xpyex.plugin.parrot.mirai.core.command.CommandArguments;
import me.xpyex.plugin.parrot.mirai.core.command.CommandBus;
import me.xpyex.plugin.parrot.mirai.core.command.CommandNode;
import me.xpyex.plugin.parrot.mirai.core.module.CoreModule;
import net.mamoe.mirai.contact.Contact;

public class RestartBroadcast extends CoreModule {
    private static boolean restartMode = false;

    @Override
    public void register() {
        registerCommand(Contact.class,
            CommandNode.of((source, sender, arguments) -> {
                    new CommandMenu(arguments)
                        .add("start", "添加一个重启任务，在10秒后重启")
                        .add("stop", "停止现有的重启任务")
                        .add("now", "立刻重启")
                        .add("exit", "退出机器人，不重启")
                        .send(source);
                })
                .permission(getName() + ".use")
                .child(CommandNode.of((source, sender, arguments) -> {
                    restartMode = true;
                    source.sendMessage("Mirai将在 10 秒后重启\n使用 #" + arguments.getLabel(0) + " stop 以停止重启");
                    for (int i = 10; i >= 0; i--) {
                        if (!restartMode) {
                            return;
                        }
                        if (i <= 3) {
                            source.sendMessage("倒计时: " + i);
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    CommandBus.dispatchCommand(source, sender, CommandArguments.of("#bot", "shutdown"));
                }), "start")
                .child(CommandNode.of((source, sender, arguments) -> {
                    restartMode = false;
                    source.sendMessage("已取消重启计划");
                }), "stop")
                .notMatchedArg((source, sender, arguments) -> {
                    source.sendMessage("未知子命令");
                })
            , "rbc", "restart");
    }
}
