package me.xpyex.plugin.allinone.module;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import me.xpyex.plugin.allinone.api.CommandMenu;
import me.xpyex.plugin.allinone.core.module.Module;
import me.xpyex.plugin.allinone.utils.StringUtil;
import net.mamoe.mirai.contact.User;

public class RandomCodes extends Module {
    private static final Random RANDOM = new Random();
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    private static final String[] LETTERS_WITH_NUMBERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".split("");
    private static final String[] NUMBERS = "0123456789".split("");
    private static final String[] LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".split("");

    private static String getRandomCode(int length, int useLetter) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < length; i++) {
            result.append(switch (useLetter) {
                case 0 -> LETTERS_WITH_NUMBERS[RANDOM.nextInt(LETTERS_WITH_NUMBERS.length - 1)];
                case -1 -> NUMBERS[RANDOM.nextInt(NUMBERS.length - 1)];
                case 1 -> LETTERS[RANDOM.nextInt(LETTERS.length - 1)];
                default -> throw new IllegalStateException("错误的状态");
            });
        }
        return result.toString();
    }

    @Override
    public void register() {
        registerCommand(User.class, (source, sender, label, args) -> {
            if (args.length == 0) {
                new CommandMenu(label)
                    .add("劳动 [长度]", "生成当日劳动码")
                    .add("数字 [长度]", "在范围内生成随机数")
                    .add("字母 [长度]", "在长度范围随机生成字母")
                    .add("随机 [长度]", "字母+数字")
                    .send(source);
                return;
            }
            int length = args.length == 1 ? 4 : Integer.parseInt(args[1]);
            if (StringUtil.equalsIgnoreCaseOr(args[0], "劳动", "LaoDong")) {
                source.sendMessage("今日劳动码: " + FORMATTER.format(new Date()) + "-" + getRandomCode(length, 0));
            } else if (StringUtil.equalsIgnoreCaseOr(args[0], "数字", "num", "number")) {
                source.sendMessage(getRandomCode(length, -1));
            } else if (StringUtil.equalsIgnoreCaseOr(args[0], "随机", "random")) {
                source.sendMessage(getRandomCode(length, 0));
            } else if (StringUtil.equalsIgnoreCaseOr(args[0], "字母", "letter")) {
                source.sendMessage(getRandomCode(length, 1));
            }
        }, "codes");
    }
}
