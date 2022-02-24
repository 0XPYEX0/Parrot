package me.xpyex.plugin.allinone.functions.informs;

import me.xpyex.plugin.allinone.Utils;
import net.mamoe.mirai.event.events.GroupMessageEvent;

public class Repeater {
    static String enableList;
    public static void load() {
        enableList = "";
    }
    public static String[] getEnableList() {
        return enableList.split(", ");
    }
    public static void addToEnableList(String target) {
        enableList = enableList + target + ", ";
    }
    public static void removeFromEnableList(String target) {
        enableList = enableList.replace(target + ", ", "");
    }
    public static void Execute(GroupMessageEvent event) {
        String[] cmds = Utils.getNormalText(event.getMessage()).split(" ");
        if (cmds[0].equalsIgnoreCase("/repeater") || cmds[0].equalsIgnoreCase("#repeater")) {
            if (!Utils.canExecute(event)) {
                Utils.autoSendMsg(event, "该命令仅允许管理员使用");
                return;
            }
            if (cmds.length < 3 || cmds[1].equalsIgnoreCase("help")) {
                if ((cmds.length==2) && (cmds[1].equalsIgnoreCase("clear") || cmds[1].equalsIgnoreCase("clean")))  {
                    enableList = "";
                    event.getGroup().sendMessage("已重置复读列表");
                    return;
                }
                Utils.autoSendMsg(event, cmds[0] + " add GroupId\n" + cmds[0] + " remove GroupId\n" + cmds[0] + " clear");
                return;
            }
            String willDeal = "";
            if (cmds[2].equalsIgnoreCase("this")) {
                willDeal = event.getGroup().getId() + "";
            } else {
                try {
                    willDeal = Long.parseLong(cmds[2]) + "";
                } catch (Throwable e) {
                    event.getGroup().sendMessage("错误了嗷");
                    return;
                }
            }
            if (cmds[1].equalsIgnoreCase("add")) {
                addToEnableList(willDeal);
                event.getGroup().sendMessage("已将 " + cmds[2] + " 加入复读列表");
                return;
            }
            if (cmds[1].equalsIgnoreCase("remove") || cmds[1].equalsIgnoreCase("rem")) {
                removeFromEnableList(willDeal);
                event.getGroup().sendMessage("已将 " + cmds[2] + " 移出复读列表");
                return;
            }
            return;
        }
        if (Utils.isCmdMsg(event.getMessage())) {
            return;
        }
        for (String check : getEnableList()) {
            if (check.equals(event.getGroup().getId() + "")) {
                Utils.autoSendMsg(event, event.getMessage());
                return;
            }
        }
    }
}
