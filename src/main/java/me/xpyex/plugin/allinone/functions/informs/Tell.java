package me.xpyex.plugin.allinone.functions.informs;

import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Stranger;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.PlainText;

public class Tell {
    public static void Execute(MessageEvent event) {
        String[] cmd = Util.getPlainText(event.getMessage()).split(" ");
        if (cmd[0].equals("test")) {
            Util.autoSendMsg(event, event.getMessage().contentToString());
            return;
        }
        if (cmd[0].equals("/tell") || cmd[0].equals("#tell")) {
            if (cmd.length == 1 || cmd[1].equals("help")) {
                Util.autoSendMsg(event, cmd[0] + " <friend||group||stranger> <ID> <Msg>");
                return;
            }
            if (event.getMessage().contentToString().split(" ").length < 4) {
                Util.autoSendMsg(event, "参数不足\n使用 " + cmd[0] + " 查看帮助");
                return;
            }
            if (cmd[2].equalsIgnoreCase("this")) {
                if (cmd[1].equalsIgnoreCase("group")) {
                    cmd[2] = ((GroupMessageEvent)event).getGroup().getId() + "";
                } else {
                    cmd[2] = event.getSender().getId() + "";
                }
            }
            String getMsg = Util.getPlainText(event.getMessage()).replace(cmd[0] + " ", "");
            getMsg = getMsg.replace(cmd[1] + " ", "");
            getMsg = getMsg.replace(cmd[2] + " ", "");
            if (cmd[1].equalsIgnoreCase("friend")) {
                Friend friend;
                try {
                    friend = Util.getBot().getFriend(Long.parseLong(cmd[2]));
                } catch (Exception e) {
                    Util.autoSendMsg(event, "无法找到好友\n请检查输入的QQ号");
                    return;
                }
                assert friend != null;
                friend.sendMessage(getMsg);
                Util.autoSendMsg(event, new PlainText("已将\n\n").plus(getMsg).plus("\n\n发送至好友\n" + cmd[2]));
                return;
            }
            if (cmd[1].equalsIgnoreCase("group")) {
                Group group;
                try {
                    group = Util.getBot().getGroup(Long.parseLong(cmd[2]));
                } catch (Exception e) {
                    Util.autoSendMsg(event, "无法找到群组\n请检查输入的群号");
                    return;
                }
                assert group != null;
                group.sendMessage(getMsg);
                Util.autoSendMsg(event, new PlainText("已将\n\n").plus(getMsg).plus("\n\n发送至群组\n" + cmd[2]));
                return;
            }
            if (cmd[1].equalsIgnoreCase("stranger")) {
                Stranger target;
                try {
                    target = Util.getBot().getStranger(Long.parseLong(cmd[2]));
                    assert target != null;
                    target.sendMessage(getMsg);
                    Util.autoSendMsg(event, new PlainText("已将\n\n").plus(getMsg).plus("\n\n发送至好友\n" + cmd[2]));
                    return;
                } catch (Throwable e) {
                    Util.autoSendMsg(event, "无法找到临时会话\n请检查输入的QQ号");
                    return;
                }
            }
            Util.autoSendMsg(event, "参数错误\n使用 " + cmd[0] + " 查看帮助");
            return;
        }
        if (cmd[0].equals("/say") || cmd[0].equals("#say")) {
            if (cmd.length == 1 || cmd[1].equals("help")) {
                Util.autoSendMsg(event, cmd[0] + " Msg");
                return;
            }
            String getMsg = Util.getPlainText(event.getMessage()).replace(cmd[0] + " ", "");
            Util.autoSendMsg(event, getMsg);
        }
    }
}
