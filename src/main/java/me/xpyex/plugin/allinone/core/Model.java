package me.xpyex.plugin.allinone.core;

import cn.hutool.core.util.ClassUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;
import me.xpyex.plugin.allinone.Main;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.events.MessageEvent;

public abstract class Model {
    public static final HashSet<Model> MODELS = new HashSet<>();
    private static final HashMap<
            Class<? extends Event>,
            HashMap<Model, Consumer<? extends Event>>
            > EVENT_BUS = new HashMap<>();
    public static final HashMap<Model, CommandExecutor> COMMANDS = new HashMap<>();

    public Model() {
        Main.LOGGER.info("正在加载 " + getName() + " 模块");
        try {
            register();
            MODELS.add(this);
        } catch (Exception e) {
            Util.handleException(e);
            Main.LOGGER.error("加载模块 " + getName() + "时出错: " + e);
            return;
        }
        Main.LOGGER.info("成功加载 " + getName() + " 模块");
    }

    public void register() {
        throw new UnsupportedOperationException("你不应该调用Model根类的方法");
        //
    }

    public void registerCommand(CommandExecutor exec, String... aliases) {
        CommandsList.register(this, aliases);
        COMMANDS.put(this, exec);
    }

    public <T extends Event> void listenEvent(Class<T> eventType, Consumer<T> listener) {
        if (EVENT_BUS.containsKey(eventType)) {
            EVENT_BUS.get(eventType).put(this, listener);
        } else {
            HashMap<Model, Consumer<? extends Event>> map = new HashMap<>();
            map.put(this, listener);
            EVENT_BUS.put(eventType, map);
        }
    }

    public static void callEvents(Event event) {
        for (Class<? extends Event> eventClass : EVENT_BUS.keySet()) {
            if (ClassUtil.isAssignable(event.getClass(), eventClass) || ClassUtil.isAssignable(eventClass, event.getClass())) {
                for (Consumer listener : EVENT_BUS.get(eventClass).values()) {
                    listener.accept(event);
                }
            }
        }
    }

    public static void callCommands(MessageEvent event, String msg) {
        String cmd = msg.split(" ")[0];
        String[] cmds = msg.substring(cmd.length()).trim().split(" ");
        if (cmds.length == 1 && cmds[0].trim().isEmpty()) {
            cmds = new String[0];
        }
        for (Model model : COMMANDS.keySet()) {
            if (CommandsList.isCmd(model, cmd.toLowerCase())) {
                COMMANDS.get(model).execute(Util.getRealSender(event), event.getSender(), cmd.substring(1), cmds);
                return;
            }
        }
    }

    public String getName() {
        return this.getClass().getSimpleName();
        //
    }
}
