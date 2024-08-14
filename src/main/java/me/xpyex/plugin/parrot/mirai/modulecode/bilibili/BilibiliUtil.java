package me.xpyex.plugin.parrot.mirai.modulecode.bilibili;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import me.xpyex.plugin.parrot.mirai.api.MapBuilder;
import me.xpyex.plugin.parrot.mirai.api.MessageBuilder;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.module.Bilibili;
import me.xpyex.plugin.parrot.mirai.utils.MsgUtil;
import me.xpyex.plugin.parrot.mirai.utils.StringUtil;
import me.xpyex.plugin.parrot.mirai.utils.Util;
import me.xpyex.plugin.parrot.mirai.utils.ValueUtil;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;

public class BilibiliUtil {
    private static final String URL_BILIBILI_VIDEO = "bilibili.com/video/";
    private static final String URL_LIVE = "live.bilibili.com/";
    private static final String API_URL_BILIBILI_USER = "https://api.bilibili.com/x/space/acc/info";
    private static final String API_URL_BILIBILI_VIDEO = "https://api.bilibili.com/x/web-interface/view";
    private static final String API_URL_BILIBILI_DYNAMIC = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/get_dynamic_detail";
    private static final String API_URL_BILIBILI_LIVE_ROOM = "https://api.live.bilibili.com/room/v1/Room/get_info";
    private static final WeakHashMap<String, Message> VIDEO_CACHES = new WeakHashMap<>();

    public static Message getVideoInfo(String url) throws Exception {
        Module.getModule(Bilibili.class).info(url);
        String id = StringUtil.getStrBetweenKeywords(URL_BILIBILI_VIDEO + StringUtil.getStrBetweenKeywords(url, URL_BILIBILI_VIDEO, "?").split("\n")[0], URL_BILIBILI_VIDEO, "/");
        Module.getModule(Bilibili.class).info("getVideoInfo时截取到的ID: " + id);
        HashMap<String, Object> map = new HashMap<>();
        if (StringUtil.startsWithIgnoreCaseOr(id, "AV")) {
            map.put("aid", id.substring(2));
        } else if (StringUtil.startsWithIgnoreCaseOr(id, "BV")) {
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
                return VIDEO_CACHES.get("BV" + param.get("bvid"));
            }
        } else if (param.containsKey("bvid")) {
            if (VIDEO_CACHES.containsKey("BV" + param.get("bvid"))) {
                return VIDEO_CACHES.get("BV" + param.get("bvid"));
            }
        }
        String result = ValueUtil.repeatIfError(() -> {
            return HttpUtil.get(API_URL_BILIBILI_VIDEO, param);
        }, 5, 5000);
        if (result == null || result.isEmpty()) {
            return new PlainText("解析超时");
        }
        Module.getModule(Bilibili.class).info(result);
        JSONObject infos = new JSONObject(result);
        int failed = infos.getInt("code");
        if (failed != 0) {
            String reason = switch (failed) {
                case -400 -> "请求错误";
                case -403 -> "权限不足";
                case -404 -> "视频不存在";
                case 62002 -> "视频不可见(被锁定)";
                default -> "未知原因";
            };
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
        BigInteger authorId = authorInfo.getBigInteger("mid");
        String faceUrl = data.getStr("pic");

        String videoID = param.containsKey("aid") ? "AV" + param.get("aid") : "BV" + param.get("bvid");
        MessageBuilder messager = new MessageBuilder("")
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
        Message out = ValueUtil.repeatIfError(() -> MsgUtil.getForwardMsgBuilder(Util.getBot().getAsFriend())
                                                        .add(Util.getBot(), new PlainText("视频: " + videoID)
                                                                                .plus(Util.getBot().getFriend(Util.getBot().getId()).uploadImage(MsgUtil.getImage(faceUrl)))
                                                                                .plus(messager.toString())
                                                        )
                                                        .build(), 2, 3000);
        if (out == null) {
            return new PlainText("解析失败");
        }
        VIDEO_CACHES.put("AV" + AvID, out);
        VIDEO_CACHES.put(BvID, out);
        return out;
    }

