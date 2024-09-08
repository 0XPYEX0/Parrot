package me.xpyex.plugin.parrot.mirai.utils;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

public class MsgUtil {
    private static final Message EMPTY_MSG = new PlainText("");

    public static Contact getRealSender(MessageEvent event) {
        if (Util.isGroupEvent(event)) return ((GroupMessageEvent) event).getGroup();

        return event.getSender();
    }

    @SuppressWarnings("unchecked")
    public static <C extends Contact> C getRealSender(MessageEvent event, Class<C> returnType) {
        return (C) getRealSender(event);
        //
    }

    public static String getPlainText(Message msg) {
        if (msg == null) return "";

        return msg.contentToString();
    }

    public static void sendMsg(Contact contact, String msg) {
        if (msg == null) {
            return;
        }
        sendMsg(contact, new PlainText(msg));
    }

    public static void sendMsg(Contact contact, Message msg) {
        if (contact == null || msg == null) {
            return;
        }
        if (contact instanceof Group group) {
            if (group.getSettings().isMuteAll() && group.getBotPermission() == MemberPermission.MEMBER) {
                return;
            }
            if (group.getBotAsMember().isMuted()) {
                return;
            }
        }
        contact.sendMessage(msg);
    }

    public static void sendMsgToOwner(String msg) {
        sendMsgToOwner(new PlainText(msg).plus(""));
        //
    }

    public static void sendMsgToOwner(Message msg) {
        if (Util.OWNER_ID == null) {
            return;
        }
        Util.getOwner().sendMessage(msg);
    }

    public static ExternalResource getImage(URL url) throws Exception {
        URLConnection uc = url.openConnection();
        InputStream in = uc.getInputStream();
        byte[] bytes = Util.readAll(in);
        return ExternalResource.create(bytes);
    }

    public static ExternalResource getImage(String url) throws Exception {
        return getImage(new URL(url));
        //
    }

    public static Message getEmptyMessage() {
        return EMPTY_MSG;
        //
    }

    public static ForwardMessageBuilder getForwardMsgBuilder(Contact contact) {
        return new ForwardMessageBuilder(contact);
        //
    }

    public static ForwardMessageBuilder getForwardMsgBuilder(MessageEvent event) {
        return getForwardMsgBuilder(getRealSender(event));
        //
    }
}
