package me.xpyex.plugin.parrot.mirai.module.core;

import me.xpyex.plugin.parrot.mirai.api.CommandMenu;
import me.xpyex.plugin.parrot.mirai.core.command.CommandBus;
import me.xpyex.plugin.parrot.mirai.core.module.CoreModule;
import net.mamoe.mirai.contact.Contact;

public class RestartBroadcast extends CoreModule {
    private static boolean restartMode = false;

    @Override
    public void register() {
        registerCommand(Contact.class, ((source, sender, label, args) -> {
            if (!sender.hasPerm(getName() + ".use")) {
                source.sendMessage("你没有权限");
                return;
            }
            if (args.length == 0) {
                new CommandMenu(label)
                    .add("start", "添加一个重启任务，在10秒后重启")
                    .add("stop", "停止现有的重启任务")
                    .add("now", "立刻重启")
                    .add("exit", "退出机器人，不重启")
                    .send(source);
            } else if (args[0].equalsIgnoreCase("start")) {
                restartMode = true;
                source.sendMessage("Mirai将在 10 秒后重启\n使用 #" + label + " stop 以停止重启");
                try {
                    for (int i = 10; i >= 0; i--) {
                        if (!restartMode) {
                            return;
                        }
                        if (i <= 3) {
                            source.sendMessage("倒计时: " + i);
                        }
                        Thread.sleep(1000);
                    }
                    CommandBus.dispatchCommand(source, sender, "#bot", "shutdown");
                } catch (Throwable ignored) {
                }
            } else if (args[0].equalsIgnoreCase("stop")) {
                restartMode = false;
                source.sendMessage("已取消重启计划");
            } else if (args[0].equalsIgnoreCase("now")) {
                CommandBus.dispatchCommand(source, sender, "#bot", "shutdown");
            } else {
                source.sendMessage("未知子命令");
            }
        }), "rbc", "restart");
    }
}
