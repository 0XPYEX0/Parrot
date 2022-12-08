package me.xpyex.plugin.allinone.core;

import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.ClassUtil;
import java.util.ArrayList;
import java.util.HashMap;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;

public class CommandBus {
    private static final ArrayList<Tuple> COMMAND_BUSES = new ArrayList<>();
    private static final HashMap<String, Model> COMMAND_LIST = new HashMap<>();

    public static void register(Model model, String... commands) {
        for (String cmd : commands) {
            cmd = (cmd.startsWith("#") ? cmd : ("#" + cmd)).toLowerCase();
            COMMAND_LIST.put(cmd, model);
        }
    }

    public static boolean isCmd(String cmd) {
        return COMMAND_LIST.containsKey((cmd.startsWith("#") ? cmd : ("#" + cmd)).toLowerCase());
        //
    }

    public static boolean isCmd(Model model, String cmd) {
        cmd = (cmd.startsWith("#") ? cmd : ("#" + cmd)).toLowerCase();
        return (COMMAND_LIST.containsKey(cmd) && COMMAND_LIST.get(cmd).equals(model));
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

    public static <C extends Contact> void takeInBus(Class<C> contactType, Model model, CommandExecutor<C> executor) {
        COMMAND_BUSES.add(new Tuple(contactType, model, executor));
        //
    }

    public static void callCommands(MessageEvent event, String msg) {
        //TODO  没有区分aliases，目前检查到是Model的命令直接触发！！
        //TODO  等待修复！！
        String cmd = msg.split(" ")[0];
        String[] args = msg.substring(cmd.length()).trim().split(" ");
        if (args.length == 1 && args[0].trim().isEmpty()) {
            args = new String[0];
        }
        for (Tuple commandBus : COMMAND_BUSES) {
            if (ClassUtil.isAssignable(commandBus.get(0), Util.getRealSender(event).getClass())) {
                Model model = commandBus.get(1);
                if (model.isEnabled()) {
                    if (isCmd(model, cmd)) {
                        CommandExecutor<Contact> executor = commandBus.get(2);
                        try {
                            executor.execute(Util.getRealSender(event), event.getSender(), cmd.substring(1), args);
                        } catch (Throwable e) {
                            Util.handleException(e, false);
                            Util.sendMsgToOwner("模块 " + model.getName() + " 在处理命令 " + cmd + " 时出现异常，已被捕获: " + e);
                        }
                    }
                }
            }
        }
    }
}
