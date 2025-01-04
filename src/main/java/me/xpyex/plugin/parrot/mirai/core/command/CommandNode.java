package me.xpyex.plugin.parrot.mirai.core.command;

import java.util.HashMap;
import java.util.function.BiFunction;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.xpyex.plugin.parrot.mirai.api.TripleFunction;
import me.xpyex.plugin.parrot.mirai.core.mirai.ParrotContact;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.module.TestMsg;
import me.xpyex.plugin.parrot.mirai.utils.ValueUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;

public class CommandNode<C extends Contact> {
    private final HashMap<String, CommandNode<?>> children = new HashMap<>();

    @Setter
    @Getter
    public CommandNode<C> parent = null;

    @Getter
    @Setter
    @Accessors(chain = true)
    private CommandExecutor<C> executor = null;

    @Setter
    @Accessors(fluent = true)
    private BiFunction<ParrotContact<C>, ParrotContact<User>, Boolean> executableCheck = null;

    @Setter
    @Accessors(fluent = true)
    private TripleFunction<ParrotContact<C>, ParrotContact<User>, CommandArguments, Boolean> executableCheckWithArg = null;

    @Getter
    @Setter
    @Accessors(fluent = true)
    private CommandExecutor<C> notMatchedArg = null;

    public static <C extends Contact> CommandNode<C> of() {
        return new CommandNode<>();
    }

    public static <C extends Contact> CommandNode<C> of(CommandExecutor<C> executor) {
        CommandNode<C> node = of();
        return node.setExecutor(executor);
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

    public void execute(ParrotContact<C> source, ParrotContact<User> sender, CommandArguments arguments) throws Throwable {
        if (executableCheck != null && !executableCheck.apply(source, sender)) return;
        if (executableCheckWithArg != null && !executableCheckWithArg.apply(source, sender, arguments)) return;
        if (arguments.hasMoreArg()) {
            CommandNode<C> commandNode = (CommandNode<C>) children.get(Module.getModule(TestMsg.class).info(arguments.getArgument(0)).toLowerCase());
            if (commandNode != null) {
                commandNode.execute(source, sender, arguments.next());
                return;
            } else if (notMatchedArg != null) {
                notMatchedArg.execute(source, sender, arguments);
                return;
            }
        }
        if (executor != null) {
            executor.execute(source, sender, arguments);
        }
    }
}
