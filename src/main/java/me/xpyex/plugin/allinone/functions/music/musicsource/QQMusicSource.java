package me.xpyex.plugin.allinone.functions.music.musicsource;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import me.xpyex.plugin.allinone.functions.music.MusicInfo;
import me.xpyex.plugin.allinone.functions.music.MusicSource;
import me.xpyex.plugin.allinone.functions.music.MusicUtils;

public class QQMusicSource implements MusicSource {

    public QQMusicSource() {
    }

    public String queryRealUrl(String songmid) {
        try {
            StringBuilder urlsb = new StringBuilder(
                    "https://u.y.qq.com/cgi-bin/musicu.fcg?format=json&data=%7B%22req_0%22%3A%7B%22module%22%3A%22vkey.GetVkeyServer%22%2C%22method%22%3A%22CgiGetVkey%22%2C%22param%22%3A%7B%22guid%22%3A%22358840384%22%2C%22songmid%22%3A%5B%22");
            urlsb.append(songmid);
            urlsb.append(
                    "%22%5D%2C%22songtype%22%3A%5B0%5D%2C%22uin%22%3A%221443481947%22%2C%22loginflag%22%3A1%2C%22platform%22%3A%2220%22%7D%7D%2C%22comm%22%3A%7B%22uin%22%3A%2218585073516%22%2C%22format%22%3A%22json%22%2C%22ct%22%3A24%2C%22cv%22%3A0%7D%7D");
            URL u = new URL(urlsb.toString());
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Host", "u.y.qq.com");
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1");
            conn.connect();
            byte[] bs = MusicUtils.readAll(conn.getInputStream());

            JSONObject out = JSONUtil.parseObj(new String(bs, "UTF-8"));
            if (out.getInt("code") != 0) {
                return null;
            }
            StringBuilder sb = new StringBuilder(out.getJSONObject("req_0").getJSONObject("data")
                    .getJSONArray("sip").getStr(0));

            sb.append(out.getJSONObject("req_0").getJSONObject("data").getJSONArray("midurlinfo")
                    .getJSONObject(0).getStr("purl"));
            return sb.toString();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public MusicInfo get(String keyword) throws Exception {
        URL url = new URL("https://c.y.qq.com/soso/fcgi-bin/client_search_cp?p=1&cr=1&aggr=1&flag_qc=0&n=3&w=" + keyword
                + "&format=json");
        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        huc.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36");
        huc.setRequestMethod("GET");
        huc.connect();
        JSONArray ss = JSONUtil.parseObj(new String(MusicUtils.readAll(huc.getInputStream()), StandardCharsets.UTF_8))
                .getJSONObject("data").getJSONObject("song").getJSONArray("list");
        JSONObject song = ss.getJSONObject(0);// .data.song.list
        String mid = song.getStr("songmid");
        String musicURL = queryRealUrl(mid);
        int i = 0;
        while (!MusicUtils.isExistent(musicURL)) {
            song = ss.getJSONObject(++i);
            mid = song.getStr("songmid");
            musicURL = queryRealUrl(mid);
        }
        String desc;
        try {
            JSONArray singers = song.getJSONArray("singer");
            StringBuilder sgs = new StringBuilder();
            for (Object je : singers) {
                if (je instanceof JSONObject) {
                    sgs.append(((JSONObject) je).getStr("name"));
                    sgs.append(";");
                }
            }
            sgs.deleteCharAt(sgs.length() - 1);
            desc = sgs.toString();
        } catch (Exception e) {
            desc = song.getStr("albumname");
        }

        if (musicURL == null) {
            throw new FileNotFoundException();
        }
        return new MusicInfo(song.getStr("songname"), desc,
                "http://y.gtimg.cn/music/photo_new/T002R300x300M000" + song.getStr("albummid") + ".jpg",
                musicURL, "https://i.y.qq.com/v8/playsong.html?_wv=1&songid=" + song.getStr("songid")
                + "&source=qqshare&ADTAG=qqshare",
                "QQ音乐", "https://url.cn/PwqZ4Jpi", 100497308);
    }

}
