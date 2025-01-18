package me.xpyex.plugin.parrot.mirai.core.command;

import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.xpyex.plugin.parrot.api.TripleFunction;
import me.xpyex.plugin.parrot.mirai.core.mirai.ParrotContact;
import me.xpyex.plugin.parrot.mirai.utils.ValueUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.User;

public class CommandNode<C extends Contact> {
    private final HashMap<String, Object> children = new HashMap<>();
    private final WeakHashMap<Long, Long> cacheCooldown = new WeakHashMap<>();  //ID, time
    @Setter
    public CommandNode<C> parent = null;
    @Setter
    @Accessors(chain = true)
    private CommandExecutor<C> executor = null;
    @Setter
    @Accessors(fluent = true)
    private CommandExecutor<C> notMatchedArg = null;
    //CommandNode self


    private String permission = null;
    private MemberPermission passLevel = null;
    private BiConsumer<ParrotContact<C>, ParrotContact<User>> permAction = null;
    //Permissions check


    private BiFunction<ParrotContact<C>, ParrotContact<User>, Boolean> executableCheck = null;
    @Setter
    @Accessors(fluent = true)
    private TripleFunction<ParrotContact<C>, ParrotContact<User>, CommandArguments, Boolean> executableCheckWithArg = null;
    //Executable check


    private Integer cooldown = null;
    private BiConsumer<ParrotContact<C>, ParrotContact<User>> cooldownAction = null;
    //Cooldown check

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

    public CommandNode<C> permission(String permission) {
        return permission(permission, "你没有权限执行此命令");
    }

    public CommandNode<C> permission(String permission, MemberPermission passLevel) {
        return permission(permission, passLevel, "你没有权限执行此命令");
    }

    public CommandNode<C> permission(String permission, String permMessage) {
        return permission(permission, null, permMessage);
    }

    public CommandNode<C> permission(String permission, MemberPermission passLevel, String permMessage) {
        return permission(permission, passLevel, (source, sender) -> source.sendMessage(permMessage));
    }

    public CommandNode<C> permission(String permission, MemberPermission passLevel, BiConsumer<ParrotContact<C>, ParrotContact<User>> action) {
        this.permission = permission;
        this.passLevel = passLevel;
        this.permAction = action;
        return this;
    }

    public CommandNode<C> child(CommandNode<C> executor, String... argAliases) {
        ValueUtil.notNull("child node不应为null", executor);
        ValueUtil.notEmpty("aliases为空，要注册什么？", argAliases, ".");
        for (String alias : argAliases) {
            ValueUtil.mustTrue("参数不应为空", () -> !alias.trim().isEmpty());
            children.put(alias.toLowerCase(), argAliases[0].toLowerCase());
        }
        children.put(argAliases[0].toLowerCase(), executor);
        executor.setParent(this);
        return this;
    }

    public void execute(ParrotContact<C> source, ParrotContact<User> sender, CommandArguments arguments) throws Throwable {
        if (executableCheck != null && !executableCheck.apply(source, sender)) return;  //是否可执行
        if (executableCheckWithArg != null && !executableCheckWithArg.apply(source, sender, arguments))
            return;  //检查参数本身是否可执行
        if (permission != null && !sender.hasPerm(permission, passLevel)) {  //是否有权限
            permAction.accept(source, sender);
            return;
        }
        if (cooldown != null && cooldownAction != null) {  //是否在冷却
            long now = System.currentTimeMillis();
            long last = cacheCooldown.getOrDefault(sender.getId(), 0L);
            if (Math.abs(now - last) < cooldown * 1000) {
                cooldownAction.accept(source, sender);
                return;
            }
            cacheCooldown.put(sender.getId(), now);
        }

        //真正进入命令判断
        if (arguments.hasMoreArg()) {  //如果后面是有参数的
            CommandNode<C> commandNode = getChildren(arguments.getArgument(0).toLowerCase());
            if (commandNode != null) {  //看看是否有匹配的子命令
                commandNode.execute(source, sender, arguments.next());
                return;
            } else if (notMatchedArg != null) {  //无法匹配，但是参数依然需要处理，看看有没有填充不匹配要做什么
                notMatchedArg.execute(source, sender, arguments);
                return;
            }
        }
        if (executor != null) {
            executor.execute(source, sender, arguments);  //都没有，执行默认执行器
        }
    }

    public CommandNode<C> cooldown(int seconds) {
        return cooldown(seconds, (source, sender) -> source.sendMessage("冷却中，请稍后再试"));
    }

    public CommandNode<C> cooldown(int seconds, BiConsumer<ParrotContact<C>, ParrotContact<User>> cooldownAction) {
        this.cooldown = seconds;
        this.cooldownAction = cooldownAction;
        return this;
    }

    @SuppressWarnings("unchecked")
    private CommandNode<C> getChildren(String key) {
        Object nodeOrPointer = children.get(key.toLowerCase());
        if (nodeOrPointer instanceof CommandNode<?> node) return (CommandNode<C>) node;
        return getChildren(nodeOrPointer + "");
    }
}
