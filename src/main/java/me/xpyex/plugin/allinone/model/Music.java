package me.xpyex.plugin.allinone.model;

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
import me.xpyex.plugin.allinone.core.Model;
import me.xpyex.plugin.allinone.modelcode.music.MusicCardProvider;
import me.xpyex.plugin.allinone.modelcode.music.MusicSource;
import me.xpyex.plugin.allinone.modelcode.music.cardprovider.MiraiCardProvider;
import me.xpyex.plugin.allinone.modelcode.music.musicsource.KugouMusicSource;
import me.xpyex.plugin.allinone.modelcode.music.musicsource.NetEaseMusicSource;
import me.xpyex.plugin.allinone.modelcode.music.musicsource.QQMusicSource;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.event.events.MessageEvent;

public class Music extends Model {
    private static final Executor EXEC = Executors.newFixedThreadPool(8);
    public static final Map<String, BiConsumer<MessageEvent, String[]>> MUSIC_CMDS = new ConcurrentHashMap<>();
    private static final Map<String, MusicSource> SOURCES = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final Map<String, MusicCardProvider> CARDS = new ConcurrentHashMap<>();
    static {
        SOURCES.put("QQ音乐", new QQMusicSource());
        SOURCES.put("网易", new NetEaseMusicSource());
        SOURCES.put("酷狗", new KugouMusicSource());
        CARDS.put("MiraiCard", new MiraiCardProvider());
        HttpURLConnection.setFollowRedirects(true);
        MUSIC_CMDS.put("#点歌", makeTemplate("QQ音乐", "MiraiCard"));
        MUSIC_CMDS.put("#网易", makeTemplate("网易", "MiraiCard"));
        MUSIC_CMDS.put("#酷狗", makeTemplate("酷狗", "MiraiCard"));
    }

    @Override
    public void register() {
        listenEvent(MessageEvent.class, (event) -> {
            String[] args = Util.getPlainText(event.getMessage()).split(" ");
            BiConsumer<MessageEvent, String[]> exec = MUSIC_CMDS.get(args[0]);
            if (exec != null)
                exec.accept(event, args);
        });
    }

    public static BiConsumer<MessageEvent, String[]> makeTemplate(String source, String card) {
        MusicCardProvider cb = CARDS.get(card);
        if (cb == null)
            throw new IllegalArgumentException("card template not exists");
        MusicSource mc = SOURCES.get(source);
        if (mc == null)
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