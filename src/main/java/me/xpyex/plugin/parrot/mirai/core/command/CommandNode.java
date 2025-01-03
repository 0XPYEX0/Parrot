package me.xpyex.plugin.parrot.mirai.core.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiFunction;
import lombok.Getter;
import lombok.Setter;
import me.xpyex.plugin.parrot.mirai.api.TripleFunction;
import me.xpyex.plugin.parrot.mirai.core.mirai.ParrotContact;
import me.xpyex.plugin.parrot.mirai.utils.ValueUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;

public class CommandNode<C extends Contact> {
    @Getter
    private CommandExecutor<C> executor = null;
    @Getter
    private final HashMap<String, CommandNode<?>> children = new HashMap<>();
    @Setter
    @Getter
    public CommandNode<C> parent = null;
    private BiFunction<ParrotContact<C>, ParrotContact<User>, Boolean> executableCheck = null;
    private TripleFunction<ParrotContact<C>, ParrotContact<User>, String[], Boolean> tripleCheck = null;
    @Getter
    private CommandExecutor<C> notMatchedArg = null;

    public static <C extends Contact> CommandNode<C> of() { return new CommandNode<>(); }

    public static <C extends Contact> CommandNode<C> of(CommandExecutor<C> executor) {
        CommandNode<C> node = of();
        node.setExecutor(executor);
        return node;
    }

    public CommandNode<C> setExecutor(CommandExecutor<C> executor) {
        this.executor = executor;
        return this;
    }

    public CommandNode<C> child(CommandNode<C> executor, String... argAliases) {
        ValueUtil.notNull("child node不应为null", executor);
        ValueUtil.notEmpty("aliases为空，要注册什么？", (Object[]) argAliases);
        for (String alias : argAliases) {
            ValueUtil.mustTrue("参数不应为空", () -> !alias.trim().isEmpty());
            children.put(alias.toLowerCase(), executor);
        }
        executor.setParent(this);
        return this;
    }

    public CommandNode<C> executableCheck(BiFunction<ParrotContact<C>, ParrotContact<User>, Boolean> check) {
        this.executableCheck = check;
        return this;
    }

    public CommandNode<C> executableCheckWithCmdArg(TripleFunction<ParrotContact<C>, ParrotContact<User>, String[], Boolean> check) {
        this.tripleCheck = check;
        return this;
    }

    public CommandNode<C> notMatchedArg(CommandExecutor<C> notMatchedArg) {
        this.notMatchedArg = notMatchedArg;
        return this;
    }

    public void execute(ParrotContact<C> source, ParrotContact<User> sender, String[] nodeArgSelf, String... argsLater) throws Throwable {
        if (executableCheck != null && !executableCheck.apply(source, sender)) return;
        if (tripleCheck != null && !tripleCheck.apply(source, sender, nodeArgSelf)) return;
        if (argsLater != null && argsLater.length != 0) {
            CommandNode<C> commandNode = (CommandNode<C>) children.get(argsLater[0].toLowerCase());
            if (commandNode != null) {
                String[] label = Arrays.copyOf(nodeArgSelf, nodeArgSelf.length + 1);
                label[label.length - 1] = argsLater[0];
                commandNode.execute(source, sender, label, Arrays.copyOfRange(argsLater, 1, argsLater.length));
                return;
            } else if (notMatchedArg != null) {
                notMatchedArg.execute(source, sender, nodeArgSelf, argsLater);
                return;
            }
        }
        if (executor != null) {
            executor.execute(source, sender, nodeArgSelf, argsLater);
        }
    }
}
