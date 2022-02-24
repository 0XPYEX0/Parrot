package me.xpyex.plugin.allinone.functions.music.cardprovider;

import me.xpyex.plugin.allinone.functions.music.MusicCardProvider;
import me.xpyex.plugin.allinone.functions.music.MusicInfo;
import net.mamoe.mirai.message.data.*;

public class XMLCardProvider implements MusicCardProvider
{
    @Override
    public MessageChain process(final MusicInfo mi) {
        final StringBuilder xmb = new StringBuilder("<msg serviceID=\"2\" templateID=\"1\" action=\"web\" brief=\"[\u97f3\u4e50]")
                .append(mi.title)
                .append("\" sourceMsgId=\"0\" url=\"")
                .append(mi.jurl.replaceAll("\\&", "&amp;"))
                .append("\" flag=\"0\" adverSign=\"0\" multiMsgFlag=\"0\">\r\n<item layout=\"2\">\r\n")
                .append("<audio cover=\"")
                .append(mi.purl.replaceAll("\\&", "&amp;"))
                .append("\" src=\"")
                .append(mi.murl.replaceAll("\\&", "&amp;"))
                .append("\"/>\r\n").append("<title>")
                .append(mi.title)
                .append("</title>\r\n<summary>")
                .append(mi.desc)
                .append("</summary>\r\n</item>\r\n<source name=\"")
                .append(mi.source)
                .append("\" icon=\"")
                .append(mi.icon)
                .append("\" url=\"\" action=\"\" a_actionData=\"\" i_actionData=\"\" appid=\"")
                .append(mi.appid)
                .append("\"/>\r\n</msg>");
        final Message msg = new SimpleServiceMessage(2, xmb.toString());
        return msg.plus(mi.jurl);
    }
}
