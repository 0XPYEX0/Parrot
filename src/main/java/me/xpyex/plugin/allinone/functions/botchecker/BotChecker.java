package me.xpyex.plugin.allinone.functions.botchecker;

import me.xpyex.plugin.allinone.Utils;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;

import java.util.HashMap;
import java.util.Random;

public class BotChecker {
    public static boolean enableMode = true;
    public static HashMap<Long, Integer> answers = new HashMap<>();
    public static HashMap<Long, Long> group = new HashMap<>();
    //public static HashMap<Long, Long> checkTime = new HashMap<>();
    public static void Execute(MemberJoinEvent event) {
        if (!enableMode) {
            return;
        }
        if (event.getGroup().getBotAsMember().getPermission().getLevel() < 1) {
            return;
        }
        /*
        try {
            Thread.sleep(1000L);
        } catch (Throwable ignored) {}
         */
        event.getMember().mute(1200); //seconds
        event.getGroup().sendMessage(new At(event.getMember().getId()).plus(" ，欢迎！\n请在私聊中完成验证以确认你是人类\n验证成功后将会解除禁言\n十分钟内未验证完成将被请出"));
        int random_i1 = new Random().nextInt(50);
        int random_i2 = new Random().nextInt(50);
        answers.put(event.getMember().getId(), random_i1 + random_i2);
        event.getMember().sendMessage("请计算\n" + random_i1 + "+" + random_i2 + "\n的结果，并发送");
        group.put(event.getMember().getId(), event.getGroup().getId());
        //checkTime.put(event.getMember().getId(), 0L);
        new Thread(() -> {
            try {
                Thread.sleep(10*60*1000);
                if (!event.getMember().isMuted()) {
                    answers.remove(event.getMember().getId());
                    group.remove(event.getMember().getId());
                    return;
                }
                if (answers.containsKey(event.getMember().getId())) {
                    event.getGroup().sendMessage(new At(event.getMember().getId()).plus(" 验证超时，已请出"));
                    event.getMember().kick("");
                    answers.remove(event.getMember().getId());
                    group.remove(event.getMember().getId());
                }
            } catch (Throwable ignored) {}
        }).start();
    }
    public static void Execute(MessageEvent event) {
        if (!enableMode) {
            return;
        }
        if (Utils.isGroupEvent(event)) {
            return;
        }
        if (!answers.containsKey(event.getSender().getId())) {
            return;
        }
        if (Utils.getNormalText(event.getMessage()).contains("自动回复")) {
            return;
        }
        if (Utils.getNormalText(event.getMessage()).equals(answers.get(event.getSender().getId()) + "")) {
            answers.remove(event.getSender().getId());
            event.getSender().sendMessage("你已通过验证");
            NormalMember target;
            try {
                target = Utils.getBot().getGroup(group.get(event.getSender().getId())).get(event.getSender().getId());
            } catch (Throwable ignored) {
                return;
            }
            Utils.sendGroupMsg(group.get(event.getSender().getId()), new At(event.getSender().getId()).plus(" 已通过验证."));
            assert target != null;
            target.unmute();
            group.remove(event.getSender().getId());
            return;
        }
        event.getSender().sendMessage("验证错误，请重试");
    }
    public static void setEnableMode(boolean mode) {
        enableMode = mode;
    }
}
