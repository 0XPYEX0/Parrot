package me.xpyex.plugin.allinone.functions.manager;

import me.xpyex.plugin.allinone.Utils;
import net.mamoe.mirai.event.events.MessageEvent;

public class CoreCmds {
    public static boolean restartMode = false;
    public static void Execute(MessageEvent event) {
        String[] cmd = Utils.getNormalText(event.getMessage()).split(" ");
        if (cmd[0].equalsIgnoreCase("/restart") || cmd[0].equalsIgnoreCase("/rbc")) {
            if (event.getSender().getId() != 1723275529L) {
                Utils.autoSendMsg(event, "你没有权限");
                return;
            }
            if (cmd.length == 1 || cmd[1].equalsIgnoreCase("help")) {
                Utils.autoSendMsg(event, cmd[0] + " start\n"
                        + cmd[0] + " stop\n"
                        + cmd[0] + " now"
                );
                return;
            }
            if (cmd[1].equalsIgnoreCase("start")) {
                restartMode = true;
                Utils.autoSendMsg(event, "Mirai将在 10 秒后重启\n使用 " + cmd[0] + " stop 以停止重启");
                try {
                    for (int i = 10; i >= 0; i--) {
                        if (!restartMode) {
                            return;
                        }
                        if (i <= 3) {
                            Utils.autoSendMsg(event, "倒计时: " + i);
                        }
                        Thread.sleep(1000);
                    }
                    Utils.autoSendMsg(event, "开始重启");
                    Utils.runCmdFile("cmd /c start /b MiraiOK.bat");
                    System.exit(0);
                } catch (Throwable ignored) {}
            } else if (cmd[1].equalsIgnoreCase("stop")) {
                restartMode = false;
                Utils.autoSendMsg(event, "已取消重启计划");
            } else if (cmd[1].equalsIgnoreCase("now")) {
                Utils.autoSendMsg(event, "开始重启");
                Utils.runCmdFile("cmd /c start /b MiraiOK.bat");
                System.exit(0);
            } else {
                Utils.autoSendMsg(event, "未知子命令\n请使用 " + cmd[0] + " help 查看帮助");
            }
        }
    }
}
