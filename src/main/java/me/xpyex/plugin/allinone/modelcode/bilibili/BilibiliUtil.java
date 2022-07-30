package me.xpyex.plugin.allinone.modelcode.bilibili;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import me.xpyex.plugin.allinone.Main;
import me.xpyex.plugin.allinone.api.CommandMessager;
import me.xpyex.plugin.allinone.utils.StringUtil;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;

@SuppressWarnings("all")  //不切实际的警告，编译器哪懂我(确信
public class BilibiliUtil {

    private static final String URL_BILIBILI_VIDEO = "bilibili.com/video/";
    private static final String API_URL_BILIBILI_USER = "https://api.bilibili.com/x/space/acc/info";
    private static final String API_URL_BILIBILI_VIDEO = "https://api.bilibili.com/x/web-interface/view";
    private static final String API_URL_BILIBILI_DYNAMIC = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/get_dynamic_detail";
    private static final String API_URL_BILIBILI_LIVE_ROOM = "https://api.live.bilibili.com/room/v1/Room/getRoomInfoOld";
    private static final HashMap<String, Message> VIDEO_CACHES = new HashMap<>();

    public static Message getVideoInfo(String url) throws Exception {
        Main.LOGGER.info(url);
        String id = StringUtil.getStrBetweenKeywords(URL_BILIBILI_VIDEO + StringUtil.getStrBetweenKeywords(url, URL_BILIBILI_VIDEO, "?").split("\n")[0], URL_BILIBILI_VIDEO, "/");
        Main.LOGGER.info("getVideoInfo时截取到的ID: " + id);
        HashMap<String, Object> map = new HashMap<>();
        if (StringUtil.startsWithIgnoreCase(id, "AV")) {
            map.put("aid", id.substring(2));
        } else if (StringUtil.startsWithIgnoreCase(id, "BV")) {
            if (id.length() != 12) {
                return null;
            }
            map.put("bvid", id.substring(2));
        } else {
            return null;
        }
        return getVideoInfo(map);
    }

    public static Message getVideoInfo(Map<String, Object> param) throws Exception {
        if (param.containsKey("aid")) {
            if (VIDEO_CACHES.containsKey("AV" + param.get("aid"))) {
                return VIDEO_CACHES.get("AV" + param.get("aid"));
            }
        } else if (param.containsKey("bvid")) {
            if (VIDEO_CACHES.containsKey("BV" + param.get("bvid"))) {
                return VIDEO_CACHES.get("BV" + param.get("bvid"));
            }
        }
        String result = HttpUtil.get(API_URL_BILIBILI_VIDEO, param);
        int failCount = 0;
        while (result == null || result.isEmpty()) {
            if (failCount > 5) {
                return new PlainText("解析超时");
            }
            result = HttpUtil.post(API_URL_BILIBILI_VIDEO, param);
            failCount++;
            Thread.sleep(5000L);
        }
        Main.LOGGER.info(result);
        JSONObject infos = new JSONObject(result);
        int failed = infos.getInt("code");
        if (failed != 0) {
            String reason;
            switch (failed) {
                case -400:
                    reason = "请求错误";
                    break;
                case -403:
                    reason = "权限不足";
                    break;
                case -404:
                    reason = "视频不存在";
                    break;
                case 62002:
                    reason = "视频不可见(被锁定)";
                    break;
                default:
                    reason = "未知原因";
                    break;
            }
            return new PlainText("解析失败: " + reason
                    + "\n错误码: " + failed
                    + "\n错误信息: " + infos.getStr("message"));
        }
        JSONObject data = infos.getJSONObject("data");
        int AvID = data.getInt("aid");
        String BvID = data.getStr("bvid");
        int videoCount = data.getInt("videos");
        String title = data.getStr("title");
        String description = data.getStr("desc");
        JSONObject authorInfo = data.getJSONObject("owner");
        String authorName = authorInfo.getStr("name");
        int authorId = authorInfo.getInt("mid");
        String faceUrl = data.getStr("pic");

        String videoID = param.containsKey("aid") ? "AV" + param.get("aid") : "BV" + param.get("bvid");
        CommandMessager messager = new CommandMessager("")
                .plus("AV号: AV" + AvID)
                .plus("BV号: " + BvID)
                .plus("标题: " + title)
                .plus("简介: " + description)
                .plus("分P数: " + videoCount)
                .plus("播放地址:")
                .plus("https://bilibili.com/video/av" + AvID)
                .plus("https://bilibili.com/video/" + BvID)
                .plus("")
                .plus("作者: " + authorName)
                .plus("作者主页: https://space.bilibili.com/" + authorId);
        Message out = new PlainText("视频: " + videoID)
                .plus(Util.getBot().getFriend(1723275529L).uploadImage(Util.getImage(faceUrl)))
                .plus(messager.toString());
        VIDEO_CACHES.put("AV" + AvID, out);
        VIDEO_CACHES.put(BvID, out);
        return out;
    }

    public static Message getUserInfo(int userID) throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put("mid", userID);
        String result = HttpUtil.get(API_URL_BILIBILI_USER, param);
        int failCount = 0;
        while (result == null || result.isEmpty()) {
            if (failCount > 5) {
                return new PlainText("解析超时");
            }
            result = HttpUtil.post(API_URL_BILIBILI_USER, param);
            failCount++;
            Thread.sleep(5000L);
        }

