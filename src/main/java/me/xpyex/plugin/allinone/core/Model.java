package me.xpyex.plugin.allinone.core;

import cn.hutool.core.util.ClassUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;
import me.xpyex.plugin.allinone.Main;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.events.MessageEvent;

public abstract class Model {
    public static final HashSet<Model> LOADED_MODELS = new HashSet<>();
    public static final HashSet<Model> DISABLED_MODELS = new HashSet<>();
    private static final HashMap<
            Class<? extends Event>,
            HashMap<Model, Consumer<? extends Event>>
            > EVENT_BUS = new HashMap<>();
    public static final HashMap<Model, CommandExecutor> COMMANDS = new HashMap<>();

    public Model() {
        Main.LOGGER.info("正在加载 " + getName() + " 模块");
        try {
            register();
            LOADED_MODELS.add(this);
        } catch (Exception e) {
            e.printStackTrace();
            Main.LOGGER.error("加载模块 " + getName() + "时出错: " + e);
            return;
        }
        Main.LOGGER.info("成功加载 " + getName() + " 模块");
        Main.LOGGER.info(" ");
    }

    public void register() {
        throw new UnsupportedOperationException("你不应该调用Model根类的方法");
        //
    }

    public void registerCommand(CommandExecutor exec, String... aliases) {
        CommandsList.register(this, aliases);
        COMMANDS.put(this, exec);
        Main.LOGGER.info(getName() + "模块注册命令: " + Arrays.toString(aliases));
    }

    public <T extends Event> void listenEvent(Class<T> eventType, Consumer<T> listener) {
        if (EVENT_BUS.containsKey(eventType)) {
            EVENT_BUS.get(eventType).put(this, listener);
        } else {
            HashMap<Model, Consumer<? extends Event>> map = new HashMap<>();
            map.put(this, listener);
            EVENT_BUS.put(eventType, map);
        }
        Main.LOGGER.info(getName() + " 模块注册监听事件: " + eventType.getSimpleName());
    }

    public static void callEvents(Event event) {
        for (Class<? extends Event> eventClass : EVENT_BUS.keySet()) {
            if (ClassUtil.isAssignable(eventClass, event.getClass())) {
                for (Model model : EVENT_BUS.get(eventClass).keySet()) {
                    if (!DISABLED_MODELS.contains(model)) {
                        Consumer listener = EVENT_BUS.get(eventClass).get(model);
                        listener.accept(event);
                    }
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
            if (DISABLED_MODELS.contains(model)) {
                continue;
            }
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

    public void disable() {
        DISABLED_MODELS.add(this);
    }

    public void enable() {
        DISABLED_MODELS.remove(this);
    }

    public static Model getModel(String name) {
        if (name == null || name.isEmpty()) return null;
        for (Model loadedModel : LOADED_MODELS) {
            if (loadedModel.getName().equalsIgnoreCase(name)) return loadedModel;
        }
        return null;
    }
}
