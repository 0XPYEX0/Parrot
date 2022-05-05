package me.xpyex.plugin.allinone.functions.networktasks;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import me.xpyex.plugin.allinone.Main;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.HashMap;

public class BiliBili {
    public static void Execute(MessageEvent event) {
        String msg = Util.getPlainText(event.getMessage());
        if (msg.toLowerCase().startsWith("#av") || msg.toLowerCase().startsWith("#bv")) {
            new Thread(() -> {
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
                    String result = HttpUtil.get("http://api.bilibili.com/x/web-interface/view", map);
                    int failCount = 0;
                    while (result == null || result.isEmpty()) {
                        if (failCount > 5) {
                            Util.autoSendMsg(event, "解析超时");
                            return;
                        }
                        result = HttpUtil.post("http://api.bilibili.com/x/web-interface/view", map);
                        failCount++;
                        Thread.sleep(5000L);
                    }
                    Main.LOGGER.info(result);
                    JSONObject infos = new JSONObject(result);
                    int success = infos.getInt("code");
                    if (success != 0) {
                        String Reason;
                        if (success == -400) {
                            Reason = "请求错误";
                        } else if (success == -403) {
                            Reason = "权限不足";
                        } else if (success == -404) {
                            Reason = "视频不存在";
                        } else if (success == 62002) {
                            Reason = "视频不可见(被锁定)";
                        } else {
                            Reason = "未知原因";
                        }
                        Util.autoSendMsg(event, "解析失败: " + Reason
                                + "\n错误码: " + success
                                + "\n错误信息: " + infos.getStr("message"));
                        return;
                    }
                    JSONObject data = infos.getJSONObject("data");
                    int AvID = data.getInt("aid");
                    String BvID = data.getStr("bvid");
                    int videoCount = data.getInt("videos");
                    String title = data.getStr("title");
                    String description = data.getStr("desc");
                    JSONObject ownerInfo = data.getJSONObject("owner");
                    String ownerName = ownerInfo.getStr("name");
                    int ownerId = ownerInfo.getInt("mid");
                    Util.autoSendMsg(event, "视频: " + msg.substring(1)
                            + "\nAV号: AV" + AvID
                            + "\nBV号: " + BvID
                            + "\n标题: " + title
                            + "\n简介: " + description
                            + "\n分P数: " + videoCount
                            + "\n播放地址:\nhttps://bilibili.com/video/av" + AvID + "\nhttps://bilibili.com/video/" + BvID + "\n"
                            + "\n作者: " + ownerName
                            + "\n作者主页: https://space.bilibili.com/" + ownerId);
                } catch (Exception e) {
                    Util.autoSendMsg(event, "解析错误: " + e);
                    Util.handleException(e);
                }
            }).start();
        } else if (msg.toLowerCase().startsWith("#ss") || msg.toLowerCase().startsWith("#ep")) {
            new Thread(() -> {
                try {
                    HashMap<String, Object> map = new HashMap<>();
                    if (msg.toLowerCase().startsWith("#ss")) {
                        map.put("season_id", msg.substring(3));
                    } else if (msg.toLowerCase().startsWith("#ep")) {
                        map.put("ep_id", msg.substring(3));
                    }
                    String result = HttpUtil.get("http://api.bilibili.com/pgc/view/web/season", map);
                    int failCount = 0;
                    while (result == null || result.isEmpty()) {
                        if (failCount > 5) {
                            Util.autoSendMsg(event, "解析超时");
                            return;
                        }
                        result = HttpUtil.post("http://api.bilibili.cn/view/" + msg, map);
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
                    Util.autoSendMsg(event, "番剧: " + msg.substring(1) + "\n" +
                            "番剧名: " + title + "\n" +
                            "已完结: " + finished + "\n" +
                            "上映时间: " + publishTime + "\n" +
                            "番剧播放地址: https://www.bilibili.com/bangumi/play/ss" + seasonId + "\n" +
                            "最新集播放地址: https://www.bilibili.com/bangumi/play/ep" + newestEP.getInt("id") + "\n" +
                            "最新集: " + newestEP.getStr("title"));
                } catch (Exception e) {
                    Util.autoSendMsg(event, "解析错误: " + e);
                    Util.handleException(e);
                }
            }).start();
        } else if (msg.toLowerCase().startsWith("#search bilibili ")) {
            new Thread(() -> {
                try {
                    String keyword = msg.substring(17);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("keyword", keyword);
                    String result = HttpUtil.get("http://api.bilibili.com/x/web-interface/search/all/v2", map);
                    int count = 0;
                    while (result == null || result.isEmpty()) {
                        if (count >= 5) {
                            Util.autoSendMsg(event, "搜索超时");
                            return;
                        }
                        result = HttpUtil.get("http://api.bilibili.com/x/web-interface/search/all/v2", map);
                        count++;
                        Thread.sleep(5000L);
                    }
                    JSONObject json = new JSONObject(result);
                    //System.out.println(JSONUtil.toJsonPrettyStr(json));
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
                    StringBuilder out = new StringBuilder("关键词: " + keyword + "\n" +
                            "仅展示前 " + limit + " 条结果！");
                    for (int i = 0; i < limit; i++) {
                        out.append("\n\n");
                        JSONObject videoInfo = results.getJSONObject(i);
                        if (videoInfo == null) {
                            continue;
                        }
                        String bvid = videoInfo.getStr("bvid");
                        String title = videoInfo.getStr("title").replace("<em class=\"keyword\">", "").replace("</em>", "");
                        String url = videoInfo.getStr("arcurl");
                        String author = videoInfo.getStr("author");
                        String description = videoInfo.getStr("description");
                        out
                                .append("视频 ").append(bvid)
                                .append("\n")
                                .append("标题: ").append(title)
                                .append("\n")
                                .append("作者: ").append(author)
                                .append("\n")
                                .append("简介: ").append(description)
                                .append("\n")
                                .append("播放地址: ").append(url);
                    }
                    out.append("\n\n篇幅受限，仅展示前 ").append(limit).append(" 条结果");
                    Util.autoSendMsg(event, out.toString());
                } catch (Exception e) {
                    Util.autoSendMsg(event, "搜索错误: " + e);
                    Util.handleException(e);
                }
            }).start();
        } else if (msg.contains("https://www.bilibili.com/video/") || msg.contains("https://bilibili.com/video/")) {
            new Thread(() -> {
                try {
                    String link = msg.contains("https://www.bilibili.com/video/") ? "https://www.bilibili.com/video/" : "https://bilibili.com/video/";
                    String id = msg.substring(msg.indexOf(link) + link.length(), msg.contains("?") ? msg.indexOf("?") : msg.length()).split("\n")[0];
                    System.out.println("截取到的ID: " + id);
                    HashMap<String, Object> map = new HashMap<>();
                    if (id.toLowerCase().startsWith("av")) {
                        map.put("aid", id.substring(2));
                    } else if (id.toLowerCase().startsWith("bv")) {
                        if (id.length() != 12) {
                            return;
                        }
                        map.put("bvid", id.substring(2));
                    } else {
                        return;
                    }
                    String result = HttpUtil.get("http://api.bilibili.com/x/web-interface/view", map);
                    int failCount = 0;
                    while (result == null || result.isEmpty()) {
                        if (failCount > 5) {
                            Util.autoSendMsg(event, "解析超时");
                            return;
                        }
                        result = HttpUtil.post("http://api.bilibili.com/x/web-interface/view", map);
                        failCount++;
                        Thread.sleep(5000L);
                    }
                    Main.LOGGER.info(result);
                    JSONObject infos = new JSONObject(result);
                    int success = infos.getInt("code");
                    if (success != 0) {
                        String Reason;
                        if (success == -400) {
                            Reason = "请求错误";
                        } else if (success == -403) {
                            Reason = "权限不足";
                        } else if (success == -404) {
                            Reason = "视频不存在";
                        } else if (success == 62002) {
                            Reason = "视频不可见(被锁定)";
                        } else {
                            Reason = "未知原因";
                        }
                        Util.autoSendMsg(event, "解析失败: " + Reason
                                + "\n错误码: " + success
                                + "\n错误信息: " + infos.getStr("message"));
                        return;
                    }
                    JSONObject data = infos.getJSONObject("data");
                    int AvID = data.getInt("aid");
                    String BvID = data.getStr("bvid");
                    int videoCount = data.getInt("videos");
                    String title = data.getStr("title");
                    String description = data.getStr("desc");
                    JSONObject ownerInfo = data.getJSONObject("owner");
                    String ownerName = ownerInfo.getStr("name");
                    int ownerId = ownerInfo.getInt("mid");
                    Util.autoSendMsg(event, "视频: " + id
                            + "\nAV号: AV" + AvID
                            + "\nBV号: " + BvID
                            + "\n标题: " + title
                            + "\n简介: " + description
                            + "\n分P数: " + videoCount
                            + "\n播放地址:\nhttps://bilibili.com/video/av" + AvID + "\nhttps://bilibili.com/video/" + BvID + "\n"
                            + "\n作者: " + ownerName
                            + "\n作者主页: https://space.bilibili.com/" + ownerId);
                } catch (Exception e) {
                    Util.autoSendMsg(event, "解析错误: " + e);
                    Util.handleException(e);
                }
            }).start();
        }
    }
}
