package me.xpyex.plugin.allinone.model.core;

import me.xpyex.plugin.allinone.api.CommandMenu;
import me.xpyex.plugin.allinone.core.CoreModel;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.contact.Contact;

public class RestartBroadcast extends CoreModel {
    private static boolean restartMode = false;

    @Override
    public void register() {
        registerCommand(Contact.class, ((source, sender, label, args) -> {
            if (sender.getId() != 1723275529L) {
                Util.sendMsg(source, "你没有权限");
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
                Util.sendMsg(source, "Mirai将在 10 秒后重启\n使用 #" + label + " stop 以停止重启");
                try {
                    for (int i = 10; i >= 0; i--) {
                        if (!restartMode) {
                            return;
                        }
                        if (i <= 3) {
                            Util.sendMsg(source, "倒计时: " + i);
                        }
                        Thread.sleep(1000);
                    }
                    Util.sendMsg(source, "开始重启");
                    Util.runCmd("cmd /c start /b RestartMirai.bat");
                } catch (Throwable ignored) {}
            } else if (args[0].equalsIgnoreCase("stop")) {
                restartMode = false;
                Util.sendMsg(source, "已取消重启计划");
            } else if (args[0].equalsIgnoreCase("now")) {
                Util.sendMsg(source, "开始重启");
                Util.runCmd("cmd /c start /b RestartMirai.bat");
            } else if (args[0].equalsIgnoreCase("exit")) {
                Util.sendMsg(source, "关闭Bot");
                Util.runCmd("cmd /c start /b StopMirai.bat");
            } else {
                Util.sendMsg(source, "未知子命令");
            }
        }), "rbc", "restart");
    }
}
