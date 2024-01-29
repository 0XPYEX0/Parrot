package me.xpyex.plugin.allinone.modulecode.music.musicsource;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import me.xpyex.plugin.allinone.modulecode.music.MusicInfo;
import me.xpyex.plugin.allinone.modulecode.music.MusicSource;
import me.xpyex.plugin.allinone.modulecode.music.MusicUtils;
import me.xpyex.plugin.allinone.modulecode.music.NetEaseCrypto;
import me.xpyex.plugin.allinone.utils.Util;

public class NetEaseMusicSource implements MusicSource {

    public NetEaseMusicSource() {
    }

    public String queryRealUrl(String id) throws Exception {
        return "http://music.163.com/song/media/outer/url?id=" + id + ".mp3";
    }

    @Override
    public MusicInfo get(String keyword) throws Exception {
        JSONObject params = new JSONObject();
        params.set("s", URLDecoder.decode(keyword, StandardCharsets.UTF_8));
        params.set("type", 1);
        params.set("offset", 0);
        params.set("limit", 3);
        String[] encrypt = NetEaseCrypto.weapiEncrypt(params.toString());
        String sb = "params=" + encrypt[0] +
                        "&encSecKey=" +
                        encrypt[1];
        URL url = new URL("https://music.163.com/weapi/cloudsearch/get/web?csrf_token=");
        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        huc.setDoInput(true);
        huc.setDoOutput(true);
        huc.setRequestMethod("POST");
        huc.setRequestProperty("Referer", "http://music.163.com/");
        huc.setRequestProperty("Cookie", "appver=1.5.0.75771;");
        huc.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        huc.connect();
        huc.getOutputStream().write(sb.getBytes(StandardCharsets.UTF_8));
        JSONArray ja;
        String murl;
        String data = new String(Util.readAll(huc.getInputStream()), StandardCharsets.UTF_8);
        if (huc.getResponseCode() == 200) {
            ja = JSONUtil.parseObj(data)
                     .getJSONObject("result").getJSONArray("songs");
        } else
            throw new FileNotFoundException();
        JSONObject jo = ja.getJSONObject(0);
        murl = queryRealUrl(jo.getStr("id"));
        int i = 0;
        while (!MusicUtils.isExistent(murl)) {
            jo = ja.getJSONObject(++i);
            murl = queryRealUrl(jo.getStr("id"));
        }
        return new MusicInfo(jo.getStr("name"),
            jo.getJSONArray("ar").getJSONObject(0).getStr("name"),
            jo.getJSONObject("al").getStr("picUrl"), murl,
            "https://y.music.163.com/m/song?id=" + jo.getStr("id"), "网易云音乐", "", 100495085);
    }

}