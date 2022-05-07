package me.xpyex.plugin.allinone.utils;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import java.util.Map;
import me.xpyex.plugin.allinone.Main;

public class BilibiliUtil {
    public static String getVideoInfo(Map<String, Object> param) throws Exception {
        String result = HttpUtil.get("http://api.bilibili.com/x/web-interface/view", param);
        int failCount = 0;
        while (result == null || result.isEmpty()) {
            if (failCount > 5) {
                return "解析超时";
            }
            result = HttpUtil.post("http://api.bilibili.com/x/web-interface/view", param);
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
            return "解析失败: " + Reason
                    + "\n错误码: " + success
                    + "\n错误信息: " + infos.getStr("message");
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

        String videoID = param.containsKey("aid") ? "av" + param.get("aid") : param.get("bvid") + "";
        return "视频: " + videoID
                + "\nAV号: AV" + AvID
                + "\nBV号: " + BvID
                + "\n标题: " + title
                + "\n简介: " + description
                + "\n分P数: " + videoCount
                + "\n播放地址:\nhttps://bilibili.com/video/av" + AvID + "\nhttps://bilibili.com/video/" + BvID + "\n"
                + "\n作者: " + ownerName
                + "\n作者主页: https://space.bilibili.com/" + ownerId;
    }
}
