package me.xpyex.plugin.allinone.functions.music;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import me.xpyex.plugin.allinone.Main;
import me.xpyex.plugin.allinone.functions.music.cardprovider.MiraiCardProvider;
import me.xpyex.plugin.allinone.functions.music.musicsource.KugouMusicSource;
import me.xpyex.plugin.allinone.functions.music.musicsource.NetEaseMusicSource;
import me.xpyex.plugin.allinone.functions.music.musicsource.QQMusicSource;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MessageEvent;

public class MiraiMusic {
    public static boolean enable = true;
    private static final Executor EXEC = Executors.newFixedThreadPool(8);
    public static Map<String, BiConsumer<MessageEvent, String[]>> commands = new ConcurrentHashMap<>();
    static Map<String, MusicSource> sources = Collections.synchronizedMap(new LinkedHashMap<>());
    static Map<String, MusicCardProvider> cards = new ConcurrentHashMap<>();
    public static void load() {
        sources.put("QQ音乐", new QQMusicSource());
        sources.put("网易", new NetEaseMusicSource());
        sources.put("酷狗", new KugouMusicSource());
        cards.put("MiraiCard", new MiraiCardProvider());
        HttpURLConnection.setFollowRedirects(true);
        commands.put("#点歌", makeTemplate("QQ音乐", "MiraiCard"));
        commands.put("#网易", makeTemplate("网易", "MiraiCard"));
        commands.put("#酷狗", makeTemplate("酷狗", "MiraiCard"));

        GlobalEventChannel.INSTANCE.registerListenerHost(new SimpleListenerHost() {
            @EventHandler
            public void Execute(MessageEvent event) {
                if (!getEnableMode()) {
                    return;
                }
                String[] args = Util.getPlainText(event.getMessage()).split(" ");
                BiConsumer<MessageEvent, String[]> exec = commands.get(args[0]);
                if (exec != null)
                    exec.accept(event, args);
            }
        });
        Main.LOGGER.info(" Music模块已加载");
    }

    public static void setEnableMode(boolean a) {
        enable = a;
        //
    }

    public static boolean getEnableMode() {
        return enable;
        //
    }

    public static BiConsumer<MessageEvent, String[]> makeTemplate(String source, String card) {
        MusicCardProvider cb = cards.get(card);
        if(cb == null)
            throw new IllegalArgumentException("card template not exists");
        MusicSource mc = sources.get(source);
        if(mc == null)
            throw new IllegalArgumentException("music source not exists");
        return (event, args) -> {
            String sn;
            try {
                sn = URLEncoder.encode(String.join(" ", Arrays.copyOfRange(args, 1, args.length)), "UTF-8");
            } catch (UnsupportedEncodingException ignored) {
                return;
            }
            EXEC.execute(() -> {
                try {
                    Util.autoSendMsg(event, cb.process(mc.get(sn), Util.getRealSender(event)));
                } catch (Throwable e) {
                    Util.autoSendMsg(event, "无法找到歌曲");
                    Util.handleException(e);
                }
            });
        };
    }

}