package me.xpyex.plugin.allinone.functions.qqlists;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;

import cn.hutool.json.JSONUtil;
import me.xpyex.plugin.allinone.Main;
import me.xpyex.plugin.allinone.Utils;
import me.xpyex.plugin.allinone.commands.CommandsList;

import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.event.events.FriendMessageEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Nide8Blacklist {
    static File root = new File("Nide8List");
    static File blacklistFile = new File("Nide8List/Blacklist.json");
    static JSONObject blacklist;

    public static void load() {
        CommandsList.register(Nide8Blacklist.class, "/nide8blacklist", "/nide8bl");
        boolean loadResult = true;
        if (!loadLists()) {
            loadResult = false;
        }
        boolean finalLoadResult = loadResult;
        GlobalEventChannel.INSTANCE.registerListenerHost(new SimpleListenerHost() {
            @EventHandler
            public void onOnline(BotOnlineEvent event) {
                if (!finalLoadResult) {
                    Utils.sendMsgToOwner("名单加载失败");
                }
            }
            @EventHandler
            public void onFriendMsg(FriendMessageEvent event) {
                if (event.getSender().getId() != 1723275529L) {
                    return;
                }
                String[] cmd = Utils.getNormalText(event.getMessage()).split(" ");
                if (CommandsList.isCmd(Nide8Blacklist.class, cmd[0])) {
                    if (!finalLoadResult) {
                        Utils.autoSendMsg(event, "名单加载失败，无法使用");
                        return;
                    }
                    if (cmd.length == 1) {
                        Utils.autoSendMsg(event, cmd[0] + " add <ID> [Reason] - 将某人添加至黑名单\n" +
                                cmd[0] + " remove <ID> - 将某人移出黑名单\n" +
                                cmd[0] + " check <ID> - 检查某人是否在黑名单内");
                        return;
                    }
                    if (cmd.length == 2) {
                        Utils.autoSendMsg(event, "参数不足\n执行 /" + cmd[0] + " 以查看帮助");
                        return;
                    }
                    try {
                        String memberId = Long.parseLong(cmd[2]) + "";
                        if (cmd[1].equalsIgnoreCase("add")) {
                            String reason = cmd.length == 3 ? "" : cmd[3];
                            if (blacklist.containsKey(memberId)) {
                                blacklist.getJSONObject(memberId).getJSONArray("reasons").add(reason);
                            } else {
                                JSONObject infos = new JSONObject();
                                JSONArray reasons = new JSONArray();
                                reasons.add(reason);
                                infos.put("reasons", reasons);
                                blacklist.put(memberId, infos);
                            }
                            FileWriter out = new FileWriter(blacklistFile);
                            out.write(JSONUtil.toJsonPrettyStr(blacklist));
                            out.flush();
                            out.close();
                            Utils.autoSendMsg(event, "已记录完成");
                        } else if (cmd[1].equalsIgnoreCase("remove")) {
                            blacklist.remove(memberId);
                            FileWriter out = new FileWriter(blacklistFile);
                            out.write(JSONUtil.toJsonPrettyStr(blacklist));
                            out.flush();
                            out.close();
                            Utils.autoSendMsg(event, "已删除");
                        } else if (cmd[1].equalsIgnoreCase("check")) {
                            if (blacklist.containsKey(memberId)) {
                                JSONArray reasons = blacklist.getJSONObject(memberId).getJSONArray("reasons");
                                String send = memberId + " 已被记录，理由如下:";
                                for (Object reason : reasons) {
                                    send = send + "\n" + reason;
                                }
                                Utils.autoSendMsg(event, send);
                            } else {
                                Utils.autoSendMsg(event, "查无此人");
                            }
                        } else {
                            Utils.autoSendMsg(event, "参数错误，请检查");
                        }
                    } catch (NumberFormatException ignored) {
                        Utils.autoSendMsg(event, "QQ号类型错误");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Utils.autoSendMsg(event, "输出文件错误");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Utils.autoSendMsg(event, "JSON转换错误");
                    }
                }
            }
        });
        Main.logger.info("Nide8Blacklist模块已加载");
    }
    public static boolean loadLists() {
        try {
            if (!root.exists()) {
                root.mkdirs();
            }
            if (!blacklistFile.exists()) {
                blacklistFile.createNewFile();
                FileWriter out = new FileWriter(blacklistFile);
                out.write(JSONUtil.toJsonPrettyStr(new JSONObject()));
                out.flush();
                out.close();
            }
            Scanner in = new Scanner(blacklistFile);
            String list = "";
            while (in.hasNext()) {
                list = list + in.next();
            }
            in.close();
            blacklist = new JSONObject(list);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
