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
                source.sendMessage("你没有权限");
                return;
            }
            if (args.length == 0) {
                CommandMenu helper = new CommandMenu(label)
                        .add("start", "添加一个重启任务，在10秒后重启")
                        .add("stop", "停止现有的重启任务")
                        .add("now", "立刻重启")
                        .add("exit", "退出机器人，不重启");
                source.sendMessage(helper.toString());
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
                    source.sendMessage("开始重启");
                    Util.runCmdFile("cmd /c start /b RestartMirai.bat");
                } catch (Throwable ignored) {}
            } else if (args[0].equalsIgnoreCase("stop")) {
                restartMode = false;
                source.sendMessage("已取消重启计划");
            } else if (args[0].equalsIgnoreCase("now")) {
                source.sendMessage("开始重启");
                Util.runCmdFile("cmd /c start /b RestartMirai.bat");
            } else if (args[0].equalsIgnoreCase("exit")) {
                source.sendMessage("关闭Bot");
                Util.runCmdFile("cmd /c start /b StopMirai.bat");
            } else {
                source.sendMessage("未知子命令");
            }
        }), "rbc", "restart");
    }
}
