package me.xpyex.plugin.allinone.model;

import cn.hutool.http.HttpUtil;
import java.util.Random;
import me.xpyex.plugin.allinone.api.CommandMessager;
import me.xpyex.plugin.allinone.core.Model;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.event.events.GroupMessageEvent;

public class CloudItChecker extends Model {
    private static final String URL = "http://SQ-A1.NatServer.cn:1201";
    private static final String USER_AUTH_URL = URL + "/api/user/auth";
    private static final String USER_UNAUTH_URL = URL + "/api/user/unauth";
    private static final String PLUGIN_REG_URL = URL + "/api/plugin/reg";
    private static final String PLUGIN_UNREG_URL = URL + "/api/plugin/unreg";

    @Override
    public void register() {
        listenEvent(GroupMessageEvent.class, (event) -> {
            if (event.getGroup().getId() == 1013047198 || (event.getGroup().getId() == 717324776 && event.getSender().getPermission().getLevel() >= 1)) {
                String[] cmds = Util.getPlainText(event.getMessage()).split(" ");
                try {
                    if (cmds[0].equalsIgnoreCase("$auth")) {
                        if (cmds.length != 4) {
                            event.getGroup().sendMessage("参数不足");
                            return;
                        }
                        String pluginName = cmds[1];
                        String ip = cmds[2];
                        String hwid = cmds[3];
                        String url = USER_AUTH_URL + "/pluginName/" + pluginName + "/ip/" + ip + "/hwid/" + hwid + "/cipher/g9AYgrOmr6Mjh1QmboaNWyhN0Y5gklEqEvXP0NzlhgEJKCHyeCxmCaG5atzSyvap";
                        String result = HttpUtil.get(url);
                        CommandMessager messager = new CommandMessager("添加用户验证")
                                .plus("插件名: " + pluginName)
                                .plus("IP: " + ip)
                                .plus("HWID: " + hwid)
                                .plus("验证结果: " + result);
                        event.getGroup().sendMessage(messager.toString());
                    } else if (cmds[0].equalsIgnoreCase("$unauth")) {
                        if (cmds.length != 4) {
                            event.getGroup().sendMessage("参数不足");
                            return;
                        }
                        String pluginName = cmds[1];
                        String ip = cmds[2];
                        String hwid = cmds[3];
                        String url = USER_UNAUTH_URL + "/pluginName/" + pluginName + "/ip/" + ip + "/hwid/" + hwid + "/cipher/iqU2DTRiQTG8H6VrJhSehritd9emYE7EDTuBybNj8RwSQ461LULK0ea9uHOqNcT8";
                        String result = HttpUtil.get(url);
                        CommandMessager messager = new CommandMessager("移除用户验证")
                                .plus("插件名: " + pluginName)
                                .plus("IP: " + ip)
                                .plus("HWID: " + hwid)
                                .plus("验证结果: " + result);
                        event.getGroup().sendMessage(messager.toString());
                    } else if (cmds[0].equalsIgnoreCase("$reg")) {
                        if (cmds.length != 2) {
                            event.getGroup().sendMessage("参数不足");
                            return;
                        }
                        String pluginName = cmds[1];
                        String key = generateRandomString(24);
                        String url = PLUGIN_REG_URL + "/pluginName/" + pluginName + "/key/" + key + "/cipher/LoU3D6emBfqclImdjjQ2mKYgCnnKiubU0EeBVD75GqoPTbMAxREdGsPlVZ6jmS03";
                        String result = HttpUtil.get(url);
                        CommandMessager messager = new CommandMessager("插件注册")
                                .plus("插件名: " + pluginName)
                                .plus("注册Key: " + key)
                                .plus("验证结果: " + result);
                        event.getGroup().sendMessage(messager.toString());
                    } else if (cmds[0].equalsIgnoreCase("$unreg")) {
                        if (cmds.length != 2) {
                            event.getGroup().sendMessage("参数不足");
                            return;
                        }
                        String pluginName = cmds[1];
                        String url = PLUGIN_UNREG_URL + "/pluginName/" + pluginName + "/cipher/PaNOr0MSKaSJeZ0Ha25mTdI2KCRIAlCmi4pwjQPpEuCJB435JPhXj5ZBN3CAtpWs";
                        String result = HttpUtil.get(url);
                        CommandMessager messager = new CommandMessager("取消插件注册")
                                .plus("插件名: " + pluginName)
                                .plus("验证结果: " + result);
                        event.getGroup().sendMessage(messager.toString());
                    }
                } catch (Exception e) {
                    event.getGroup().sendMessage("捕获异常: " + e);
                    Util.handleException(e);
                }
            }
        });
    }

    private static String generateRandomString(int length){
        Random random = new Random();
        String codes = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder code = new StringBuilder();
        for(int i = 0; i < length; i++){
            code.append(codes.charAt(random.nextInt(codes.length()-1)));
        }
        return code.toString();
    }
}
