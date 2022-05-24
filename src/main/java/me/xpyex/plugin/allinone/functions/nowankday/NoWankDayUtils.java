package me.xpyex.plugin.allinone.functions.nowankday;

import java.math.BigInteger;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.PlainText;

public class NoWankDayUtils {
    public static BigInteger getDay(String name) {
        BigInteger willReturn = new BigInteger("0");
        String[] strings = name.split(" ");
        if(strings.length >= 3) {
            try {
                willReturn = new BigInteger(strings[strings.length - 1].replace(")", ""));
            } catch (Exception ignored) {}
        }
        return willReturn;
    }
    public static BigInteger getTimes(String name) {
        BigInteger willReturn = new BigInteger("0");
        String[] names = name.split(" ");
        if(names.length >= 3) {
            try {
                willReturn = new BigInteger(names[names.length - 2]);
            } catch (Exception ignored) {}
        }
        return willReturn;
    }
    public static boolean isLessThan(BigInteger a, BigInteger b) {
        return a.compareTo(b) < 0;
    }
    public static boolean isMoreThan(BigInteger a, BigInteger b) {
        return a.compareTo(b) > 0;
    }
    public static boolean isEquals(BigInteger a, BigInteger b) {
        return a.compareTo(b) == 0;
    }
    public static BigInteger getTimes(Member sender) {
        return getTimes(sender.getNameCard());
    }
    public static String getResetTime(String namecard) { //ID (重设: Time ,天数: Day)
        String NC[] = namecard.split(" ");
        String Time = "";
        try {
            Time = NC[NC.length - 3];
            if (!stringIsTime(Time)) {
                Time = Util.getTimeOfNow();
            }
        } catch (Exception ignored) {
            Time = Util.getTimeOfNow();
        }
        return Time;
    }
    public static String getResetTime(Member sender) {
        return getResetTime(sender.getNameCard());
    }
    public static BigInteger getDay(Member sender) {
        return getDay(sender.getNameCard());
    }
    public static String getRealNamecard(String nickName) {
        BigInteger day = getDay(nickName);
        BigInteger times = getTimes(nickName);
        String time = getResetTime(nickName);
        nickName = nickName.replace(" (重设: " + time + " ,天数: " + day + ")", "");
        nickName = nickName.replace(" (共手冲 " + times + " 次)", "");
        return nickName;
    }
    public static String getRealNamecard(Member sender) {
        return getRealNamecard(sender.getNameCard());
    }
    public static boolean stringIsTime(String check) {
        String[] numbers = check.split(":");
        if (numbers.length != 3) {
            return false;
        }
        if (numbers[0].length() != 2 || numbers[1].length() != 2 || numbers[2].length() != 2) {
            return false;
        }
        int hour;
        int minute;
        int second;
        try {
            hour = Integer.parseInt(numbers[0]);
            minute = Integer.parseInt(numbers[1]);
            second = Integer.parseInt(numbers[2]);
        } catch (Exception e) {
            return false;
        }
        return hour <= 23 && hour >= 0 && minute <= 59 && minute >= 0 && second <= 59 && second >= 0;
    }
    public static boolean hasWankDay(Member member) {
        return member.getNameCard().contains(" (重设: ") && member.getNameCard().contains(" ,天数: ");
    }
    public static void autoUpDayAll(Group QQGroup) {
        if (QQGroup == null) {
            return;
        }
        for (Member member : QQGroup.getMembers()) {
            if (!hasWankDay(member)) {
                continue;
            }
            /*
            if (NoWankDay.ignoreList.contains(member.getId() + ",")) {
                NoWankDay.ignoreList.replace(member.getId() + ",", "");
                continue;
            }
            */
            if (getResetTime(member).equals(Util.getTimeOfNow())) {
                autoUpDay(QQGroup,member);
                QQGroup.sendMessage(
                        new PlainText("已为 ")
                                .plus(new At(member.getId()))
                                .plus(" 自动打卡\n"
                                        + "当前时间: " + Util.getTimeOfNow()));
            }
        }
    }
    public static void autoUpDayAll(Long QQGroup) {
        autoUpDayAll(Util.getBot().getGroup(QQGroup));
    }
    public static NormalMember getNormalMember(Group QG, Member member) {
        return QG.get(member.getId());
    }
    public static void autoUpDay(Group QG, Member member) {
        getNormalMember(QG,member).setNameCard(getRealNamecard(member) + " (重设: " + getResetTime(member) + " ,天数: " + getDay(member).add(BigInteger.ONE) + ")");
    }
    public static void startTimer() {
        //NoWankDayTimer.run();
    }
}
