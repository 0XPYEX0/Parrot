package me.xpyex.plugin.parrot.mirai.modulecode.music.cardprovider;

import cn.evole.onebot.sdk.enums.ActionPathEnum;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import me.xpyex.plugin.parrot.mirai.modulecode.music.MusicCardProvider;
import me.xpyex.plugin.parrot.mirai.modulecode.music.MusicInfo;
import me.xpyex.plugin.parrot.mirai.modulecode.music.MusicUtils;
import me.xpyex.plugin.parrot.mirai.utils.Util;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.LightApp;
import net.mamoe.mirai.message.data.Message;
import top.mrxiaom.overflow.contact.RemoteBot;

public class StructMsgCardProvider implements MusicCardProvider {
    private static final MessageDigest MD5;

    static {
        try {
            MD5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message process(MusicInfo mi, Contact ct) throws Exception {
        JSONObject config = new JSONObject();
        int token = JSONUtil.parseObj(RemoteBot.getAsRemoteBot(Util.getBot()).executeAction(ActionPathEnum.GET_CSRF_TOKEN.getPath(), null)).getJSONObject("data").getInt("token");
        MD5.update((token + "").getBytes(StandardCharsets.UTF_8));
        String tokenMd5 = HexFormat.of().formatHex(MD5.digest());
        //如何计算String的MD5值？
        config.set("ctime", MusicUtils.getTime() / 1000)
            .set("forward", 1)
            .set("token", tokenMd5)
            .set("type", "normal");

        JSONObject extra = new JSONObject();
        extra.set("app_type", 1)
            .set("appid", 101097681)
            .set("uin", Util.getBot().getId());

        JSONObject music = new JSONObject();
        music.set("action", "")
            .set("android_pkg_name", "")
            .set("app_type", 1)
            .set("appid", 101097681)
            .set("ctime", MusicUtils.getTime() / 1000)
            .set("desc", mi.desc)
            .set("jumpUrl", mi.jurl)
            .set("musicUrl", mi.murl)
            .set("preview", mi.purl)
            .set("sourceMsgId", "0")
            .set("source_icon", mi.icon)
            .set("source_url", "")
            .set("tag", mi.source)
            .set("title", mi.title)
            .set("uin", Util.getBot().getId());

        JSONObject out = new JSONObject();
        out.set("app", "com.tencent.structmsg")
            .set("config", config)
            .set("extra", extra)
            .set("meta", new JSONObject().set("music", music))
            .set("view", "music")
            .set("ver", "0.0.0.1")
            .set("prompt", "[音乐]" + mi.title);

        return new LightApp(out.toString());
    }
}
