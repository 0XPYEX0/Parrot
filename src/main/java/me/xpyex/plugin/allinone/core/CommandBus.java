package me.xpyex.plugin.allinone.core;

import cn.hutool.core.util.ClassUtil;
import java.util.ArrayList;
import me.xpyex.plugin.allinone.api.XPTuple;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;

public class CommandBus {
    private static final ArrayList<XPTuple> COMMAND_BUSES = new ArrayList<>();

    public <C extends Contact> CommandBus(Class<C> contactType, Model model, CommandExecutor<C> executor) {
        COMMAND_BUSES.add(new XPTuple(contactType, model, executor));
        //
    }

    public static void callCommands(MessageEvent event, String msg) {
        String cmd = msg.split(" ")[0];
        String[] args = msg.substring(cmd.length()).trim().split(" ");
        if (args.length == 1 && args[0].trim().isEmpty()) {
            args = new String[0];
        }
        for (XPTuple commandBus : COMMAND_BUSES) {
            if (ClassUtil.isAssignable(commandBus.get(1), Util.getRealSender(event).getClass())) {
                if (!Model.DISABLED_MODELS.contains(commandBus.get(2, Model.class))) {
                    if (CommandsList.isCmd(commandBus.get(2), cmd)) {
                        CommandExecutor executor = commandBus.get(3);
                        executor.execute(Util.getRealSender(event), event.getSender(), cmd.substring(1), args);
                    }
                }
            }
        }
    }
}
