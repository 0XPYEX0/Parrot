package me.xpyex.plugin.allinone.model;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.xpyex.plugin.allinone.api.CommandMessager;
import me.xpyex.plugin.allinone.core.Model;
import me.xpyex.plugin.allinone.modelcode.bilibili.BilibiliUtil;
import me.xpyex.plugin.allinone.utils.StringUtil;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.event.events.MessageEvent;

public class Bilibili extends Model {
    private static final ExecutorService SERVICE = Executors.newSingleThreadExecutor();
    private static final String URL_B23 = "b23.tv/";
    private static final String URL_BILIBILI = "bilibili.com/video/";

    @Override
    public void register() {
        listenEvent(MessageEvent.class, (event) -> {
            SERVICE.submit(() -> {
                String msg = Util.getPlainText(event.getMessage()).replace("\\/", "/");
                if (msg.toLowerCase().startsWith("#av") || msg.toLowerCase().startsWith("#bv")) {
                    try {
                        HashMap<String, Object> map = new HashMap<>();
                        if (msg.toLowerCase().startsWith("#av")) {
                            map.put("aid", msg.substring(3));
                        } else if (msg.toLowerCase().startsWith("#bv")) {
                            if (msg.length() != 13) {
                                return;
                            }
                            map.put("bvid", msg.substring(3, 13));
                        }
                        Util.autoSendMsg(event, BilibiliUtil.getVideoInfo(map));
                    } catch (Exception e) {
                        Util.autoSendMsg(event, "解析错误: " + e);
                        Util.handleException(e);
                    }
                } else if (msg.toLowerCase().startsWith("#ss") || msg.toLowerCase().startsWith("#ep")) {
                    try {
                        HashMap<String, Object> map = new HashMap<>();
                        if (msg.toLowerCase().startsWith("#ss")) {
                            map.put("season_id", msg.substring(3));
                        } else if (msg.toLowerCase().startsWith("#ep")) {
                            map.put("ep_id", msg.substring(3));
                        }
                        String result = HttpUtil.get("https://api.bilibili.com/pgc/view/web/season", map);
                        int failCount = 0;
                        while (result == null || result.isEmpty()) {
                            if (failCount > 5) {
                                Util.autoSendMsg(event, "解析超时");
                                return;
                            }
                            result = HttpUtil.post("https://api.bilibili.cn/view/" + msg, map);
                            failCount++;
                            Thread.sleep(5000L);
                        }
                        JSONObject infos = new JSONObject(result);
                        if (infos.getInt("code") != 0) {
                            Util.autoSendMsg(event, "无法找到番剧: " + infos.getStr("message")
                                    + "\n错误码: " + infos.getInt("code"));
                            return;
                        }
                        JSONObject publish = infos.getJSONObject("result").getJSONObject("publish");
                        boolean finished = (publish.getInt("is_finish") == 1);
                        String publishTime = publish.getStr("pub_time");
                        JSONObject newestEP = infos.getJSONObject("result").getJSONObject("new_ep");
                        String title = infos.getJSONObject("result").getStr("title");
                        int seasonId = infos.getJSONObject("result").getInt("season_id");
                        CommandMessager messager = new CommandMessager()
                                .plus("番剧: " + msg.substring(1))
                                .plus("番剧名: " + title)
                                .plus("已完结: " + finished)
                                .plus("上映时间: " + publishTime)
                                .plus("番剧播放地址: https://www.bilibili.com/bangumi/play/ss" + seasonId)
                                .plus("最新集播放地址: https://www.bilibili.com/bangumi/play/ep" + newestEP.getInt("id"))
                                .plus("最新集: " + newestEP.getStr("title"));
                        Util.autoSendMsg(event, messager.toString());
                    } catch (Exception e) {
                        Util.autoSendMsg(event, "解析错误: " + e);
                        Util.handleException(e);
                    }
                } else if (msg.toLowerCase().startsWith("#search bilibili ")) {
                    try {
                        String keyword = msg.substring(17);
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("keyword", keyword);
                        String result = HttpUtil.get("https://api.bilibili.com/x/web-interface/search/all/v2", map);
                        int count = 0;
                        while (result == null || result.isEmpty()) {
                            if (count >= 5) {
                                Util.autoSendMsg(event, "搜索超时");
                                return;
                            }
                            result = HttpUtil.get("https://api.bilibili.com/x/web-interface/search/all/v2", map);
                            count++;
                            Thread.sleep(5000L);
                        }
                        JSONObject json = new JSONObject(result);
                        int code = json.getInt("code");
                        if (code != 0) {
                            Util.autoSendMsg(event, "搜索错误: 请求错误" +
                                    "\n" +
                                    "错误码: " + code +
                                    "错误信息: " + json.getStr("message")
                            );
                            return;
                        }
                        JSONObject data = json.getJSONObject("data");
                        JSONArray results = data.getJSONArray("result").getJSONObject(10).getJSONArray("data");
                        int limit = 5;  //限制展示几条信息
                        CommandMessager messager = new CommandMessager("关键词: " + keyword)
                                .plus("仅展示前 " + limit + " 条结果！");
                        for (int i = 0; i < limit; i++) {
                            messager.plus("").plus("");
                            JSONObject videoInfo = results.getJSONObject(i);
                            if (videoInfo == null) {
                                continue;
                            }
                            String bvid = videoInfo.getStr("bvid");
                            String title = videoInfo.getStr("title").replace("<em class=\"keyword\">", "").replace("</em>", "");
                            String url = videoInfo.getStr("arcurl");
                            String author = videoInfo.getStr("author");
                            String description = videoInfo.getStr("description");
                            messager.plus("视频 " + bvid)
                                    .plus("标题: " + title)
                                    .plus("作者: " + author)
                                    .plus("简介: " + description)
                                    .plus("播放地址: " + url);
                        }
                        messager.plus("").plus("篇幅受限，仅展示前 " + limit + " 条结果");
                        Util.autoSendMsg(event, messager.toString());
                    } catch (Exception e) {
                        Util.autoSendMsg(event, "搜索错误: " + e);
                        Util.handleException(e);
                    }
                } else if (msg.contains(URL_BILIBILI)) {
                    try {
                        String id = StringUtil.getStrBetweenChars(URL_BILIBILI + StringUtil.getStrBetweenChars(msg, URL_BILIBILI, "?"), URL_BILIBILI, "\"");
                        Util.autoSendMsg(event, BilibiliUtil.getVideoInfo("https://" + URL_BILIBILI + id));
                    } catch (Exception e) {
                        Util.autoSendMsg(event, "解析错误: " + e);
                        Util.handleException(e);
                    }
                } else if (msg.startsWith("#user")) {
                    try {
                        int ID = Integer.parseInt(msg.substring(5));
                        Util.autoSendMsg(event, BilibiliUtil.getUserInfo(ID));
                    } catch (Exception e) {
                        if (e instanceof NumberFormatException) {
                            Util.autoSendMsg(event, "请输入正确的ID");
                        } else {
                            Util.autoSendMsg(event, "出现错误: " + e);
                            Util.handleException(e);
                        }
                    }
                } else if (msg.contains(URL_B23)) {
                    try {
                        String b23ID = StringUtil.getStrBetweenChars(URL_B23 + StringUtil.getStrBetweenChars(msg, URL_B23, "?"), URL_B23, "\"");
                        String path = "https://" + URL_B23 + b23ID;
                        HttpRequest r = HttpUtil.createGet(path, false);
                        r.execute();
                        HttpURLConnection conn = r.getConnection().getHttpURLConnection();
                        System.out.println(conn.getHeaderFields());
                        Util.autoSendMsg(event, BilibiliUtil.getVideoInfo(conn.getHeaderField("Location")));
                    } catch (Exception e) {
                        Util.autoSendMsg(event, "解析错误: " + e);
                        Util.handleException(e);
                    }
                }
            });
        });
    }
}
