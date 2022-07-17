package me.xpyex.plugin.allinone.core;

import cn.hutool.core.util.ClassUtil;
import java.util.ArrayList;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;

public class CommandBus {
    private final Class<? extends Contact> contactType;
    private final Model model;
    private final CommandExecutor<? extends Contact> executor;
    private static final ArrayList<CommandBus> COMMAND_BUSES = new ArrayList<>();

    public CommandBus(Class<? extends Contact> contactType, Model model, CommandExecutor<? extends Contact> executor) {
        this.contactType = contactType;
        this.model = model;
        this.executor = executor;
        COMMAND_BUSES.add(this);
    }

    public static void callCommands(MessageEvent event, String msg) {
        String cmd = msg.split(" ")[0];
        String[] args = msg.substring(cmd.length()).trim().split(" ");
        if (args.length == 1 && args[0].trim().isEmpty()) {
            args = new String[0];
        }
        for (CommandBus commandBus : COMMAND_BUSES) {
            if (ClassUtil.isAssignable(commandBus.contactType, Util.getRealSender(event).getClass())) {
                if (!Model.DISABLED_MODELS.contains(commandBus.model)) {
                    if (CommandsList.isCmd(commandBus.model, cmd)) {
                        CommandExecutor executor = commandBus.executor;
                        executor.execute(Util.getRealSender(event), event.getSender(), cmd.substring(1), args);
                    }
                }
            }
        }
    }
}
