package me.xpyex.plugin.allinone.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageContent;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.Nullable;

public class Util {
    public static final SimpleDateFormat FORMATTER = new SimpleDateFormat("HH:mm:ss");
    private static final Message EMPTY_MSG = new PlainText("");
    public static Long OWNER_ID;

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
        return Bot.getInstances().get(0);
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
        if (OWNER_ID == null) {
            return;
        }
        getOwner().sendMessage(msg);
    }

    public static Friend getOwner() {
        return getBot().getFriend(OWNER_ID);
        //
    }

    public static void handleException(Throwable e) {
        handleException(e, true);
        //
    }

    public static void handleException(Throwable e, boolean noticeOwner) {
        handleException(e, noticeOwner, null);
        //
    }

    public static void handleException(Throwable e, @Nullable Event event) {
        handleException(e, true, event);
        //
    }

    public static void handleException(Throwable e, boolean noticeOwner, @Nullable Event event) {
        e.printStackTrace();
        String eventCause;
        if (event != null) {
            if (event instanceof MessageEvent) {
                if (event instanceof GroupMessageEvent) {
                    eventCause = "群聊-" + ((GroupMessageEvent) event).getGroup().getId() + "";
                } else {
                    eventCause = "私聊-" + ((MessageEvent) event).getSender().getId();
                }
            } else {
                eventCause = "事件-" + event.getClass().getSimpleName() + "\n详细信息: " + event;
            }
        } else {
            eventCause = "未知事件";
        }
        if (noticeOwner) {
            sendMsgToOwner("在执行 " + e.getStackTrace()[0].getClassName() + " 类的方法 " +
                               e.getStackTrace()[0].getMethodName() + " 时出错: " +
                               e + "\n" +
                               "该代码位于该类的第 " + e.getStackTrace()[0].getLineNumber() + " 行" +
                               "\n" + "该错误由 " + eventCause + " 触发");
        }
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

    public static Message getEmptyMessage() {
        return EMPTY_MSG;
        //
    }

    public static <T> T getOrDefault(T value, T defaulted) {
        return value == null ? defaulted : value;
        //
    }
}
