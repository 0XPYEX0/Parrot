package me.xpyex.plugin.allinone.modelcode.music.musicsource;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import me.xpyex.plugin.allinone.modelcode.music.MusicInfo;
import me.xpyex.plugin.allinone.modelcode.music.MusicSource;
import me.xpyex.plugin.allinone.modelcode.music.MusicUtils;

public class QQMusicSource implements MusicSource {

    public QQMusicSource() {
    }

    public String queryRealUrl(String songmid) {
        try {
            StringBuilder urlsb = new StringBuilder("https://u.y.qq.com/cgi-bin/musicu.fcg?format=json&data=%7B%22req_0%22%3A%7B%22module%22%3A%22vkey.GetVkeyServer%22%2C%22method%22%3A%22CgiGetVkey%22%2C%22param%22%3A%7B%22guid%22%3A%22358840384%22%2C%22songmid%22%3A%5B%22");
            urlsb.append(songmid);
            urlsb.append("%22%5D%2C%22songtype%22%3A%5B0%5D%2C%22uin%22%3A%221443481947%22%2C%22loginflag%22%3A1%2C%22platform%22%3A%2220%22%7D%7D%2C%22comm%22%3A%7B%22uin%22%3A%2218585073516%22%2C%22format%22%3A%22json%22%2C%22ct%22%3A24%2C%22cv%22%3A0%7D%7D");
            
            JSONObject out = JSONUtil.parseObj(HttpUtil.get(urlsb.toString()));
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
        URL url = new URL("https://u.y.qq.com/cgi-bin/musicu.fcg");
        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        huc.setRequestProperty("User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36");
    
        JSONObject scs = new JSONObject();
        JSONObject search = new JSONObject();
        JSONObject searchParam = new JSONObject();
        searchParam.set("query", keyword);
        searchParam.set("num_per_page",3);
        searchParam.set("search_type",0);
        searchParam.set("page_num",1);
        search.set("param",searchParam);
        search.set("module","music.search.SearchCgiService");
        search.set("method", "DoSearchForQQMusicDesktop");
        scs.set("music.search.SearchCgiService", search);
        huc.setRequestMethod("POST");
        huc.setRequestProperty("referer","https://y.qq.com");
        huc.setDoOutput(true);
        huc.connect();
        huc.getOutputStream().write(scs.toString().getBytes(StandardCharsets.UTF_8));
        String s = new String(MusicUtils.readAll(huc.getInputStream()), StandardCharsets.UTF_8);
        s = s.substring(s.indexOf("{"));
        s = s.substring(0,s.lastIndexOf("}") + 1);
        JSONArray ss = new JSONObject(s).getJSONObject("music.search.SearchCgiService")
                           .getJSONObject("data").getJSONObject("body").getJSONObject("song").getJSONArray("list");
        JSONObject song = ss.getJSONObject(0);// .data.song.list
        String mid = song.getStr("mid");
        String musicURL = queryRealUrl(mid);
        int i = 0;
        while (!MusicUtils.isExistent(musicURL)) {
            song = ss.getJSONObject(++i);
            mid = song.getStr("mid");
            musicURL = queryRealUrl(mid);
        }
        String desc;
        try {
            JSONArray singers = song.getJSONArray("singer");
            StringBuilder sgs = new StringBuilder();
            for (Object je : singers) {
                sgs.append(new JSONObject(je).getStr("name"));
                sgs.append(";");
            }
            sgs.deleteCharAt(sgs.length() - 1);
            desc = sgs.toString();
        } catch (Exception e) {
            desc = song.getJSONObject("album").getStr("name");
        }
    
        if (musicURL == null) {
            throw new FileNotFoundException();
        }
        return new MusicInfo(song.getStr("title"), desc,
            "http://y.gtimg.cn/music/photo_new/T002R300x300M000" + song.getJSONObject("album").getStr("mid") + ".jpg",
            musicURL, "https://i.y.qq.com/v8/playsong.html?_wv=1&songid=" + song.getStr("id")
                          + "&source=qqshare&ADTAG=qqshare",
            "QQ音乐", "https://url.cn/PwqZ4Jpi", 100497308);
    }

}
