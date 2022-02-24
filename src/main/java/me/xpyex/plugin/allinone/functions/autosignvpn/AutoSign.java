package me.xpyex.plugin.allinone.functions.autosignvpn;

import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import me.xpyex.plugin.allinone.Main;
import me.xpyex.plugin.allinone.Utils;
import me.xpyex.plugin.allinone.functions.informs.MsgToOwner;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.HashMap;
import java.util.Map;

public class AutoSign {
    public static void load() {
        Main.logger.info(" AutoSign模块已加载");
        String a = CronUtil.schedule("1 0 2 * * *", (Task) AutoSign::run);
    }
    public static void run() {
        MsgToOwner.sendMsgToOwner("现在时间是: " + Utils.getTimeOfNow());
        Map<String, Object> Map = new HashMap<>();
        Map.put("email", "XPYEX0@163.com");
        Map.put("passwd", "15880561966--");
        String[] result = new String[2];
        result[0] = UnicodeUtil.toString(HttpUtil.post("https://ass.1145141919810.ltd/auth/login", Map));
        result[1] = UnicodeUtil.toString(HttpUtil.post("https://ass.1145141919810.ltd/user/checkin", new HashMap<>(0)));
        int failCount = 0;
        while (result[0] == null || result[0].equals("")) {
            if (failCount > 5) {
                Utils.sendMsgToOwner("失败超过5次，终止登录");
                return;
            }
            Utils.sendMsgToOwner("无法获取登录结果，5秒后重试");
            result[0] = UnicodeUtil.toString(HttpUtil.post("https://ass.1145141919810.ltd/auth/login", Map));
            failCount++;
            try {
                Thread.sleep(5000L);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        failCount = 0;
        while (result[1] == null || result[1].equals("")) {
            if (failCount > 5) {
                Utils.sendMsgToOwner("失败超过5次，终止登录");
                return;
            }
            Utils.sendMsgToOwner("无法获取打卡结果，5秒后重试");
            result[1] = UnicodeUtil.toString(HttpUtil.post("https://ass.1145141919810.ltd/user/checkin", new HashMap<>(0)));
            failCount++;
            try {
                Thread.sleep(5000L);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Main.logger.info("任务已执行");
        Main.logger.info("结果: \n" + result[0] + "\n" + result[1]);
        //Utils.sendMsgToOwner("结果: \n" + result[0] + "\n" + result[1]);
        JSONObject[] Return = new JSONObject[2];
        Return[0] = new JSONObject(result[0]);
        Return[1] = new JSONObject(result[1]);
        boolean successLogin = Return[0].getInt("ret") == 1;
        boolean successSign = Return[1].getInt("ret") == 1;
        if (!successLogin) {
            String failReason = Return[0].getStr("msg");
            Utils.sendMsgToOwner("登录失败: " + failReason);
            return;
        }
        if (!successSign) {
            String failReason = Return[1].getStr("msg");
            Utils.sendMsgToOwner("打卡失败: " + failReason);
            return;
        }
        Utils.sendMsgToOwner("打卡成功: \n" + Return[1].getStr("msg"));
    }
    public static void Execute(MessageEvent event) {
        if (Utils.getNormalText(event.getMessage()).equalsIgnoreCase("/vpnTest")) {
           run();
        }
    }
}
