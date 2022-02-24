package me.xpyex.plugin.allinone.functions.music.musicsource;

import java.net.*;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import me.xpyex.plugin.allinone.Main;
import me.xpyex.plugin.allinone.functions.music.MusicInfo;
import me.xpyex.plugin.allinone.functions.music.MusicSource;
import me.xpyex.plugin.allinone.functions.music.MusicUtils;
import me.xpyex.plugin.allinone.functions.music.NetEaseCrypto;

import java.nio.charset.*;
import java.io.*;

public class NetEaseMusicSource implements MusicSource
{
    public String queryRealUrl(final String id) throws Exception {
        //final JsonObject params = new JsonObject();
        final JSONObject params = new JSONObject();
        //params.add("ids", (JsonElement)new JsonPrimitive("[" + id + "]"));
        params.put("ids", "[" + id + "]");
        //params.add("br", (JsonElement)new JsonPrimitive((Number)999000));
        params.put("br", 999000);
        final String[] encrypt = NetEaseCrypto.weapiEncrypt(params.toString());
        final StringBuilder sb = new StringBuilder("params=");
        sb.append(encrypt[0]);
        sb.append("&encSecKey=");
        sb.append(encrypt[1]);
        final byte[] towrite = sb.toString().getBytes("UTF-8");
        final URL u = new URL("http://music.163.com/weapi/song/enhance/player/url?csrf_token=");
        final HttpURLConnection conn = (HttpURLConnection)u.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(4000);
        conn.setReadTimeout(4000);
        conn.setFixedLengthStreamingMode(towrite.length);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,gl;q=0.6,zh-TW;q=0.4");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(towrite.length));
        conn.setRequestProperty("Referer", "https://music.163.com");
        conn.setRequestProperty("Host", "music.163.com");
        conn.setRequestProperty("User-Agent", NetEaseCrypto.getUserAgent());
        conn.connect();
        conn.getOutputStream().write(towrite);
        if (conn.getResponseCode() == 200) {
            final InputStream is = conn.getInputStream();
            byte[] bs = null;
            bs = MusicUtils.readAll(is);
            is.close();
            conn.disconnect();
            //final JsonObject main = JsonParser.parseString(new String(bs, "UTF-8")).getAsJsonObject();
            final JSONObject main = JSONUtil.parseObj(new String(bs, StandardCharsets.UTF_8));
            //if (main.get("code").getAsInt() == 200) {
            if (main.getInt("code") == 200) {
                //final JsonArray data = main.get("data").getAsJsonArray();
                final JSONArray data = main.getJSONArray("data");
                //final JsonObject song = data.get(0).getAsJsonObject();
                final JSONObject song = data.getJSONObject(0);
                //if (song.get("code").getAsInt() == 200) {
                if (song.getInt("code") == 200) {
                    //return song.get("url").getAsString().trim();
                    return song.getStr("url").trim();
                }
            }
        }
        return null;
    }
    
    @Override
    public MusicInfo get(final String keyword) throws Exception {
        final URL url = new URL("http://music.163.com/api/search/pc");
        final HttpURLConnection huc = (HttpURLConnection)url.openConnection();
        huc.setDoInput(true);
        huc.setDoOutput(true);
        huc.setRequestMethod("POST");
        huc.setRequestProperty("Referer", "http://music.163.com/");
        huc.setRequestProperty("Cookie", "appver=1.5.0.75771;");
        huc.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        huc.connect();
        huc.getOutputStream().write(("type=1&offset=0&limit=1&s=" + keyword).getBytes(StandardCharsets.UTF_8));
        huc.getInputStream();
        if (huc.getResponseCode() == 200) {
            //final JsonObject jo = JsonParser.parseString(new String(MusicUtils.readAll(huc.getInputStream()), StandardCharsets.UTF_8)).getAsJsonObject().get("result").getAsJsonObject().get("songs").getAsJsonArray().get(0).getAsJsonObject();
            final JSONObject jo = JSONUtil.parseObj(new String(MusicUtils.readAll(huc.getInputStream()), StandardCharsets.UTF_8)).getJSONObject("result").getJSONArray("songs").getJSONObject(0);
            Main.LOGGER.info(jo.toStringPretty());
            //return new MusicInfo(jo.get("name").getAsString(), jo.get("artists").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString(), jo.get("album").getAsJsonObject().get("picUrl").getAsString(), this.queryRealUrl(jo.get("id").getAsString()), "https://y.music.163.com/m/song?id=" + jo.get("id").getAsString(), "\u7f51\u6613\u4e91\u97f3\u4e50", "", 100495085L);
            return new MusicInfo(
                    jo.getStr("name"),
                    jo.getJSONArray("artists").getJSONObject(0).getStr("name"),
                    jo.getJSONObject("album").getStr("picUrl"),
                    this.queryRealUrl(jo.getStr("id")),
                    "https://y.music.163.com/m/song?id=" + jo.getStr("id"),
                    "网易云音乐",
                    "",
                    100495085L);
        }
        throw new FileNotFoundException();
    }
}