    public static Message getUserInfo(BigInteger userID) throws Exception {
        Map<String, Object> param = MapBuilder.builder(String.class, Object.class).put("mid", (Object) userID).build();
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

        Module.getModule(Bilibili.class).info(result);
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
        String officialInfo = switch (official) {
            case 1, 2, 7 -> "个人认证: " + data.getJSONObject("official").getStr("title");
            case 3, 4, 5, 6 -> "企业认证: " + data.getJSONObject("official").getStr("title");
            default -> "无";
        };
        String vipInfo = switch (vip) {
            case 1 -> "月度大会员";
            case 2 -> switch (data.getJSONObject("vip").getJSONObject("label").getStr("label_theme")) {
                case "ten_annual_vip" -> "十年大会员";
                case "hundred_annual_vip" -> "百年大会员";
                default -> "年度大会员";
            };
            default -> "非大会员";
        };
        return MsgUtil.getForwardMsgBuilder(Util.getBot().getAsFriend())
                   .add(Util.getBot(), new PlainText("用户: " + userID + "\n" +
                                                         "昵称: " + name + "\n")
                                           .plus(Util.getBot().getFriend(Util.getBot().getId()).uploadImage(MsgUtil.getImage(faceURL)))
                                           .plus("性别: " + gender + "\n" +
                                                     "等级: LV" + level + "\n" +
                                                     "会员: " + vipInfo + "\n" +
                                                     "认证信息: " + officialInfo + "\n" +
                                                     "空间地址: https://space.bilibili.com/" + userID)).build();
    }

    public static Message getDynamicInfo(long ID) throws Exception {
        Module.getModule(Bilibili.class).info("getDynamicInfo时的ID: " + ID);
        if (true) {
            return new PlainText("动态: " + ID + "\n" +
                                     "暂不可用");
        }

        Map<String, Object> param = MapBuilder.builder(String.class, Object.class).put("dynamic_id", ID).build();
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
            String reason = switch (obj.getInt("code")) {
                default -> "未知原因";
            };
            return new PlainText("解析失败: " + reason + "\n" +
                                     "错误代码: " + obj.getInt("code") + "\n" +
                                     "错误信息: " + obj.getStr("message"));
        }

        JSONObject card = obj.getJSONObject("data").getJSONObject("card");

        MessageBuilder messager = new MessageBuilder()
                                      .plus("动态: " + ID)
                                      .plus("内容: " + card.getStr("desc"));
        return MsgUtil.getForwardMsgBuilder(Util.getBot().getAsFriend())
                   .add(Util.getBot(), new PlainText(messager.toString()))
                   .build();
    }

    public static Message getLiveInfo(BigInteger userID) throws Exception {
        Module.getModule(Bilibili.class).info("getLiveInfo时的ID: " + userID);
        String result = HttpUtil.get(API_URL_BILIBILI_LIVE_ROOM, MapBuilder.builder(String.class, Object.class).put("room_id", userID).build());

        Module.getModule(Bilibili.class).info("请求结果: " + result);

        JSONObject obj = new JSONObject(result);
        if (obj.getInt("code") != 0) {
            String reason = switch (obj.getInt("code")) {
                case 1 -> "直播间不存在";
                case -400 -> "请求错误";
                default -> "未知原因";
            };
            return new PlainText("解析失败: " + reason + "\n" +
                                     "错误代码: " + obj.getInt("code") + "\n" +
                                     "错误信息: " + obj.getStr("message"));
        }
        JSONObject data = obj.getJSONObject("data");
        BigInteger uID = data.getBigInteger("uid");
        String isOnline = switch (data.getInt("live_status")) {
            case 1 -> "直播中";
            case 2 -> "轮播中，主播不在线";
            default -> "未开播";
        };
        MessageBuilder messager1 = new MessageBuilder()
                                       .plus(StringUtil.getStrBetweenKeywords(getUserInfo(uID).contentToString(), "昵称: ", "\n") + " 的直播间:")
                                       .plus("标题: " + data.getStr("title"))
                                       .plus("描述: " + data.getStr("description").replace("<p>", "").replace("</p>", ""));
        MessageBuilder messager2 = new MessageBuilder("")
                                       .plus("开播状态: " + isOnline)
                                       .plus("直播间地址: https://" + URL_LIVE + data.getBigInteger("room_id"));
        Message image = null;
        if (data.containsKey("cover")) {
            image = Util.getBot().getFriend(Util.getBot().getId()).uploadImage(MsgUtil.getImage(data.getStr("cover")));
        }
        return MsgUtil.getForwardMsgBuilder(Util.getBot().getAsFriend())
                   .add(Util.getBot(), new PlainText(messager1.toString())
                                           .plus(null != image ? image : MsgUtil.getEmptyMessage())
                                           .plus(messager2.toString())
                                           .plus("\n")
                                           .plus(getUserInfo(uID)))
                   .build();
    }

    public static String getFixedID(String s) {
        Module.getModule(Bilibili.class).info("getFixedID中参数s为: " + s);
        return s.replaceAll("_|\\W", "");
    }
}
