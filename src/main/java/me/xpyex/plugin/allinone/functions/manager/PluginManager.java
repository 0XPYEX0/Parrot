package me.xpyex.plugin.allinone.functions.manager;

import me.xpyex.plugin.allinone.Utils;
import me.xpyex.plugin.allinone.functions.botchecker.BotChecker;
import me.xpyex.plugin.allinone.functions.music.MiraiMusic;

import net.mamoe.mirai.event.events.MessageEvent;

public class PluginManager {
    public static void Execute(MessageEvent event) {
        String[] cmd = Utils.getNormalText(event.getMessage()).split(" ");
        if (cmd[0].equalsIgnoreCase("/plugin") || cmd[0].equalsIgnoreCase("/pl")) {
            if (!Utils.canExecute(event)) {
                Utils.autoSendMsg(event, "该命令仅允许管理员使用");
                return;
            }
            if (cmd.length==1 || cmd[1].equalsIgnoreCase("help")) {
                Utils.autoSendMsg(event, cmd[0] + " enable <模块> - 启用某模块\n" + cmd[0] + " disable <模块> - 禁用某模块\n" + cmd[0] + " list - 列出可操作模块列表");
                return;
            }
            if (cmd[1].equalsIgnoreCase("enable") || cmd[1].equalsIgnoreCase("load")) {
                if (cmd[2].equalsIgnoreCase("music")) {
                    MiraiMusic.setEnableMode(true);
                    Utils.autoSendMsg(event, "已启用Music模块");
                    return;
                }
                if (cmd[2].equalsIgnoreCase("JoinAccepter")) {
                    JoinAcceptor.setEnableMode(true);
                    Utils.autoSendMsg(event, "已启用JoinAccepter模块");
                    return;
                }
                if (cmd[2].equalsIgnoreCase("BotChecker")) {
                    BotChecker.setEnableMode(true);
                    Utils.autoSendMsg(event, "已启用BotChecker模块");
                    return;
                }
                Utils.autoSendMsg(event, cmd[0] + " list - 查看可操作模块列表");
                return;
            }
            if (cmd[1].equalsIgnoreCase("disable") || cmd[1].equalsIgnoreCase("unload")) {
                if (cmd[2].equalsIgnoreCase("music")) {
                    MiraiMusic.setEnableMode(false);
                    Utils.autoSendMsg(event, "已禁用Music模块");
                    return;
                }
                if (cmd[2].equalsIgnoreCase("JoinAccepter")) {
                    JoinAcceptor.setEnableMode(false);
                    Utils.autoSendMsg(event, "已禁用JoinAccepter模块");
                    return;
                }
                if (cmd[2].equalsIgnoreCase("BotChecker")) {
                    BotChecker.setEnableMode(false);
                    Utils.autoSendMsg(event, "已禁用BotChecker模块");
                    return;
                }
                Utils.autoSendMsg(event, cmd[0] + " list - 查看可操作模块列表");
                return;
            }
            if (cmd[1].equalsIgnoreCase("list")) {
                Utils.autoSendMsg(event, "BotChecker\nJoinAcceptor\nMusic\nQiongJu");
                return;
            }
            Utils.autoSendMsg(event, "未知子命令\n使用 " + cmd[0] + " help 查看帮助");
        }
    }
}
