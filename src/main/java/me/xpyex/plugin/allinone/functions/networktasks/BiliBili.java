package me.xpyex.plugin.allinone.functions.networktasks;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import me.xpyex.plugin.allinone.Main;
import me.xpyex.plugin.allinone.Utils;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.HashMap;
import java.util.Map;

public class BiliBili {
    public static void Execute(MessageEvent event) {
        String msg = Utils.getNormalText(event.getMessage());
        if (msg.toLowerCase().startsWith("#av") || msg.toLowerCase().startsWith("#bv")) {
            new Thread(() -> {
                try {
                    Map<String, Object> map = new HashMap<>();
                    if (msg.toLowerCase().startsWith("#av")) {
                        map.put("aid", msg.substring(3));
                    } else if (msg.toLowerCase().startsWith("#bv")) {
                        if (msg.length() < 13) {
                            return;
                        }
                        map.put("bvid", msg.substring(3, 13));
                    }
                    String result = HttpUtil.get("http://api.bilibili.com/x/web-interface/view", map);
                    int failCount = 0;
                    while (result == null || result.equals("")) {
                        if (failCount > 5) {
                            Utils.autoSendMsg(event, "解析超时");
                            return;
                        }
                        result = HttpUtil.post("http://api.bilibili.com/x/web-interface/view", map);
                        failCount++;
                        Thread.sleep(5000L);
                    }
                    Main.logger.info(result);
                    JSONObject infos = new JSONObject(result);
                    int success = infos.getInt("code");
                    if (success != 0) {
                        String Reason = "";
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
                        Utils.autoSendMsg(event, "解析失败: " + Reason
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
                    Utils.autoSendMsg(event, "视频: " + msg
                            + "\nAV号: AV" + AvID
                            + "\nBV号: " + BvID
                            + "\n标题: " + title
                            + "\n简介: " + description
                            + "\n分P数: " + videoCount
                            + "\n播放地址:\nhttps://bilibili.com/video/av" + AvID + "\nhttps://bilibili.com/video/" + BvID + "\n"
                            + "\n作者: " + ownerName
                            + "\n作者主页: https://space.bilibili.com/" + ownerId);
                } catch (Exception e) {
                    Utils.autoSendMsg(event, "解析错误: " + e);
                    e.printStackTrace();
                }
            }).start();
        }
        else if (msg.toLowerCase().startsWith("#ss") || msg.toLowerCase().startsWith("#ep")) {
            new Thread(() -> {
                try {
                    Map<String, Object> map = new HashMap<>();
                    if (msg.toLowerCase().startsWith("#ss")) {
                        map.put("season_id", msg.substring(3));
                    }else if (msg.toLowerCase().startsWith("#ep")) {
                        map.put("ep_id", msg.substring(3));
                    }
                    String result = HttpUtil.get("http://api.bilibili.com/pgc/view/web/season", map);
                    int failCount = 0;
                    while (result == null || result.equals("")) {
                        if (failCount > 5) {
                            Utils.autoSendMsg(event, "解析超时");
                            return;
                        }
                        result = HttpUtil.post("http://api.bilibili.cn/view/" + msg, map);
                        failCount++;
                        Thread.sleep(5000L);
                    }
                    JSONObject infos = new JSONObject(result);
                    if (infos.getInt("code") != 0) {
                        Utils.autoSendMsg(event, "无法找到番剧: " + infos.getStr("message")
                                + "\n错误码: " + infos.getInt("code"));
                        return;
                    }
                    JSONObject publish = infos.getJSONObject("result").getJSONObject("publish");
                    boolean finished = (publish.getInt("is_finish") == 1);
                    String publishTime = publish.getStr("pub_time");
                    JSONObject newestEP = infos.getJSONObject("result").getJSONObject("new_ep");
                    String title = infos.getJSONObject("result").getStr("title");
                    int seasonId = infos.getJSONObject("result").getInt("season_id");
                    Utils.autoSendMsg(event, "番剧: " + msg + "\n" +
                            "番剧名: " + title + "\n" +
                            "已完结: " + finished + "\n" +
                            "上映时间: " + publishTime + "\n" +
                            "番剧播放地址: https://www.bilibili.com/bangumi/play/ss" + seasonId + "\n" +
                            "最新集播放地址: https://www.bilibili.com/bangumi/play/ep" + newestEP.getInt("id") + "\n" +
                            "最新集: " + newestEP.getStr("title"));
                } catch (Exception e) {
                    Utils.autoSendMsg(event, "解析错误: " + e);
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
