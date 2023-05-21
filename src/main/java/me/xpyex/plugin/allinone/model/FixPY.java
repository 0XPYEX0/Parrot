package me.xpyex.plugin.allinone.model;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.io.File;
import java.util.Calendar;
import me.xpyex.plugin.allinone.api.CommandMenu;
import me.xpyex.plugin.allinone.core.Model;
import me.xpyex.plugin.allinone.utils.FileUtil;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.contact.Friend;

public class FixPY extends Model {
    @Override
    public void register() throws Throwable {
        final File users = new File(getDataFolder(), "users.json");
        if (!users.exists()) {
            FileUtil.writeFile(users, JSONUtil.toJsonPrettyStr(new JSONObject()), false);
        }
        final JSONObject data = JSONUtil.parseObj(FileUtil.readFile(users));
        //声明常量(算是吧)

        runTaskTimer(() -> {
            for (String s : data.keySet()) {
                int nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                System.out.println(nowHour);
                Friend friend = Util.getBot().getFriend(Long.parseLong(s));
                if (friend != null) {
                    JSONObject userInfo = data.getJSONObject(s);
                    int startHour = userInfo.getInt("StartHour");
                    int endHour = userInfo.getInt("EndHour");
                    if (nowHour < startHour) {
                        continue;
                    }
                    if (nowHour > endHour && startHour < endHour) {
                        continue;
                    }
                    friend.sendMessage("嘿，半个小时过去了，该提肛了 :)" +
                                           "\n" +
                                           "执行 #提肛 ，我将会引导你开始 :)");
                }
            }
        }, 30 * 60, 30 * 60);

        registerCommand(Friend.class, (source, sender, label, args) -> {
            if (args.length == 0) {
                new CommandMenu(label)
                    .add("join <StartHour> <EndHour>", "加入提肛列表，")
                    .add("quit", "不再接收提肛提醒")
                    .send(source);
                return;
            }
            if (args[0].equalsIgnoreCase("join")) {
                if (args.length < 3) {
                    source.sendMessage("参数不足");
                    return;
                }
                try {
                    JSONObject hour = new JSONObject();
                    hour.set("StartHour", Integer.parseInt(args[1]));
                    hour.set("EndHour", Integer.parseInt(args[2]));
                    data.set(source.getId() + "", hour);
                    FileUtil.writeFile(users, JSONUtil.toJsonPrettyStr(data), true);
                    source.sendMessage("您将会在 " + Integer.parseInt(args[1]) + ":00 - " + Integer.parseInt(args[2]) + ":00 期间" +
                                           "\n" +
                                           "每间隔半小时被提醒一次" +
                                           "\n" +
                                           "如果您不再想接收到，可使用 #" + label + " quit");
                } catch (NumberFormatException ignored) {
                    source.sendMessage("请填入整数");
                }
                return;
            }
            if (args[0].equalsIgnoreCase("quit")) {
                data.remove(source.getId() + "");
                FileUtil.writeFile(users, JSONUtil.toJsonPrettyStr(data), true);
                source.sendMessage("您将不会再收到通知 :)");
                return;
            }
            source.sendMessage("未知子命令");
        }, "FixPY", "提肛");
    }
}
