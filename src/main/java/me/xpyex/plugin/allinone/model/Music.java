package me.xpyex.plugin.allinone.model;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;

@SuppressWarnings("unused")
public class Music extends Model {
    private static final Executor EXEC = Executors.newFixedThreadPool(8);
    public static final Map<String, BiConsumer<MessageEvent, String[]>> MUSIC_CMDS = new ConcurrentHashMap<>();
    private static final Map<String, MusicSource> SOURCES = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final Map<String, MusicCardProvider> CARDS = new ConcurrentHashMap<>();
    private static final HashMap<Long, Integer> GROUP_LIMITER = new HashMap<>();

    static {
        SOURCES.put("QQ音乐", new QQMusicSource());
        SOURCES.put("网易", new NetEaseMusicSource());
        SOURCES.put("酷狗", new KugouMusicSource());
        CARDS.put("MiraiCard", new MiraiCardProvider());
        HttpURLConnection.setFollowRedirects(true);
        MUSIC_CMDS.put("#点歌", makeTemplate("QQ音乐", "MiraiCard"));
        MUSIC_CMDS.put("#网易", makeTemplate("网易", "MiraiCard"));
        MUSIC_CMDS.put("#酷狗", makeTemplate("酷狗", "MiraiCard"));

        GROUP_LIMITER.put(741053728L, 0);
    }

    @Override
    public void register() {
        listenEvent(MessageEvent.class, (event) -> {
            String[] args = Util.getPlainText(event.getMessage()).split(" ");
            BiConsumer<MessageEvent, String[]> exec = MUSIC_CMDS.get(args[0]);
            if (exec != null)
                exec.accept(event, args);
        });
        runTaskTimer(() -> {
            GROUP_LIMITER.replaceAll((k, v) -> 0);
        }, 60 * 60, 60 * 60);
    }

    public static BiConsumer<MessageEvent, String[]> makeTemplate(String source, String card) {
        MusicCardProvider cb = CARDS.get(card);
        if (cb == null)
            throw new IllegalArgumentException("card template not exists");
        MusicSource mc = SOURCES.get(source);
        if (mc == null)
            throw new IllegalArgumentException("music source not exists");
        return (event, args) -> {
            if (Util.isGroupEvent(event) && GROUP_LIMITER.containsKey(((GroupMessageEvent) event).getGroup().getId()) && GROUP_LIMITER.get(((GroupMessageEvent) event).getGroup().getId()) > 20) {
                Util.autoSendMsg(event, "抱歉，根据管理员设定，该群每个小时仅允许点歌20首\n当前已超出限制");
                return;
            }
            String sn;
            try {
                sn = URLEncoder.encode(String.join(" ", Arrays.copyOfRange(args, 1, args.length)), "UTF-8");
            } catch (UnsupportedEncodingException ignored) {
                return;
            }
            EXEC.execute(() -> {
                try {
                    Util.autoSendMsg(event, cb.process(mc.get(sn), Util.getRealSender(event)));
                    if (Util.isGroupEvent(event)) {
                        GroupMessageEvent gEvent = (GroupMessageEvent) event;
                        if (GROUP_LIMITER.containsKey(gEvent.getGroup().getId())) {
                            GROUP_LIMITER.put(gEvent.getGroup().getId(), GROUP_LIMITER.get(gEvent.getGroup().getId()) + 1);
                        }
                    }
                } catch (Throwable e) {
                    Util.autoSendMsg(event, "无法找到歌曲");
                    Util.handleException(e, false);
                }
            });
        };
    }

}