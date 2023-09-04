package me.xpyex.plugin.allinone.model.core;

import me.xpyex.plugin.allinone.api.CommandMenu;
import me.xpyex.plugin.allinone.core.CoreModel;
import me.xpyex.plugin.allinone.utils.MsgUtil;
import net.mamoe.mirai.console.MiraiConsoleImplementation;
import net.mamoe.mirai.contact.Contact;

public class RestartBroadcast extends CoreModel {
    private static boolean restartMode = false;

    @Override
    public void register() {
        registerCommand(Contact.class, ((source, sender, label, args) -> {
            if (!sender.hasPerm(getName() + ".use", false)) {
                MsgUtil.sendMsg(source, "你没有权限");
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
                MsgUtil.sendMsg(source, "Mirai将在 10 秒后重启\n使用 #" + label + " stop 以停止重启");
                try {
                    for (int i = 10; i >= 0; i--) {
                        if (!restartMode) {
                            return;
                        }
                        if (i <= 3) {
                            MsgUtil.sendMsg(source, "倒计时: " + i);
                        }
                        Thread.sleep(1000);
                    }
                    MsgUtil.sendMsg(source, "开始重启");
                    MiraiConsoleImplementation.class.getMethod("shutdown").invoke(null);
                } catch (Throwable ignored) {
                }
            } else if (args[0].equalsIgnoreCase("stop")) {
                restartMode = false;
                MsgUtil.sendMsg(source, "已取消重启计划");
            } else if (args[0].equalsIgnoreCase("now")) {
                MsgUtil.sendMsg(source, "开始重启");
                MiraiConsoleImplementation.class.getMethod("shutdown").invoke(null);
            } else if (args[0].equalsIgnoreCase("exit")) {
                MsgUtil.sendMsg(source, "关闭Bot");
                MiraiConsoleImplementation.class.getMethod("shutdown").invoke(null);
            } else {
                MsgUtil.sendMsg(source, "未知子命令");
            }
        }), "rbc", "restart");
    }
}
