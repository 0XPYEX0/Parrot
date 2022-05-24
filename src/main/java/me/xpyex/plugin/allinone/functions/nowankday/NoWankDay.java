package me.xpyex.plugin.allinone.functions.nowankday;

import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import java.math.BigInteger;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.event.events.GroupMessageEvent;

public class NoWankDay {
    //static String ignoreList = "";
    public static void load() {
        String a = CronUtil.schedule("* * * * * *", (Task) () -> {
            NoWankDayUtils.autoUpDayAll(1125581517L);
        });
    }
    public static void Execute(GroupMessageEvent event) {
        String msg = Util.getPlainText(event.getMessage());
        String[] cmd = msg.split(" ");
        if (cmd[0].equalsIgnoreCase("#day") || cmd[0].equalsIgnoreCase("/day")) {
            if (cmd.length == 1 || cmd[1].equalsIgnoreCase("help")) {
                String willReturn = cmd[0] + " up\n"
                        + cmd[0] + " reset\n"
                        + cmd[0] + " set <Integer>\n"
                        + cmd[0] + " setTime <Time>";
                event.getGroup().sendMessage(willReturn);
                return;
            }
            if (cmd[1].equalsIgnoreCase("up")) {
                BigInteger day = NoWankDayUtils.getDay(event.getSender());
                String nickName = NoWankDayUtils.getRealNamecard(event.getSender());
                day = day.add(BigInteger.ONE);
                Util.setNameCard(event,nickName + " (重设: " + NoWankDayUtils.getResetTime(event.getSender()) + " ,天数: " + day + ")");
                event.getGroup().sendMessage("已帮助你快速打卡\n当前时间: " + Util.getTimeOfNow());
                return;
            }
            if (cmd[1].equalsIgnoreCase("reset")) {
                String NC = NoWankDayUtils.getRealNamecard(event.getSender());
                Util.setNameCard(event,NC + " (重设: " + Util.getTimeOfNow() + " ,天数: 0)");
                event.getGroup().sendMessage("天数已重置\n当前时间: " + Util.getTimeOfNow());
                //ignoreList = ignoreList + event.getSender().getId() + ",";
                return;
            }
            if (cmd[1].equalsIgnoreCase("set")) {
                if (cmd.length < 3) {
                    event.getGroup().sendMessage("请填写天数");
                    return;
                }
                BigInteger day = new BigInteger("0");
                try {
                    day = new BigInteger(cmd[2]);
                } catch (Exception e) {
                    event.getGroup().sendMessage("nmsl，整数都不会输的？😅");
                    return;
                }
                if (NoWankDayUtils.isLessThan(day, BigInteger.ZERO)) {
                    event.getGroup().sendMessage("你天数还能是负的？宁可真他妈是小天才电话手表😅");
                    return;
                }
                String rnc = NoWankDayUtils.getRealNamecard(event.getSender());
                Util.setNameCard(event,rnc + " (重设: " + NoWankDayUtils.getResetTime(event.getSender()) + " ,天数: " + day + ")");
                event.getGroup().sendMessage("已将打卡天数设定为 " + day);
                return;
            }
            if (cmd[1].equalsIgnoreCase("setTime")) {
                if (cmd.length != 3) {
                    event.getGroup().sendMessage("请填写时间");
                    return;
                }
                if (!NoWankDayUtils.stringIsTime(cmd[2])) {
                    event.getGroup().sendMessage("时间格式是不是错了？标准格式如下\n当前时间: " + Util.getTimeOfNow());
                    return;
                }
                Util.setNameCard(event,NoWankDayUtils.getRealNamecard(event.getSender()) + " (重设: " + cmd[2] + " ,天数: " + NoWankDayUtils.getDay(event.getSender()) + ")");
                event.getGroup().sendMessage("已将重设时间改为 " + cmd[2]);
                return;
            }
            event.getGroup().sendMessage("未知子命令\n请使用 " + cmd[0] + " 查看帮助");
            return;
        }
        if (cmd[0].equalsIgnoreCase("#wank") || cmd[0].equalsIgnoreCase("/wank")) {
            if (cmd.length == 1 || cmd[1].equalsIgnoreCase("help")) {
                String willReturn = cmd[0] + " up" + "\n" + cmd[0] + " set <Integer>";
                event.getGroup().sendMessage(willReturn);
                return;
            }
            if (cmd[1].equalsIgnoreCase("up")) {
                BigInteger times = NoWankDayUtils.getTimes(event.getSender());
                String nickName = NoWankDayUtils.getRealNamecard(event.getSender());
                times = times.add(BigInteger.ONE);
                Util.setNameCard(event,nickName + " (共手冲 " + times + " 次)");
                event.getGroup().sendMessage("已帮助你快速打卡");
                return;
            }
            if (cmd[1].equalsIgnoreCase("set")) {
                if (cmd.length < 3) {
                    event.getGroup().sendMessage("请填写次数");
                    return;
                }
                BigInteger times = new BigInteger("0");
                try {
                    times = new BigInteger(cmd[2]);
                } catch (Exception e) {
                    event.getGroup().sendMessage("nmsl，整数都不会输的？😅");
                    return;
                }
                if (NoWankDayUtils.isLessThan(times, BigInteger.ZERO)) {
                    event.getGroup().sendMessage("你次数还能是负的？宁可真他妈是小天才电话手表😅");
                    return;
                }
                String rnc = NoWankDayUtils.getRealNamecard(event.getSender());
                Util.setNameCard(event,rnc + " (共手冲 " + times + " 次)");
                event.getGroup().sendMessage("已将打卡次数设定为 " + times);
                return;
            }
            event.getGroup().sendMessage("未知子命令\n请使用" + cmd[0] + "查看帮助");
            return;
        }
    }
}
