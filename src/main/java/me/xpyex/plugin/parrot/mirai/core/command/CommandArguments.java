package me.xpyex.plugin.parrot.mirai.core.command;

import java.util.Arrays;
import java.util.Optional;
import lombok.experimental.ExtensionMethod;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.ArgParser;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.StrParser;
import me.xpyex.plugin.parrot.mirai.utils.ValueUtil;

@ExtensionMethod(ArgParser.class)
public class CommandArguments {
    protected final String[] wholeCommand;
    private int currentIndex = 0;

    private CommandArguments(String... wholeCommand) {
        if (wholeCommand == null || wholeCommand.length == 0) throw new IllegalArgumentException("command is null");
        this.wholeCommand = wholeCommand;
    }

    public static CommandArguments of(String... wholeCommand) {
        return new CommandArguments(wholeCommand);
        //
    }

    public String buildWholeCommand() {
        return String.join(" ", wholeCommand);
        //
    }

    public <T> Optional<T> getLabel(int index, Class<? extends ArgParser> useParser, Class<T> parsedType) {
        ValueUtil.notNull("类型不应为null", useParser, parsedType);
        if (index > this.currentIndex)
            throw new ArrayIndexOutOfBoundsException("访问的Label超出范围. 你可能正在访问Argument?");

        return useParser.of().parse(() -> wholeCommand[index], parsedType);
    }

    public String getLabel(int index) {
        return getLabel(index, StrParser.class, String.class).orElse("null");
    }

    public String getLabelReverse(int index) {
        return getLabel(currentIndex - index);
    }

    public String[] getLabels() {
        return Arrays.copyOfRange(wholeCommand, 0, currentIndex);
    }

    public String buildLabels() {
        return String.join(" ", getLabels());
    }

    public <T> Optional<T> getArgument(int index, Class<? extends ArgParser> useParser, Class<T> parsedType) {
        ValueUtil.notNull("类型不应为null", useParser, parsedType);
        return useParser.of().parse(() -> wholeCommand[currentIndex + 1 + index], parsedType);
    }

    public String getArgument(int index) {
        return getArgument(index, StrParser.class, String.class).orElse("null");
    }

    public int getIntArg(int index, int def) {
        try {
            return getIntArg(index);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
            return def;
        }
    }

    public int getIntArg(int index) {
        return Integer.parseInt(getArgument(index));
    }

    public <T> Optional<T> getArgumentReverse(int index, Class<? extends ArgParser> useParser, Class<T> parsedType) {
        ValueUtil.notNull("类型不应为null", useParser, parsedType);
        return useParser.of().parse(() -> getArgumentReverse(index), parsedType);
    }

    public String getArgumentReverse(int index) {
        return wholeCommand[wholeCommand.length - index - 1];
    }

    public String[] getArguments() {
        return Arrays.copyOfRange(wholeCommand, currentIndex + 1, wholeCommand.length);
    }

    public String buildArguments() {
        return String.join(" ", getArguments());
    }

    public boolean hasMoreArg() {
        return hasEnoughArg(1);
    }

    public boolean hasEnoughArg(int count) {
        return wholeCommand.length - count < currentIndex;
    }

    protected CommandArguments next() {
        currentIndex++;
        return this;
    }
}
