package me.xpyex.plugin.allinone.modulecode.music.cardprovider;

import me.xpyex.plugin.allinone.modulecode.music.MusicCardProvider;
import me.xpyex.plugin.allinone.modulecode.music.MusicInfo;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.SimpleServiceMessage;

public class XMLCardProvider implements MusicCardProvider {

    public XMLCardProvider() {
    }

    @Override
    public MessageChain process(MusicInfo mi, Contact ct) {
        String xmb = "<msg serviceID=\"2\" templateID=\"1\" action=\"web\" actionData=\"\" a_actionData=\"\" i_actionData=\"\" brief=\"[音乐]" +
                         escapeXmlTag(mi.title) + "\" sourceMsgId=\"0\" url=\"" + escapeXmlTag(mi.jurl) +
                         "\" flag=\"0\" adverSign=\"0\" multiMsgFlag=\"0\">\r\n<item layout=\"2\">\r\n" +
                         "<audio cover=\"" + escapeXmlTag(mi.purl) + "\" src=\"" +
                         escapeXmlTag(mi.murl) + "\"/>\r\n" + "<title>" + escapeXmlContent(mi.title) +
                         "</title>\r\n<summary>" + escapeXmlContent(mi.desc) +
                         "</summary>\r\n</item>\r\n<source name=\"" + escapeXmlTag(mi.source) + "\" icon=\"" +
                         escapeXmlTag(mi.icon) +
                         "\" url=\"\" action=\"web\" a_actionData=\"tencent0://\" i_actionData=\"\" appid=\"" + mi.appid +
                         "\"/>\r\n</msg>";
        Message msg = new SimpleServiceMessage(2, xmb);
        return msg.plus(mi.jurl);
    }

    public String escapeXmlContent(String org) {
        return org.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        //
    }

    public String escapeXmlTag(String org) {
        return org.replaceAll("\\&", "&amp;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;")
                   .replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }
}
