package me.xpyex.plugin.allinone.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import me.xpyex.plugin.allinone.core.CommandsList;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageContent;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

public class Util {
    public static final SimpleDateFormat FORMATTER = new SimpleDateFormat("HH:mm:ss");

    public static Contact getRealSender(MessageEvent event) {
        if (isGroupEvent(event)) return ((GroupMessageEvent) event).getGroup();

        return event.getSender();
    }

    public static String getPlainText(MessageChain msg) {
        MessageContent pt = msg.get(PlainText.Key);

        if (pt == null) return "";

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
        if (msg == null) return;
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

    public static void runCmd(String cmd) {
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
        return FORMATTER.format(new Date());
        //
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
        sendMsgToOwner("在执行 " + e.getStackTrace()[0].getClassName() + " 类的方法 " +
                e.getStackTrace()[0].getMethodName() + " 时出错: " +
                e + "\n" +
                "该代码位于该类的第 " + e.getStackTrace()[0].getLineNumber() + " 行");
    }

    public static ExternalResource getImage(URL url) throws Exception {
        URLConnection uc = url.openConnection();
        InputStream in = uc.getInputStream();
        byte[] bytes = readAll(in);
        return ExternalResource.create(bytes);
    }

    public static ExternalResource getImage(String url) throws Exception {
        return getImage(new URL(url));
        //
    }

    public static byte[] readAll(InputStream i) throws IOException {
        ByteArrayOutputStream ba = new ByteArrayOutputStream(16384);
        byte[] data = new byte[4096];
        int nRead;
        while ((nRead = i.read(data, 0, data.length)) != -1) {
            ba.write(data, 0, nRead);
        }
        return ba.toByteArray();
    }
}
