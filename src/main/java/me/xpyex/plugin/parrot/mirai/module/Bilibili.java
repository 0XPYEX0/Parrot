package me.xpyex.plugin.parrot.mirai.module;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.xpyex.plugin.parrot.mirai.api.MapBuilder;
import me.xpyex.plugin.parrot.mirai.api.MessageBuilder;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.utils.MsgUtil;
import me.xpyex.plugin.parrot.mirai.utils.StringUtil;
import me.xpyex.plugin.parrot.mirai.utils.Util;
import me.xpyex.plugin.parrot.mirai.utils.ValueUtil;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;

public final class Bilibili extends Module {
    private static final ExecutorService SERVICE = Executors.newSingleThreadExecutor();
    private static final String URL_B23 = "b23.tv/";
    private static final String URL_BILIBILI = "bilibili.com/video/";
    private static final String URL_SPACE = "space.bilibili.com/";
    private static final String URL_DYNAMIC = "bilibili.com/dynamic/";
    private static final String URL_LIVE = "live.bilibili.com/";
    private static final String URL_NEW_LIVE = URL_LIVE + "h5/";

    @Override
    public void register() {
        listenEvent(MessageEvent.class, event -> {
            SERVICE.submit(() -> {
                String msg = MsgUtil.getPlainText(event.getMessage()).replace("\\/", "/");
                if (StringUtil.startsWithIgnoreCaseOr(msg, "#AV", "#BV")) {
                    try {
                        autoSendMsg(event,
                            BilibiliUtil.getVideoInfo(
                                MapBuilder.builder(String.class, Object.class)
                                    .putIfTrue(StringUtil.startsWithIgnoreCaseOr(msg, "#AV"), () -> "aid", () -> msg.substring(3))
                                    .putIfTrue((StringUtil.startsWithIgnoreCaseOr(msg, "#BV") && msg.length() == 13), () -> "bvid", () -> msg.substring(3, 13))
                                    .build()
                            ));
                    } catch (Exception e) {
                        autoSendMsg(event, "解析错误: " + e);
                        handleException(e, true, event);
                    }
                } else if (StringUtil.startsWithIgnoreCaseOr(msg, "#ss", "#ep")) {
                    try {
                        String result = ValueUtil.repeatIfError(() -> {
                            HttpUtil.get("https://bilibili.com");
                            return HttpUtil.createGet("https://api.bilibili.com/pgc/view/web/season")
                                       .form(MapBuilder.builder(String.class, Object.class)
                                                 .putIfTrue(msg.toLowerCase().startsWith("#ss"), () -> "season_id", () -> msg.substring(3))
                                                 .putIfTrue(msg.toLowerCase().startsWith("#ep"), () -> "ep_id", () -> msg.substring(3))
                                                 .build())
                                       .execute().body();
                        }, 5, 5000);
                        JSONObject infos = new JSONObject(result);
                        if (infos.getInt("code") != 0) {
                            new MessageBuilder()
                                .plus("无法找到番剧: " + infos.getStr("message"))
                                .plus("错误码: " + infos.getInt("code"))
                                .send(event);
                            return;
                        }
                        JSONObject publish = infos.getJSONObject("result").getJSONObject("publish");
                        boolean finished = publish.getInt("is_finish") == 1;
                        String publishTime = publish.getStr("pub_time");
                        JSONObject newestEP = infos.getJSONObject("result").getJSONObject("new_ep");
                        String title = infos.getJSONObject("result").getStr("title");
                        int seasonId = infos.getJSONObject("result").getInt("season_id");
                        autoSendMsg(event,
                            MsgUtil.getForwardMsgBuilder(event)
                                .add(Util.getBot(),
                                    new MessageBuilder()
                                        .plus("番剧ID: " + msg.substring(1))
                                        .plus("番剧名: " + title)
                                        .plus("是否完结: " + (finished ? "已完结" : "连载中"))
                                        .plus("上映时间: " + publishTime)
                                        .plus("番剧播放地址: https://www.bilibili.com/bangumi/play/ss" + seasonId)
                                        .plus("最新集播放地址: https://www.bilibili.com/bangumi/play/ep" + newestEP.getInt("id"))
                                        .plus("最新集: " + newestEP.getStr("title"))
                                        .toMessage()
                                ).build()
                        );
                    } catch (Exception e) {
                        autoSendMsg(event, "解析错误: " + e);
                        handleException(e, true, event);
                    }
                } else if (StringUtil.startsWithIgnoreCaseOr(msg, "#search bilibili ")) {
                    try {
                        String keyword = msg.substring(17);
                        String result = ValueUtil.repeatIfError(() -> {
                            info(HttpRequest.of("https://bilibili.com").header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36 Edg/115.0.1901.188").headers());
                            return HttpUtil.createGet("https://api.bilibili.com/x/web-interface/search/all/v2")
                                       .form(MapBuilder.builder(String.class, Object.class)
                                                 .put("keyword", keyword)
                                                 .build())
                                       .execute().body();
                        }, 5, 5000);
                        if (result == null || result.isEmpty()) {
                            autoSendMsg(event, "搜索超时");
                            return;
                        }
                        JSONObject json = new JSONObject(result);
                        int code = json.getInt("code");
                        if (code != 0) {
                            new MessageBuilder()
                                .plus("搜索错误: 请求错误")
                                .plus("错误码: " + code)
                                .plus("错误信息: " + json.getStr("message"))
                                .send(event);
                            return;
                        }
                        JSONObject data = json.getJSONObject("data");
                        JSONArray results = data.getJSONArray("result").getJSONObject(10).getJSONArray("data");
                        int limit = 5;  //限制展示几条信息
                        MessageBuilder messager = new MessageBuilder("关键词: " + keyword)
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
                        autoSendMsg(event, MsgUtil.getForwardMsgBuilder(event)
                                               .add(MsgUtil.getRealSender(event, User.class),
                                                   messager.toMessage()
                                               ).build());
                    } catch (Exception e) {
                        autoSendMsg(event, "搜索错误: " + e);
                        handleException(e, true, event);
                    }
                } else if (StringUtil.startsWithIgnoreCaseOr(msg, "#user")) {
                    try {
                        BigInteger ID = new BigInteger(msg.substring(5).split("/")[0]);
                        autoSendMsg(event, BilibiliUtil.getUserInfo(ID));
                    } catch (Exception e) {
                        if (e instanceof NumberFormatException) {
                            autoSendMsg(event, "请输入正确的ID");
                        } else {
                            autoSendMsg(event, "出现错误: " + e);
                            handleException(e, true, event);
                        }
                    }
                } else if (StringUtil.containsIgnoreCase(msg, URL_BILIBILI)) {
                    try {
                        String id = BilibiliUtil.getFixedID(StringUtil.getStrBetweenKeywords(URL_BILIBILI + StringUtil.getStrBetweenKeywords(msg, URL_BILIBILI, "?"), URL_BILIBILI, "\"").split("\n")[0].split("/")[0]);
                        autoSendMsg(event, BilibiliUtil.getVideoInfo("https://" + URL_BILIBILI + id));
                    } catch (Exception e) {
                        handleException(e, true, event);
                        autoSendMsg(event, "解析错误: " + e);
                    }
                } else if (StringUtil.containsIgnoreCase(msg, URL_B23)) {
                    try {
                        String b23ID = BilibiliUtil.getFixedID(StringUtil.getStrBetweenKeywords(URL_B23 + StringUtil.getStrBetweenKeywords(msg, URL_B23, "?"), URL_B23, "\"").split("\n")[0].split("/")[0]);
                        String path = "https://" + URL_B23 + b23ID;
                        info("解析b23.tv链接时截取到的ID为: " + b23ID);
                        String reconnectLink = HttpUtil.createGet(path, false).execute().header("Location");

                        if (StringUtil.containsIgnoreCase(reconnectLink, URL_BILIBILI)) {
                            autoSendMsg(event, BilibiliUtil.getVideoInfo(reconnectLink));
                        } else if (StringUtil.containsIgnoreCase(reconnectLink, URL_SPACE)) {
                            String userID = StringUtil.getStrBetweenKeywords(reconnectLink, URL_SPACE, "?").split("\n")[0].split("/")[0].split(" ")[0];
                            autoSendMsg(event, BilibiliUtil.getUserInfo(new BigInteger(userID)));
                        } else if (StringUtil.containsIgnoreCase(reconnectLink, URL_DYNAMIC)) {
                            String dID = StringUtil.getStrBetweenKeywords(reconnectLink, URL_DYNAMIC, "?").split("\n")[0].split("/")[0].split(" ")[0];
                            autoSendMsg(event, BilibiliUtil.getDynamicInfo(Long.parseLong(dID)));
                        } else if (StringUtil.containsIgnoreCase(reconnectLink, URL_NEW_LIVE)) {
                            String liveID = StringUtil.getStrBetweenKeywords(reconnectLink, URL_NEW_LIVE, "?").split("\n")[0].split("/")[0].split(" ")[0];
                            autoSendMsg(event, BilibiliUtil.getLiveInfo(new BigInteger(liveID)));
                        } else if (StringUtil.containsIgnoreCase(reconnectLink, URL_LIVE)) {
                            String liveID = StringUtil.getStrBetweenKeywords(reconnectLink, URL_LIVE, "?").split("\n")[0].split("/")[0].split(" ")[0];
                            autoSendMsg(event, BilibiliUtil.getLiveInfo(new BigInteger(liveID)));
                        }
                    } catch (Exception e) {
                        handleException(e, true, event);
                        autoSendMsg(event, "解析错误: " + e);
                    }
                } else if (StringUtil.containsIgnoreCase(msg, URL_SPACE)) {
                    try {
                        String userID = BilibiliUtil.getFixedID(StringUtil.getStrBetweenKeywords(msg, URL_SPACE, "?").split("\n")[0].split("/")[0].split(" ")[0]);
                        autoSendMsg(event, BilibiliUtil.getUserInfo(new BigInteger(BilibiliUtil.getFixedID(userID))));
                    } catch (Exception e) {
                        handleException(e, true, event);
                        autoSendMsg(event, "解析错误: " + e);
                    }
                } else if (StringUtil.containsIgnoreCase(msg, URL_DYNAMIC)) {
                    try {
                        String dID = StringUtil.getStrBetweenKeywords(msg, URL_DYNAMIC, "?").split("\n")[0].split("/")[0].split(" ")[0];
                        autoSendMsg(event, BilibiliUtil.getDynamicInfo(Long.parseLong(BilibiliUtil.getFixedID(dID))));
                    } catch (Exception e) {
                        handleException(e, true, event);
                        autoSendMsg(event, "解析错误: " + e);
                    }
                } else if (StringUtil.containsIgnoreCase(msg, URL_NEW_LIVE)) {
                    try {
                        String liveID = StringUtil.getStrBetweenKeywords(msg, URL_NEW_LIVE, "?").split("\n")[0].split("/")[0].split(" ")[0];
                        autoSendMsg(event, BilibiliUtil.getLiveInfo(new BigInteger(liveID)));
                    } catch (Exception e) {
                        handleException(e, true, event);
                        autoSendMsg(event, "解析错误: " + e);
                    }
                } else if (StringUtil.containsIgnoreCase(msg, URL_LIVE)) {
                    try {
                        String liveID = StringUtil.getStrBetweenKeywords(msg, URL_LIVE, "?").split("\n")[0].split("/")[0].split(" ")[0];
                        autoSendMsg(event, BilibiliUtil.getLiveInfo(new BigInteger(BilibiliUtil.getFixedID(liveID))));
                    } catch (Exception e) {
                        handleException(e, true, event);
                        autoSendMsg(event, "解析错误: " + e);
                    }
                }
            });
        });
    }

    public static class BilibiliUtil {
        private static final String URL_BILIBILI_VIDEO = "bilibili.com/video/";
        private static final String URL_LIVE = "live.bilibili.com/";
        private static final String API_URL_BILIBILI_USER = "https://api.bilibili.com/x/space/acc/info";
        private static final String API_URL_BILIBILI_VIDEO = "https://api.bilibili.com/x/web-interface/view";
        private static final String API_URL_BILIBILI_DYNAMIC = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/get_dynamic_detail";
        private static final String API_URL_BILIBILI_LIVE_ROOM = "https://api.live.bilibili.com/room/v1/Room/get_info";
        private static final WeakHashMap<String, Message> VIDEO_CACHES = new WeakHashMap<>();

        public static Message getVideoInfo(String url) throws Exception {
            getModule(Bilibili.class).info(url);
            String id = StringUtil.getStrBetweenKeywords(URL_BILIBILI_VIDEO + StringUtil.getStrBetweenKeywords(url, URL_BILIBILI_VIDEO, "?").split("\n")[0], URL_BILIBILI_VIDEO, "/");
            getModule(Bilibili.class).info("getVideoInfo时截取到的ID: " + id);
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
            String result = ValueUtil.repeatIfError(() -> HttpUtil.get(API_URL_BILIBILI_VIDEO, param), 5, 5000);
            if (result == null || result.isEmpty()) {
                return new PlainText("解析超时");
            }
            getModule(Bilibili.class).info(result);
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
            BigInteger AvID = data.getBigInteger("aid");
            String BvID = data.getStr("bvid");
            int videoCount = data.getInt("videos");
            String title = data.getStr("title");
            String description = data.getStr("desc");
            JSONObject authorInfo = data.getJSONObject("owner");
            String authorName = authorInfo.getStr("name");
            BigInteger authorId = authorInfo.getBigInteger("mid");
            String faceUrl = data.getStr("pic");

            String videoID = param.containsKey("aid") ? "AV" + param.get("aid") : "BV" + param.get("bvid");
            Message out = ValueUtil.repeatIfError(() -> MsgUtil.getForwardMsgBuilder(Util.getBot().getAsFriend())
                                                            .add(Util.getBot(), new PlainText("视频: " + videoID + "\n")
                                                                                    .plus(Util.getBot().getFriend(Util.getBot().getId()).uploadImage(MsgUtil.getImage(faceUrl)))
                                                                                    .plus(new MessageBuilder()
                                                                                              .plus("AV号: AV" + AvID)
                                                                                              .plus("BV号: " + BvID)
                                                                                              .plus("标题: " + title)
                                                                                              .toString()))
                                                            .add(Util.getBot(), new PlainText("简介: \n" + description))
                                                            .add(Util.getBot(), new MessageBuilder()
                                                                                    .plus("分P数: " + videoCount)
                                                                                    .plus("播放地址:")
                                                                                    .plus("https://bilibili.com/video/av" + AvID)
                                                                                    .plus("https://bilibili.com/video/" + BvID)
                                                                                    .toMessage())
                                                            .add(Util.getBot(), new MessageBuilder()
                                                                                    .plus("作者: " + authorName)
                                                                                    .plus("作者主页: https://space.bilibili.com/" + authorId)
                                                                                    .toMessage())
                                                            .build(), 2, 3000);
            if (out == null) {
                return new PlainText("解析失败");
            }
            VIDEO_CACHES.put("AV" + AvID, out);
            VIDEO_CACHES.put(BvID, out);
            return out;
        }

        public static Message getUserInfo(BigInteger userID) throws Exception {
            Map<String, Object> param = MapBuilder.builder(String.class, Object.class).put("mid", userID).build();
            String result = ValueUtil.repeatIfError(() -> HttpUtil.get(API_URL_BILIBILI_USER, param), 5, 5000);
            if (result == null) {
                return new PlainText("解析超时");
            }

            getModule(Bilibili.class).info(result);
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
            getModule(Bilibili.class).info("getDynamicInfo时的ID: " + ID);
            if (true) {
                return new PlainText("动态: " + ID + "\n" +
                                         "暂不可用");
            }

            Map<String, Object> param = MapBuilder.builder(String.class, Object.class).put("dynamic_id", ID).build();
            String result = ValueUtil.repeatIfError(() -> HttpUtil.get(API_URL_BILIBILI_DYNAMIC, param), 5, 5000);
            if (result == null) {
                return new PlainText("解析超时");
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
            getModule(Bilibili.class).info("getLiveInfo时的ID: " + userID);
            String result = HttpUtil.get(API_URL_BILIBILI_LIVE_ROOM, MapBuilder.builder(String.class, Object.class).put("room_id", userID).build());

            getModule(Bilibili.class).info("请求结果: " + result);

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
            getModule(Bilibili.class).info("getFixedID中参数s为: " + s);
            return s.replaceAll("_|\\W", "");
        }
    }
}
