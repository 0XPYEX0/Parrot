package me.xpyex.plugin.allinone.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import me.xpyex.plugin.allinone.Main;
import me.xpyex.plugin.allinone.core.CommandsList;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageContent;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

public class Util {
    public static final File cacheFolder = new File("cache");
    private static final File imageCacheFolder = new File(cacheFolder, "Images");
    private static final HashMap<String, File> fileCaches = new HashMap<>();
    public static final SimpleDateFormat FORMATTER = new SimpleDateFormat("HH:mm:ss");

    static {
        cacheFolder.mkdirs();
        imageCacheFolder.mkdirs();
        cacheFolder.deleteOnExit();
    }

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
        //
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
        return FORMATTER.format(new Date());
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


    public static URLConnection getConn(String URL) {
        URLConnection conn = null;
        try {
            conn = new URL(URL).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static void downloadFile(String URL, File f) throws Exception {
        URLConnection conn = getConn(URL);
        ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream());
        FileChannel fc = FileChannel.open(f.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        fc.transferFrom(rbc, 0, Long.MAX_VALUE);
        fc.close();
        rbc.close();
        Main.LOGGER.info("文件 " + f.getName() + " 下载完成");
    }

    public static Image getUrlImage(String url) throws Exception {
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }
        if (!imageCacheFolder.exists()) {
            imageCacheFolder.mkdirs();
        }
        File cacheImage = getUrlImageFile(url);
        cacheImage.createNewFile();
        downloadFile(url, cacheImage);
        ExternalResource resource = ExternalResource.create(cacheImage);
        Image image = getBot().getFriend(1723275529L).uploadImage(resource);
        resource.close();
        return image;
    }

    public static File getUrlImageFile(String url) {
        String fileName = UUID.randomUUID().toString();
        if (!fileCaches.containsKey(url)) {
            fileCaches.put(url, new File(imageCacheFolder, fileName));
        }
        return fileCaches.get(url);
    }
}
