package me.xpyex.plugin.allinone.core;

import cn.hutool.core.util.ClassUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;
import me.xpyex.plugin.allinone.Main;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.events.MessageEvent;

/**
 * 这是所有模块的根类，所有模块应继承Model类以实现自动注册及所有管理
 * 你必须在子类覆写register()方法，你可以在这个方法内注册监听器及命令
 * 覆写getName()方法以自定义你的模块名字，否则以类名作为模块名
 * 调用listenEvent()方法以监听Mirai事件
 * 调用registerCommand()方法以注册AllInOne命令
 * 请勿覆写除了register()与getName()以外的方法/常量
 */
public abstract class Model {
    public static final HashSet<Model> LOADED_MODELS = new HashSet<>();
    public static final HashSet<Model> DISABLED_MODELS = new HashSet<>();
    private static final HashMap<
            Class<? extends Event>,
            HashMap<Model, Consumer<? extends Event>>
            > EVENT_BUS = new HashMap<>();
    private static final HashMap<Class<? extends Contact>, HashMap<Model, CommandExecutor<? extends Contact>>> COMMAND_BUS = new HashMap<>();

    public Model() {
        Main.LOGGER.info("正在加载 " + getName() + " 模块");
        try {
            register();
            LOADED_MODELS.add(this);
        } catch (Exception e) {
            e.printStackTrace();
            Main.LOGGER.error("加载模块 " + getName() + " 时出错: " + e);
            return;
        }
        Main.LOGGER.info("成功加载 " + getName() + " 模块");
        Main.LOGGER.info(" ");
    }

    public void register() {
        throw new UnsupportedOperationException("你不应该调用Model根类的方法");
        //
    }

    public <T extends Contact> void registerCommand(Class<T> contactType, CommandExecutor<T> exec, String... aliases) {
        CommandsList.register(this, aliases);
        if (COMMAND_BUS.containsKey(contactType)) {
            COMMAND_BUS.get(contactType).put(this, exec);
        } else {
            HashMap<Model, CommandExecutor<? extends Contact>> map = new HashMap<>();
            map.put(this, exec);
            COMMAND_BUS.put(contactType, map);
        }
        Main.LOGGER.info(getName() + " 模块注册命令: " + Arrays.toString(aliases) + ", 命令监听范围: " + contactType.getSimpleName());
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
        for (Class<? extends Contact> contactClass : COMMAND_BUS.keySet()) {
            if (ClassUtil.isAssignable(contactClass, Util.getRealSender(event).getClass())) {
                for (Model model : COMMAND_BUS.get(contactClass).keySet()) {
                    if (!DISABLED_MODELS.contains(model)) {
                        if (CommandsList.isCmd(model, cmd)) {
                            CommandExecutor executor = COMMAND_BUS.get(contactClass).get(model);
                            executor.execute(Util.getRealSender(event), event.getSender(), cmd.substring(1), cmds);
                        }
                    }
                }
            }
        }
    }

    public String getName() {
        return this.getClass().getSimpleName();
        //
    }

    public void disable() {
        DISABLED_MODELS.add(this);
        //
    }

    public void enable() {
        DISABLED_MODELS.remove(this);
        //
    }

    public static Model getModel(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        for (Model loadedModel : LOADED_MODELS) {
            if (loadedModel.getName().equalsIgnoreCase(name)) return loadedModel;
        }
        return null;
    }
}
