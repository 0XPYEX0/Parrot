package me.xpyex.plugin.allinone.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import me.xpyex.plugin.allinone.api.TryCallable;
import me.xpyex.plugin.allinone.api.TryRunnable;
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
            ExceptionUtil.handleException(e);
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

    public static <T> T getOrDefault(T value, T defaulted) {
        return value == null ? defaulted : value;
        //
    }

    /**
     * 当需要获取值，而方法可能抛出错误时，使用该方法重复获取.
     *
     * @param callable    获取值的方法体
     * @param repeatTimes 重复获取的次数，超出次数则返回null. 填入0则不限制获取次数，将不断尝试直至取到值. 填入0时请注意安全，避免堵塞主线程！
     * @return 返回需要获取的值
     */
    public static <T> T repeatIfError(TryCallable<T> callable, long repeatTimes) {
        return repeatIfError(callable, repeatTimes, 0);
    }

    /**
     * 当需要获取值，而方法可能抛出错误时，使用该方法重复获取.
     *
     * @param callable    获取值的方法体
     * @param repeatTimes 重复获取的次数，超出次数则返回null. 填入0则不限制获取次数，将不断尝试直至取到值. 填入0时请注意安全，避免堵塞主线程！
     * @param waitMillis  当出现错误时，等待多久(单位: 毫秒)再次执行. 若为0则不等待. 填入非0时请注意安全，避免在主线程等待.
     * @return 返回需要获取的值
     */
    public static <T> T repeatIfError(TryCallable<T> callable, long repeatTimes, long waitMillis) {
        if (repeatTimes == 0) {
            while (true) {
                try {
                    return callable.call();
                } catch (Throwable ignored) {
                    if (waitMillis > 0)  {
                        try {
                            Thread.sleep(waitMillis);
                        } catch (Throwable ignored1) {
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < repeatTimes; i++) {
                try {
                    return callable.call();
                } catch (Throwable ignored) {
                    if (waitMillis > 0)  {
                        try {
                            Thread.sleep(waitMillis);
                        } catch (Throwable ignored1) {
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 当需要执行方法，而方法有可能抛出错误时，使用该方法重复执行
     *
     * @param r           需要执行的方法体
     * @param repeatTimes 重复获取的次数，超出次数则返回null. 填入0则不限制获取次数，将不断尝试直至取到值. 填入0时请注意安全，避免堵塞主线程！
     */
    public static void repeatIfError(TryRunnable r, long repeatTimes) {
        repeatIfError(() -> {
            r.run();
            return null;
        }, repeatTimes);
    }
}