        Main.LOGGER.info(result);
        JSONObject infos = new JSONObject(result);
        int failed = infos.getInt("code");
        if (failed != 0) {
            String reason;
            if (failed == -400) {
                reason = "请求错误";
            } else {
                reason = "未知原因";
            }
            return new PlainText("查找用户失败: " + reason
                    + "\n错误码: " + failed
                    + "\n错误信息: " + infos.getStr("message"));
        }
        JSONObject data = infos.getJSONObject("data");
        String gender = data.getStr("sex");  //性别
        String name = data.getStr("name");  //昵称
        String faceURL = data.getStr("face");
        int level = data.getInt("level");  //等级
        int vip = data.getJSONObject("vip").getInt("type");  //0无，1月度，2年度+
        int official = data.getJSONObject("official").getInt("role");  //0无;1,2,7个人认证;3,4,5,6机构认证
        String officialInfo;
        switch (official) {
            case 1:
            case 2:
            case 7:
                officialInfo = "个人认证: " + data.getJSONObject("official").getStr("title");
                break;
            case 3:
            case 4:
            case 5:
            case 6:
                officialInfo = "企业认证: " + data.getJSONObject("official").getStr("title");
                break;
            default:
                officialInfo = "无";
                break;
        }
        String vipInfo;
        switch (vip) {
            case 1:
                vipInfo = "月度大会员";
                break;
            case 2:
                switch (data.getJSONObject("vip").getJSONObject("label").getStr("label_theme")) {
                    case "ten_annual_vip":
                        vipInfo = "十年大会员";
                        break;
                    case "hundred_annual_vip":
                        vipInfo = "百年大会员";
                        break;
                    default:
                        vipInfo = "年度大会员";
                        break;
                }
                break;
            default:
                vipInfo = "非大会员";
        }
        return new PlainText("用户: " + userID + "\n" +
                "昵称: " + name + "\n")
                .plus(Util.getBot().getFriend(1723275529L).uploadImage(Util.getImage(faceURL)))
                .plus("性别: " + gender + "\n" +
                        "等级: LV" + level + "\n" +
                        "会员: " + vipInfo + "\n" +
                        "认证信息: " + officialInfo + "\n" +
                        "空间地址: https://space.bilibili.com/" + userID);
    }

    public static Message getDynamicInfo(long ID) throws Exception {
        Main.LOGGER.info("getDynamicInfo时的ID: " + ID);
        if (true) {
            return new PlainText("动态: " + ID + "\n" +
                    "暂不可用");
        }

        HashMap<String, Object> param = new HashMap<>();
        param.put("dynamic_id", ID);
        String result = HttpUtil.get(API_URL_BILIBILI_DYNAMIC, param);
        int failCount = 0;
        while (result == null || result.isEmpty()) {
            if (failCount > 5) {
                return new PlainText("解析超时");
            }
            result = HttpUtil.post(API_URL_BILIBILI_DYNAMIC, param);
            failCount++;
            Thread.sleep(5000L);
        }

        JSONObject obj = new JSONObject(result);
        if (obj.getInt("code") != 0) {
            String reason;
            switch (obj.getInt("code")) {
                default:
                    reason = "未知原因";
                    break;
            }
            return new PlainText("解析失败: " + reason + "\n" +
                    "错误代码: " + obj.getInt("code") + "\n" +
                    "错误信息: " + obj.getStr("message"));
        }

        JSONObject card = obj.getJSONObject("data").getJSONObject("card");

        CommandMessager messager = new CommandMessager()
                .plus("动态: " + ID)
                .plus("内容: " + card.getStr("desc"));
        return new PlainText(messager.toString());
    }

    public static Message getLiveInfo(int userID) throws Exception {
        Main.LOGGER.info("getLiveInfo时的ID: " + userID);

        HashMap<String, Object> param = new HashMap<>();
        param.put("mid", userID);
        String result = HttpUtil.get(API_URL_BILIBILI_LIVE_ROOM, param);

        Main.LOGGER.info("请求结果: " + result);

        JSONObject obj = new JSONObject(result);
        if (obj.getInt("code") != 0) {
            String reason;
            switch (obj.getInt("code")) {
                case -400:
                    reason = "请求错误";
                    break;
                default:
                    reason = "未知原因";
                    break;
            }
            return new PlainText("解析失败: " + reason + "\n" +
                    "错误代码: " + obj.getInt("code") + "\n" +
                    "错误信息: " + obj.getStr("message"));
        }
        JSONObject data = obj.getJSONObject("data");
        String isOnline = data.getInt("liveStatus") == 1 ? "已开播" : "未开播";
        CommandMessager messager1 = new CommandMessager()
                .plus("直播间: " + userID)
                .plus("标题: " + data.getStr("title"));
        CommandMessager messager2 = new CommandMessager("")
                .plus("开播状态: " + isOnline)
                .plus("直播间地址: " + data.getStr("url"));
        return new PlainText(messager1.toString())
                .plus(Util.getBot().getFriend(1723275529L).uploadImage(Util.getImage(data.getStr("cover"))))
                .plus(messager2.toString())
                .plus("\n")
                .plus(getUserInfo(userID));
    }

    public static String getFixedID(String s) {
        Main.LOGGER.info("getFixedID中参数s为: " + s);
        return s.replaceAll("_|\\W", "");
    }
}
