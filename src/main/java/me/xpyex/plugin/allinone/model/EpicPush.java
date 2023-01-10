package me.xpyex.plugin.allinone.model;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import me.xpyex.plugin.allinone.Main;
import me.xpyex.plugin.allinone.core.Model;
import me.xpyex.plugin.allinone.utils.MsgUtil;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.ExternalResource;

@SuppressWarnings("unused")
public class EpicPush extends Model {
    private static File adminUser;
    private static File userData;
    private static File groupData;
    private static long admin;
    private static ArrayList<String> userList;
    private static ArrayList<String> groupList;

    public void init() {
        adminUser = Main.INSTANCE.resolveDataFile("admin.txt");
        userData = Main.INSTANCE.resolveDataFile("userData.txt");
        groupData = Main.INSTANCE.resolveDataFile("groupData.txt");
        userList = new ArrayList<>();
        groupList = new ArrayList<>();
        try {
            readAdminUser();
            readDataToList(userData, userList);
            readDataToList(groupData, groupList);
        } catch (IOException e) {
            e.printStackTrace();
            info("读取数据失败！");
        }
    }

    public void readAdminUser() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(adminUser);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String text;
        text = bufferedReader.readLine();
        admin = Long.parseLong(text);
    }

    private void readDataToList(File data, ArrayList<String> list) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(data);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String text;
        while ((text = bufferedReader.readLine()) != null) {
            list.add(text);
        }
        bufferedReader.close();
        inputStreamReader.close();
        fileInputStream.close();
    }

    private void writeListToFile(ArrayList<String> list, File data) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(data);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
        for (String s : list)
            outputStreamWriter.write(s + "\n");
        outputStreamWriter.close();
        fileOutputStream.close();
    }

    public void addUser(String userID) {
        userList.add(userID);
        //
    }

    public void addGroup(String groupID) {
        groupList.add(groupID);
        //
    }

    public String getJson() {
        String url = "https://store-site-backend-static.ak.epicgames.com/freeGamesPromotions?locale=zh-CN&country=CN&allowCountries=CN";
        return HttpUtil.get(url);
    }

    public void sendPushData(String str, Group group, Friend friend) {
        JSONObject jsonObject = JSONUtil.parseObj(str);
        JSONObject data = jsonObject.getJSONObject("data");
        JSONObject catalog = data.getJSONObject("Catalog");
        JSONObject searchStore = catalog.getJSONObject("searchStore");
        JSONArray elements = searchStore.getJSONArray("elements");
        for (int i = 0; i < elements.size(); i++) {
            String s = elements.getStr(i);
            JSONObject json = JSONUtil.parseObj(s);
            String game_title = json.getStr("title");
            String game_description = json.getStr("description");
            String game_seller = JSONUtil.parseObj(json.getStr("seller")).getStr("name");
            JSONObject price = JSONUtil.parseObj(json.getStr("price"));
            JSONObject totalPrice = JSONUtil.parseObj(price.getStr("totalPrice"));
            JSONObject fmtPrice = JSONUtil.parseObj(totalPrice.getStr("fmtPrice"));
            String game_originalprice = fmtPrice.getStr("originalPrice");
            String game_discountPrice = fmtPrice.getStr("discountPrice");
            JSONArray keyImages = json.getJSONArray("keyImages");
            JSONObject keyImage = JSONUtil.parseObj(keyImages.getStr(0));
            String game_keyimages = keyImage.getStr("url");
            if (game_discountPrice.equals("0") && !game_originalprice.equals("0")) {
                JSONObject promotions = json.getJSONObject("promotions");
                JSONArray promotionalOffers = promotions.getJSONArray("promotionalOffers");
                JSONObject promotionalOffer = JSONUtil.parseObj(promotionalOffers.getStr(0));
                JSONArray promotionalOffers1 = promotionalOffer.getJSONArray("promotionalOffers");
                JSONObject promotionalOffer1 = JSONUtil.parseObj(promotionalOffers1.getStr(0));
                String game_startdate = promotionalOffer1.getStr("startDate");
                game_startdate = game_startdate.split("\\.")[0];
                String game_enddate = promotionalOffer1.getStr("endDate");
                game_enddate = game_enddate.split("\\.")[0];

                String result = "游戏名:" + game_title + "\n描述:" + game_description + "\n发售商:" + game_seller + "\n\n原价:" + game_originalprice + "\n现价:" + game_discountPrice + "\n\n白嫖开始日期:" + game_startdate + "\n白嫖结束日期:" + game_enddate + "\n\n快去 Epic Game 领取吧~";
                try {
                    ExternalResource er = MsgUtil.getImage(game_keyimages);
                    if (friend != null) {
                        Image image = Contact.uploadImage(friend, er);
                        MessageChain mc = new MessageChainBuilder()
                                .append(image)
                                .append(result)
                                .build();
                        Bot bot = Bot.getInstances().get(0);
                        Objects.requireNonNull(bot.getFriend(friend.getId())).sendMessage(mc);
                    }
                    if (group != null) {
                        Image image = Contact.uploadImage(group, er);
                        MessageChain mc = new MessageChainBuilder()
                                .append(image)
                                .append(result)
                                .build();
                        Bot bot = Bot.getInstances().get(0);
                        Objects.requireNonNull(bot.getGroup(group.getId())).sendMessage(mc);
                    }
                    er.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public void register() {
        init();
        try {
            Timer timer = new Timer();
            long weekTime = 24 * 60 * 60 * 1000 * 7;
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-08 '19:00:00'");
            Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sdf.format(new Date()));
            if (System.currentTimeMillis() > startTime.getTime()) {
                startTime = new Date(startTime.getTime() + weekTime);
            }
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        String json = getJson();
                        Bot bot = Bot.getInstances().get(0);
                        for (String s : userList) {
                            sendPushData(json, null, bot.getFriend(Long.parseLong(s)));
                        }
                        for (String s : groupList) {
                            sendPushData(json, bot.getGroup(Long.parseLong(s)), null);
                        }
                    } catch (Exception e) {
                        info(e);
                    }
                }
            }, startTime, 24 * 60 * 60 * 1000 * 7);
        } catch (Exception e) {
            e.printStackTrace();
        }

        listenEvent(GroupMessageEvent.class, (event) -> {
            // 群组白嫖推送
            groupPush(event);

            // 触发群组推送
            pushToGroup(event);
        });

        listenEvent(FriendMessageEvent.class, (event) -> {
            // 好友白嫖推送
            userPush(event);

            // 触发好友推送
            pushToFriend(event);
        });
    }

    private void userPush(FriendMessageEvent event) {
        if (MsgUtil.getPlainText(event.getMessage()).equals("#开启白嫖推送")) {
            String id = String.valueOf(event.getSender().getId());
            if (userList.contains(id)) {
                event.getSender().sendMessage("当前已开启白嫖推送，无需重复开启");
                return;
            }
            addUser(id);
            try {
                writeListToFile(userList, userData);
                event.getSender().sendMessage("定时推送已开启，每周五晚定时推送");
            } catch (Exception e) {
                e.printStackTrace();
                info("fail to writeUser");
            }
        } else if (MsgUtil.getPlainText(event.getMessage()).equals("#关闭白嫖推送")) {
            String id = String.valueOf(event.getSender().getId());
            if (userList.contains(id)) {
                userList.remove(id);
                try {
                    writeListToFile(userList, userData);
                    event.getSender().sendMessage("定时推送已关闭");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else event.getSender().sendMessage("当前未开启推送！");
        }
    }

    private void groupPush(GroupMessageEvent event) {
        if (MsgUtil.getPlainText(event.getMessage()).equals("#开启白嫖推送")) {
            String id = String.valueOf(event.getGroup().getId());
            if (groupList.contains(id)) {
                event.getGroup().sendMessage("当前已开启白嫖推送，无需重复开启");
                return;
            }
            addGroup(id);
            try {
                writeListToFile(groupList, groupData);
                event.getGroup().sendMessage("定时推送已开启，每周五晚定时推送");
            } catch (Exception e) {
                e.printStackTrace();
                info("fail to writeGroup");
            }
        } else if (MsgUtil.getPlainText(event.getMessage()).equals("#关闭白嫖推送")) {
            String id = String.valueOf(event.getGroup().getId());
            if (groupList.contains(id)) {
                groupList.remove(id);
                try {
                    writeListToFile(groupList, groupData);
                    event.getGroup().sendMessage("定时推送已关闭");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else event.getGroup().sendMessage("当前未开启推送！");
        }
    }

    private void pushToGroup(GroupMessageEvent event) {
        if (MsgUtil.getPlainText(event.getMessage()).equals("#白嫖")) {
            try {
                sendPushData(getJson(), event.getGroup(), null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void pushToFriend(FriendMessageEvent event) {
        if (MsgUtil.getPlainText(event.getMessage()).equals("#白嫖")) {
            try {
                sendPushData(getJson(), null, event.getFriend());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
