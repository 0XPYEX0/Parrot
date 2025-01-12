package me.xpyex.plugin.parrot.mirai.core.command;

import java.util.HashMap;
import java.util.function.BiFunction;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.xpyex.plugin.parrot.mirai.api.TripleFunction;
import me.xpyex.plugin.parrot.mirai.core.mirai.ParrotContact;
import me.xpyex.plugin.parrot.mirai.utils.ValueUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;

public class CommandNode<C extends Contact> {
    private final HashMap<String, CommandNode<?>> children = new HashMap<>();
    @Setter
    public CommandNode<C> parent = null;
    private String permission = null;
    private String permMessage = "你没有权限执行此命令";
    @Setter
    @Accessors(chain = true)
    private CommandExecutor<C> executor = null;
    private BiFunction<ParrotContact<C>, ParrotContact<User>, Boolean> executableCheck = null;
    @Setter
    @Accessors(fluent = true)
    private TripleFunction<ParrotContact<C>, ParrotContact<User>, CommandArguments, Boolean> executableCheckWithArg = null;
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

    public CommandNode<C> executableCheck(BiFunction<ParrotContact<C>, ParrotContact<User>, Boolean> executableCheck) {
        this.executableCheck = executableCheck;
        return this;
    }

    public CommandNode<C> executableCheck(String permission) {
        return executableCheck(permission, null);
    }

    public CommandNode<C> executableCheck(String permission, String permMessage) {
        this.permission = permission;
        this.permMessage = permMessage == null ? this.permMessage : permMessage;
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

    public void execute(ParrotContact<C> source, ParrotContact<User> sender, CommandArguments arguments) throws Throwable {
        if (executableCheck != null && !executableCheck.apply(source, sender)) return;
        if (executableCheckWithArg != null && !executableCheckWithArg.apply(source, sender, arguments)) return;
        if (permission != null && !sender.hasPerm(permission)) {
            sender.sendMessage(permMessage);
            return;
        }

        if (arguments.hasMoreArg()) {
            CommandNode<C> commandNode = (CommandNode<C>) children.get(arguments.getArgument(0).toLowerCase());
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
