package me.xpyex.plugin.allinone.modelcode.music;

public class MusicInfo {
    public final String title;
    public final String desc;
    public final String purl;
    public final String murl;
    public final String jurl;
    public final String source;
    public final String icon;
    public final long appid;

    public MusicInfo(final String title, final String desc, final String purl, final String murl, final String jurl, final String source, final String icon, final long appid) {
        this.title = title;
        this.desc = desc;
        this.purl = purl;
        this.murl = murl;
        this.jurl = jurl;
        this.source = source;
        this.icon = icon;
        this.appid = appid;
    }

    public MusicInfo(final String title, final String desc, final String purl, final String murl, final String jurl, final String source) {
        this.appid = 1234561234L;
        this.icon = "";
        this.source = source;
        this.title = title;
        this.desc = desc;
        this.purl = purl;
        this.murl = murl;
        this.jurl = jurl;
    }
}
