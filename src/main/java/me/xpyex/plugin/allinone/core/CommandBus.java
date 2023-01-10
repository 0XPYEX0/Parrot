package me.xpyex.plugin.allinone.core;

import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.ClassUtil;
import java.util.ArrayList;
import java.util.HashMap;
import me.xpyex.plugin.allinone.utils.ExceptionUtil;
import me.xpyex.plugin.allinone.utils.MsgUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;

public class CommandBus {
    private static final ArrayList<Tuple> COMMAND_BUSES = new ArrayList<>();
    private static final HashMap<String, Model> COMMAND_LIST = new HashMap<>();

    public static boolean isCmd(String cmd) {
        return COMMAND_LIST.containsKey(cmd.toLowerCase());
        //
    }

    public static boolean isCmd(Model model, String cmd) {
        return (COMMAND_LIST.containsKey(cmd.toLowerCase()) && COMMAND_LIST.get(cmd.toLowerCase()).equals(model));
    }

    public static String[] getCommands(Model model) {
        ArrayList<String> list = new ArrayList<>();
        for (String key : COMMAND_LIST.keySet()) {
            if (COMMAND_LIST.get(key) == model) {
                list.add(key);
            }
        }
        return list.toArray(new String[0]);
    }

    public static <C extends Contact> void takeInBus(Class<C> contactType, Model model, CommandExecutor<C> executor, String... aliases) {
        for (String alias : aliases) {
            COMMAND_LIST.put(alias.toLowerCase(), model);  //注册
        }
        COMMAND_BUSES.add(new Tuple(contactType, model, new Command<>(executor, aliases)));
        //
    }

    public static void callCommands(MessageEvent event, String msg) {
        String cmd = msg.split(" ")[0];
        String[] args = msg.substring(cmd.length()).trim().split(" ");
        if (args.length == 1 && args[0].trim().isEmpty()) {
            args = new String[0];
        }
        for (Tuple commandBus : COMMAND_BUSES) {
            if (ClassUtil.isAssignable(commandBus.get(0), MsgUtil.getRealSender(event).getClass())) {
                Model model = commandBus.get(1);
                if (model.isEnabled()) {
                    if (isCmd(model, cmd.substring(1))) {
                        Command<Contact> command = commandBus.get(2);
                        for (String alias : command.getAliases()) {
                            if (alias.equalsIgnoreCase(cmd.substring(1))) {
                                try {
                                    command.getExecutor().execute(MsgUtil.getRealSender(event), event.getSender(), cmd.substring(1), args);
                                } catch (Throwable e) {
                                    ExceptionUtil.handleException(e, false);
                                    MsgUtil.sendMsgToOwner("模块 " + model.getName() + " 在处理命令 " + cmd + " 时出现异常，已被捕获: " + e);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
