package me.xpyex.plugin.allinone.utils;

import me.xpyex.plugin.allinone.commands.CommandsList;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;

import java.util.Calendar;

public class Util {
    public static String getPlainText(MessageChain msg) {
        MessageContent pt = msg.get(PlainText.Key);
        if (pt == null) {
            return "";
        }
        return pt.contentToString().trim();
    }
    public static void setNameCard(GroupMessageEvent event, String Namecard) {
        event.getGroup().get(event.getSender().getId()).setNameCard(Namecard);
    }
    public static boolean isGroupEvent(MessageEvent event) {
        return (event instanceof GroupMessageEvent);
    }
    public static void autoSendMsg(MessageEvent event, String Msg) {
        autoSendMsg(event, new PlainText(Msg).plus(""));
    }
    public static void autoSendMsg(MessageEvent event, MessageChain Msg) {
        if (isGroupEvent(event)) {
            ((GroupMessageEvent) event).getGroup().sendMessage(Msg);
        } else {
            event.getSender().sendMessage(Msg);
        }
    }
    public static boolean isFriendEvent(MessageEvent event) {
        return (event instanceof FriendMessageEvent);
        //
    }

    public static Bot getBot() {
        return Bot.getInstance(1393779517L);
        //
    }

    public static void sendFriendMsg(Long QQ, String Msg) {
        getBot().getFriend(QQ).sendMessage(Msg);
        //
    }

    public static void sendFriendMsg(Long QQ, MessageChain Msg) {
        getBot().getFriend(QQ).sendMessage(Msg);
        //
    }

    public static void sendGroupMsg(Long QG, MessageChain Msg) {
        getBot().getGroup(QG).sendMessage(Msg);
        //
    }

    public static boolean isCmdMsg(MessageChain msg) {
        String[] cmd = getPlainText(msg).split(" ");
        return CommandsList.isCmd(cmd[0]);
    }

    public static boolean canExecute(MessageEvent event) {
        return ((event.getSender().getId() == 1723275529L) || (isGroupEvent(event) && ((GroupMessageEvent)event).getPermission().getLevel() >= 1));
        //
    }

    public static void runCmdFile(String cmd) {
        Runtime rt = Runtime.getRuntime();
        Process ps = null;
        try {
            ps = rt.exec(cmd);
            ps.waitFor();
        } catch (Throwable e) {
            Util.handleException(e);
        }
        assert ps != null;
        int i = ps.exitValue();
        ps.destroy();
    }

    public static String getTimeOfNow() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        int second = Calendar.getInstance().get(Calendar.SECOND);
        String sHour = hour + "";
        if (hour < 10) {
            sHour = "0" + hour;
        }
        String sMinute = minute + "";
        if (minute < 10) {
            sMinute = "0" + minute;
        }
        String sSecond = second + "";
        if (second < 10) {
            sSecond = "0" + second;
        }
        return sHour + ":" + sMinute + ":" + sSecond;
    }

    public static void sendMsgToOwner(String msg) {
        sendMsgToOwner(new PlainText(msg).plus(""));
        //
    }

    public static void sendMsgToOwner(MessageChain msg) {
        sendFriendMsg(1723275529L, msg);
        //
    }

    public static void handleException(Throwable e) {
        e.printStackTrace();
        sendMsgToOwner("在执行 " + e.getStackTrace()[0].getClassName() + " 类的方法 " +
                e.getStackTrace()[0].getMethodName() + " 时出错: " +
                e + "\n" +
                "该代码位于该类的第 " + e.getStackTrace()[0].getLineNumber() + " 行");
    }
}
