package me.xpyex.plugin.allinone.modelcode.music.cardprovider;

import java.util.HashMap;
import java.util.Map;
import me.xpyex.plugin.allinone.modelcode.music.MusicCardProvider;
import me.xpyex.plugin.allinone.modelcode.music.MusicInfo;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MusicKind;
import net.mamoe.mirai.message.data.MusicShare;

public class MiraiCardProvider implements MusicCardProvider {
    private static Map<Long, MusicKind> appidMappings = new HashMap<>();
    private static MusicCardProvider def = new XMLCardProvider();

    static {
        for (MusicKind mk : MusicKind.values()) {
            appidMappings.put(mk.getAppId(), mk);
        }
    }

    @Override
    public Message process(MusicInfo mi, Contact ct) throws Exception {
        MusicKind omk = appidMappings.get(mi.appid);
        if (omk != null)
            return new MusicShare(omk, mi.title, mi.desc, mi.jurl, mi.purl, mi.murl);
        return def.process(mi, ct);
    }

}
