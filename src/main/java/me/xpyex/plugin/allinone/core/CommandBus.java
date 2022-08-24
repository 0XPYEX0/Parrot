package me.xpyex.plugin.allinone.core;

import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.ClassUtil;
import java.util.ArrayList;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;

public class CommandBus {
    private static final ArrayList<Tuple> COMMAND_BUSES = new ArrayList<>();

    public static <C extends Contact> void takeInBus(Class<C> contactType, Model model, CommandExecutor<C> executor) {
        COMMAND_BUSES.add(new Tuple(contactType, model, executor));
        //
    }

    public static void callCommands(MessageEvent event, String msg) {
        String cmd = msg.split(" ")[0];
        String[] args = msg.substring(cmd.length()).trim().split(" ");
        if (args.length == 1 && args[0].trim().isEmpty()) {
            args = new String[0];
        }
        for (Tuple commandBus : COMMAND_BUSES) {
            if (ClassUtil.isAssignable(commandBus.get(0), Util.getRealSender(event).getClass())) {
                Model model = commandBus.get(1);
                if (!Model.DISABLED_MODELS.contains(model)) {
                    if (CommandsList.isCmd(commandBus.get(1), cmd)) {
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
