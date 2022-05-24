package me.xpyex.plugin.allinone.functions.music.musicsource;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.net.HttpURLConnection;
import java.net.URL;
import me.xpyex.plugin.allinone.functions.music.MusicInfo;
import me.xpyex.plugin.allinone.functions.music.MusicSource;
import me.xpyex.plugin.allinone.functions.music.MusicUtils;

public class KugouMusicSource implements MusicSource {
    final static String COOKIE = "kg_mid=30f1713c23ab7bb496ab035b07dae834; ACK_SERVER_10015=%7B%22list%22%3A%5B%5B%22bjlogin-user.kugou.com%22%5D%5D%7D; ACK_SERVER_10016=%7B%22list%22%3A%5B%5B%22bjreg-user.kugou.com%22%5D%5D%7D; ACK_SERVER_10017=%7B%22list%22%3A%5B%5B%22bjverifycode.service.kugou.com%22%5D%5D%7D; Hm_lvt_aedee6983d4cfc62f509129360d6bb3d=1598198881; kg_dfid=1HZmYL0ngIYp0uu93N2m4s5P; kg_dfid_collect=d41d8cd98f00b204e9800998ecf8427e; Hm_lpvt_aedee6983d4cfc62f509129360d6bb3d=1598199021";

    public KugouMusicSource() {
    }

    @Override
    public MusicInfo get(String keyword) throws Exception {
        HttpURLConnection huc = (HttpURLConnection) new URL(
                "http://msearchcdn.kugou.com/api/v3/search/song?showtype=14&highlight=em&pagesize=1&tag_aggr=1&tagtype=%E5%85%A8%E9%83%A8&plat=0&sver=5&correct=1&api_ver=1&version=9108&page=1&area_code=1&tag=1&with_res_tag=1&keyword="
                        + keyword).openConnection();
        huc.setRequestMethod("GET");
        huc.setRequestProperty("Host", "msearchcdn.kugou.com");
        huc.connect();
        JSONObject je = JSONUtil
                .parseObj(new String(MusicUtils.readAll(huc.getInputStream()), "UTF-8").replaceAll("<!--[_A-Z]+-->", ""));
        String song = je.getJSONObject("data").getJSONArray("info").getJSONObject(0).getStr("hash");
        String album_id = je.getJSONObject("data").getJSONArray("info").getJSONObject(0).getStr("album_id");
        HttpURLConnection ihuc = (HttpURLConnection) new URL(
                "https://wwwapi.kugou.com/yy/index.php?r=play/getdata&hash=" + song + "&album_id=" + album_id + "&_="
                        + MusicUtils.getTime()).openConnection();
        ihuc.setRequestMethod("GET");
        ihuc.setRequestProperty("Host", "www.kugou.com");
        ihuc.setRequestProperty("Cookie", COOKIE);
        ihuc.connect();
        JSONObject info = JSONUtil.parseObj(new String(MusicUtils.readAll(ihuc.getInputStream()), "UTF-8")).getJSONObject("data");
        return new MusicInfo(info.getStr("audio_name"), info.getStr("author_name"),
                info.getStr("img"), info.getStr("play_url"),
                "https://www.kugou.com/song/#hash=" + song + "&album_id=" + info.getStr("album_id"), "酷狗", "", 205141);
    }

}