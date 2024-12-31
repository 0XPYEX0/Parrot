package me.xpyex.plugin.parrot.mirai.core.command;

import java.util.Arrays;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;
import me.xpyex.plugin.parrot.mirai.core.mirai.ParrotContact;
import me.xpyex.plugin.parrot.mirai.utils.ValueUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;

@Getter
public abstract class CommandNode<C extends Contact> {
    private CommandExecutor<C> executor = null;
    private final HashMap<String, CommandNode<?>> children = new HashMap<>();
    @Setter
    public CommandNode<C> parent = null;

    public static <C extends Contact> CommandNode<C> of() { return new CommandNode<>() {}; }

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
        for (String alias : argAliases) {
            ValueUtil.mustTrue("参数不应为空", () -> !alias.trim().isEmpty());
            children.put(alias.toLowerCase(), executor);
        }
        executor.setParent(this);
        return this;
    }

    public String describeSelf() { return "Do nothing"; }

    public String usage() { return ""; }

    public void execute(ParrotContact<C> source, ParrotContact<User> sender, String nodeArgSelf, String... argsLater) throws Throwable {
        if (argsLater != null && argsLater.length != 0) {
            CommandNode<C> commandNode = (CommandNode<C>) children.get(argsLater[0].toLowerCase());
            if (commandNode != null) {
                commandNode.execute(source, sender, nodeArgSelf + " " + argsLater[0], Arrays.copyOfRange(argsLater, 1, argsLater.length));
                return;
            }
        }
        if (executor != null) {
            executor.execute(source, sender, nodeArgSelf, argsLater);
        }
//        CommandNode<?> lastCalled = this;
//        String parentArg = nodeArgSelf;
//        for (int i = 0; i < argsLater.length; i++) {
//            CommandNode<?> node = lastCalled.getChildren().get(argsLater[i].toLowerCase());
//            if (node == null) {
//                String[] newArgs = Arrays.copyOfRange(argsLater, i, argsLater.length);
//                CommandExecutor<C> targetExecutor = (CommandExecutor<C>) lastCalled.getExecutor();
//                if (targetExecutor != null) targetExecutor.execute(source, sender, parentArg, newArgs);
//                break;
//            }
//            lastCalled = node;
//            parentArg = argsLater[i];
//        }
    }
}
