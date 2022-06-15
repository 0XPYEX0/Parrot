package me.xpyex.plugin.allinone.core;

import cn.hutool.core.util.ClassUtil;
import java.util.HashMap;
import java.util.HashSet;
import me.xpyex.plugin.allinone.Main;
import net.mamoe.mirai.event.Event;

public class Model {
    public static final HashSet<Model> MODELS = new HashSet<>();
    private static final HashMap<
            Class<? extends Event>,
            HashMap<Model, ModelsEventListener>
            > EVENT_BUS = new HashMap<>();

    public Model() {
        MODELS.add(this);
        register();
        Main.LOGGER.info("正在加载 " + getName() + " 模块");
    }

    public void register() {
        throw new UnsupportedOperationException("你不应该调用Model根类的方法");
    }

    public void registerCommand(String cmd) {
        throw new UnsupportedOperationException("你不应该调用Model根类的方法");
    }

    public void listenEvent(Class<? extends Event> eventType, ModelsEventListener listener) {
        if (EVENT_BUS.containsKey(eventType)) {
            EVENT_BUS.get(eventType).put(this, listener);
        } else {
            HashMap<Model, ModelsEventListener> map = new HashMap<>();
            map.put(this, listener);
            EVENT_BUS.put(eventType, map);
        }
    }

    public static void callEvents(Event event) {
        for (Class<? extends Event> eventClass : EVENT_BUS.keySet()) {
            if (ClassUtil.isAssignable(event.getClass(), eventClass) || ClassUtil.isAssignable(eventClass, event.getClass())) {
                for (ModelsEventListener listener : EVENT_BUS.get(eventClass).values()) {
                    listener.onEvent(event);
                }
            }
        }
    }

    public String getName() {
        throw new IllegalStateException("你的模块没有注册名字!");
    }
}
