package me.xpyex.plugin.allinone.functions.music.musicsource;

import java.net.*;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import me.xpyex.plugin.allinone.utils.Util;
import me.xpyex.plugin.allinone.functions.music.MusicInfo;
import me.xpyex.plugin.allinone.functions.music.MusicSource;

import java.io.*;

public class QQMusicSource implements MusicSource
{
    public String queryRealUrl(final String songmid) {
        try {
            final StringBuilder urlsb = new StringBuilder("https://u.y.qq.com/cgi-bin/musicu.fcg?format=json&data=%7B%22req_0%22%3A%7B%22module%22%3A%22vkey.GetVkeyServer%22%2C%22method%22%3A%22CgiGetVkey%22%2C%22param%22%3A%7B%22guid%22%3A%22358840384%22%2C%22songmid%22%3A%5B%22");
            urlsb.append(songmid);
            urlsb.append("%22%5D%2C%22songtype%22%3A%5B0%5D%2C%22uin%22%3A%221443481947%22%2C%22loginflag%22%3A1%2C%22platform%22%3A%2220%22%7D%7D%2C%22comm%22%3A%7B%22uin%22%3A%2218585073516%22%2C%22format%22%3A%22json%22%2C%22ct%22%3A24%2C%22cv%22%3A0%7D%7D");
            final URL u = new URL(urlsb.toString());
            String result = HttpUtil.get(urlsb.toString());
            final JSONObject out = new JSONObject(result);
            System.out.println(out);
            if (out.getInt("code") != 0) {
                return null;
            }
            final StringBuilder sb = new StringBuilder(out.getJSONObject("req_0").getJSONObject("data").getJSONArray("sip").getStr(0));
            sb.append(out.getJSONObject("req_0").getJSONObject("data").getJSONArray("midurlinfo").getJSONObject(0).getStr("purl"));
            return sb.toString();
        }
        catch (Throwable e) {
            Util.handleException(e);
            return null;
        }
    }
    
    @Override
    public MusicInfo get(final String keyword) throws Exception {
        final URL url = new URL("https://c.y.qq.com/soso/fcgi-bin/client_search_cp?p=1&cr=1&aggr=1&flag_qc=0&n=50&w=" + keyword + "&format=json");

        /*final HttpURLConnection huc = (HttpURLConnection)url.openConnection();
        huc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36");
        huc.setRequestMethod("GET");
        huc.connect();
         */
        String result = HttpUtil.get("https://c.y.qq.com/soso/fcgi-bin/client_search_cp?p=1&cr=1&aggr=1&flag_qc=0&n=50&w=" + keyword + "&format=json");

        //final JsonObject song = JsonParser.parseString(new String(MusicUtils.readAll(huc.getInputStream()), StandardCharsets.UTF_8)).getAsJsonObject().get("data").getAsJsonObject().get("song").getAsJsonObject().get("list").getAsJsonArray().get(0).getAsJsonObject();
        final JSONObject song = new JSONObject(result).getJSONObject("data").getJSONObject("song").getJSONArray("list").getJSONObject(0);
        //final String mid = song.get("songmid").getAsString();
        final String mid = song.getStr("songmid");
        final String musicURL = this.queryRealUrl(mid);
        String desc;
        try {
            //JsonArray singers = song.get("singer").getAsJsonArray();
            JSONArray singers = song.getJSONArray("singer");
            StringBuilder sgs = new StringBuilder();
            //for (JsonElement je : singers) {
            for (Object je : singers) {
                //sgs.append(je.getAsJsonObject().get("name").getAsString());
                sgs.append(((JSONObject)je).getStr("name"));
                sgs.append(";");
            }
            sgs.deleteCharAt(sgs.length() - 1);
            desc = sgs.toString();
        } catch (Exception e) {
            //desc = song.get("albumname").getAsString();
            desc = song.getStr("albumname");
        }
        if (musicURL == null) {
            throw new FileNotFoundException();
        }
        //return new MusicInfo(song.get("songname").getAsString(), desc, "http://y.gtimg.cn/music/photo_new/T002R300x300M000" + song.get("albummid").getAsString() + ".jpg", musicURL, "https://i.y.qq.com/v8/playsong.html?_wv=1&songid=" + song.get("songid").getAsString() + "&source=qqshare&ADTAG=qqshare", "QQ\u97f3\u4e50", "https://url.cn/PwqZ4Jpi", 1101079856L);
        return new MusicInfo(
                song.getStr("songname"),
                desc,
                "http://y.gtimg.cn/music/photo_new/T002R300x300M000" + song.getStr("albummid") + ".jpg",
                musicURL,
                "https://i.y.qq.com/v8/playsong.html?_wv=1&songid=" + song.getStr("songid") + "&source=qqshare&ADTAG=qqshare",
                "QQ音乐",
                "https://url.cn/PwqZ4Jpi",
                1101079856L);
    }
}
