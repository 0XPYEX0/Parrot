package me.xpyex.plugin.parrot.mirai.core.command;

import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.ClassUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.xpyex.plugin.parrot.mirai.core.command.argument.ArgParser;
import me.xpyex.plugin.parrot.mirai.core.mirai.ParrotContact;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.utils.ExceptionUtil;
import me.xpyex.plugin.parrot.mirai.utils.MsgUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;

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

    public static <C extends Contact> void takeInBus(Class<C> contactType, Module module, CommandExecutor<C> executor, String... aliases) {
        for (String alias : aliases) {
            COMMAND_LIST.put(alias.toLowerCase(), module);  //注册
        }
        COMMAND_BUSES.add(new Tuple(contactType, module, new Command<>(executor, aliases)));
        //
    }

    public static void callCommands(MessageEvent event, String msg) {
        String cmd = msg.split(" ")[0];
        String[] args = msg.substring(cmd.length()).trim().split(" ");
        if (args.length == 0 || (args.length == 1 && args[0].trim().isEmpty())) {
            args = new String[0];
        }
        ParrotContact<Contact> source = ParrotContact.of(MsgUtil.getRealSender(event));
        CommandExecutor.EVENT_POOL.put(source.getCreatedTime(), event);
        dispatchCommand(source, ParrotContact.of(event.getSender()), cmd, args);
    }

    /**
     * 使聊天对象执行命令
     *
     * @param contact 聊天对象，可以是任意Contact
     * @param sender  消息发送者，必须为User
     * @param cmd     执行的命令
     * @param args    命令参数
     */
    public static void dispatchCommand(ParrotContact<Contact> contact, ParrotContact<User> sender, String cmd, String... args) {
        if (!cmd.startsWith("#")) {
            cmd = "#" + cmd;
        }
        for (Tuple commandBus : COMMAND_BUSES) {  //contactType, module, command
            if (ClassUtil.isAssignable(commandBus.get(0), contact.getContact().getClass())) {  //contactType
                Module module = commandBus.get(1);  //module
                if (module.isEnabled()) {
                    if (isCmd(module, cmd.substring(1))) {
                        Command<Contact> command = commandBus.get(2);  //command
                        ArgParser.setParseObj(sender.getContact());
                        for (String alias : command.aliases()) {
                            if (alias.equalsIgnoreCase(cmd.substring(1))) {
                                try {
                                    command.executor().execute(contact, sender, cmd.substring(1), args);
                                } catch (Throwable e) {
                                    ExceptionUtil.handleException(e, false, null, module);
                                    MsgUtil.sendMsgToOwner("模块 " + module.getName() + " 在处理命令 " + cmd + " 时出现异常，已被捕获: " + e);
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
     * @param contact 聊天对象，可以是任意Contact
     * @param sender  消息发送者，必须为User
     * @param cmd     执行的命令
     * @param args    命令参数
     */
    public static void dispatchCommand(Contact contact, User sender, String cmd, String... args) {
        dispatchCommand(ParrotContact.of(contact), ParrotContact.of(sender), cmd, args);
        //
    }
}
