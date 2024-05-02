package me.xpyex.plugin.parrot.mirai.modulecode.music.cardprovider;

import java.util.HashMap;
import java.util.Map;
import me.xpyex.plugin.parrot.mirai.modulecode.music.MusicCardProvider;
import me.xpyex.plugin.parrot.mirai.modulecode.music.MusicInfo;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MusicKind;
import net.mamoe.mirai.message.data.MusicShare;

public class MiraiCardProvider implements MusicCardProvider {
    private static final Map<Long, MusicKind> appIdMappings = new HashMap<>();
    private static final MusicCardProvider def = new XMLCardProvider();

    static {
        for (MusicKind mk : MusicKind.values())
            appIdMappings.put(mk.getAppId(), mk);
    }

    @Override
    public Message process(MusicInfo mi, Contact ct) throws Exception {
        MusicKind omk = appIdMappings.get(mi.appid);
        if (omk != null)
            return new MusicShare(omk, mi.title, mi.desc, mi.jurl, mi.purl, mi.murl);
        return def.process(mi, ct);
    }
}
