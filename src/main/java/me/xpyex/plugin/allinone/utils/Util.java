package me.xpyex.plugin.allinone.utils;

import java.util.Calendar;
import me.xpyex.plugin.allinone.commands.CommandsList;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageContent;
import net.mamoe.mirai.message.data.PlainText;

public class Util {
    public static Contact getRealSender(MessageEvent event) {
        if (isGroupEvent(event)) {
            return ((GroupMessageEvent) event).getGroup();
        } else {
            return event.getSender();
        }
    }

    public static String getPlainText(MessageChain msg) {
        MessageContent pt = msg.get(PlainText.Key);
        if (pt == null) {
            return "";
        }
        return pt.contentToString().trim();
    }

    public static void setNameCard(GroupMessageEvent event, String Namecard) {
        event.getGroup().get(event.getSender().getId()).setNameCard(Namecard);
        //
    }

    public static boolean isGroupEvent(MessageEvent event) {
        return (event instanceof GroupMessageEvent);
        //
    }

    public static void autoSendMsg(MessageEvent event, String msg) {
        autoSendMsg(event, new PlainText(msg).plus(""));
        //
    }

    public static void autoSendMsg(MessageEvent event, Message msg) {
        getRealSender(event).sendMessage(msg);
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

    public static void sendFriendMsg(Long QQ, Message Msg) {
        getBot().getFriend(QQ).sendMessage(Msg);
        //
    }

    public static void sendGroupMsg(Long QG, Message Msg) {
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

    public static void sendMsgToOwner(Message msg) {
        sendFriendMsg(1723275529L, msg);
        //
    }

    public static void handleException(Throwable e) {
        e.printStackTrace();
        Throwable t = e;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        sendMsgToOwner("在执行 " + t.getStackTrace()[0].getClassName() + " 类的方法 " +
                t.getStackTrace()[0].getMethodName() + " 时出错: " +
                t + "\n" +
                "该代码位于该类的第 " + t.getStackTrace()[0].getLineNumber() + " 行");
    }
}
