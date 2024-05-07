package me.xpyex.plugin.parrot.mirai.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.events.FriendEvent;
import net.mamoe.mirai.event.events.GroupEvent;

public class Util {
    public static Long OWNER_ID;

    public static boolean isGroupEvent(Event event) {
        return (event instanceof GroupEvent);
        //
    }

    public static boolean isFriendEvent(Event event) {
        return (event instanceof FriendEvent);
        //
    }

    public static Bot getBot() {
        return Bot.getInstances().get(0);
        //
    }

    public static Friend getOwner() {
        return getBot().getFriend(OWNER_ID);
        //
    }

    public static void runCmd(String cmd) {
        Runtime rt = Runtime.getRuntime();
        Process ps = null;
        try {
            ps = rt.exec(cmd);
            ps.waitFor();
        } catch (Throwable e) {
            ExceptionUtil.handleException(e, true, null, null);
        }
        assert ps != null;
        int i = ps.exitValue();
        ps.destroy();
    }

    public static byte[] readAll(InputStream i) throws IOException {
        ByteArrayOutputStream ba = new ByteArrayOutputStream(32768);
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = i.read(data, 0, data.length)) != -1) {
            ba.write(data, 0, nRead);
        }
        return ba.toByteArray();
    }

}
