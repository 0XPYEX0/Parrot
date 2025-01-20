package me.xpyex.plugin.parrot.mirai.core.command;

import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.ClassUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.xpyex.plugin.parrot.mirai.ParrotPlugin;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.ArgParser;
import me.xpyex.plugin.parrot.mirai.core.reachable.MiraiContact;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.utils.ExceptionUtil;
import me.xpyex.plugin.parrot.utils.StringUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;

public class CommandBus {
    private static final ArrayList<Tuple> COMMAND_BUSES = new ArrayList<>();
    private static final HashMap<String, Module> COMMAND_LIST = new HashMap<>();

    public static boolean isCmd(String cmd) {
        return COMMAND_LIST.containsKey(cmd.toLowerCase());
        //
    }

    public static boolean isCmd(Module module, String cmd) {
        if (module == null || cmd == null) return false;
        return module.equals(COMMAND_LIST.get(cmd.toLowerCase()));
    }

    public static List<String> getCommands(Module module) {
        ArrayList<String> list = new ArrayList<>();
        for (String key : COMMAND_LIST.keySet()) {
            if (COMMAND_LIST.get(key) == module) {
                list.add(key);
            }
        }
        return list;
    }

    public static <C extends Contact> void takeInBus(Class<C> contactType, Module module, CommandNode<C> executor, String... aliases) {
        for (String alias : aliases) {
            COMMAND_LIST.put(alias.toLowerCase(), module);  //注册
        }
        COMMAND_BUSES.add(new Tuple(contactType, module, new Command<>(executor, aliases)));
    }

    /**
     * 使聊天对象执行命令
     *
     * @param contact   聊天对象，可以是任意Contact
     * @param sender    消息发送者，必须为User
     * @param arguments 执行的命令与命令参数
     */
    public static void dispatchCommand(MiraiContact<? extends Contact> contact, MiraiContact<User> sender, CommandArguments arguments) {
        if (!StringUtil.startsWithIgnoreCaseOr(arguments.getLabel(0), ParrotPlugin.CMD_PREFIX)) {
            arguments.wholeCommand[0] = "#" + arguments.wholeCommand[0];
        }
        for (Tuple commandBus : COMMAND_BUSES) {  //contactType, module, command
            if (ClassUtil.isAssignable(commandBus.get(0), contact.getContact().getClass())) {  //contactType
                Module module = commandBus.get(1);  //module
                if (module.isEnabled()) {
                    if (isCmd(module, arguments.getLabel(0).substring(1))) {
                        Command<?> command = commandBus.get(2);  //command
                        ArgParser.setParseObj(sender.getContact());
                        for (String alias : command.aliases()) {
                            if (alias.equalsIgnoreCase(arguments.getLabel(0).substring(1))) {
                                try {
                                    command.node().execute(((MiraiContact) contact), sender, arguments);
                                } catch (Throwable e) {
                                    ExceptionUtil.handleException(e, false, null, module);
                                    contact.sendMessage("模块 " + module.getName() + " 在处理命令 " + arguments.getLabel(0) + " 时出现异常，已被捕获: " + e);
                                }
                            }
                        }
                        ArgParser.setParseObj(null);
                    }
                }
            }
        }
    }

    /**
     * 使聊天对象执行命令
     *
     * @param contact   聊天对象，可以是任意Contact
     * @param sender    消息发送者，必须为User
     * @param arguments 执行的命令与命令参数
     */
    public static void dispatchCommand(Contact contact, User sender, CommandArguments arguments) {
        dispatchCommand(MiraiContact.of(contact), MiraiContact.of(sender), arguments);
        //
    }
}
