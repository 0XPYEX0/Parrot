package me.xpyex.plugin.parrot.mirai.module;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import me.xpyex.plugin.parrot.mirai.api.CommandMenu;
import me.xpyex.plugin.parrot.mirai.core.command.CommandNode;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import net.mamoe.mirai.contact.Contact;

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
        registerCommand(Contact.class,
            CommandNode.of((source, sender, nodeArgSelf, argsLater) -> {
                    new CommandMenu(nodeArgSelf)
                        .add("劳动 [长度]", "生成当日劳动码")
                        .add("数字 [长度]", "在范围内生成随机数")
                        .add("字母 [长度]", "在长度范围随机生成字母")
                        .add("随机 [长度]", "字母+数字")
                        .send(source);
                })
                .child(CommandNode.of((source, sender, nodeArgSelf, argsLater) -> {
                    int length = argsLater.length == 0 ? 4 : Integer.parseInt(argsLater[0]);
                    source.sendMessage("今日劳动码: " + FORMATTER.format(new Date()) + "-" + getRandomCode(length, 0));
                }), "劳动", "LaoDong")
                .child(CommandNode.of((source, sender, nodeArgSelf, argsLater) -> {
                    int length = argsLater.length == 0 ? 4 : Integer.parseInt(argsLater[0]);
                    source.sendMessage(getRandomCode(length, -1));
                }), "数字", "num", "number")
                .child(CommandNode.of((source, sender, nodeArgSelf, argsLater) -> {
                    int length = argsLater.length == 0 ? 4 : Integer.parseInt(argsLater[0]);
                    source.sendMessage(getRandomCode(length, 0));
                }))
                .child(CommandNode.of((source, sender, nodeArgSelf, argsLater) -> {
                    int length = argsLater.length == 0 ? 4 : Integer.parseInt(argsLater[0]);
                    source.sendMessage(getRandomCode(length, 1));
                }))
            , "codes");
    }
}
