package me.xpyex.plugin.allinone.core;

import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.ClassUtil;
import java.util.ArrayList;
import java.util.function.Consumer;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.event.Event;

public class EventBus {
    private static final ArrayList<Tuple> EVENT_BUSES = new ArrayList<>();

    public static <E extends Event> void takeInBus(Class<E> eventType, Model model, Consumer<E> eventExecutor) {
        EVENT_BUSES.add(new Tuple(eventType, model, eventExecutor));
        //
    }

    public static void callEvents(Event event) {
        for (Tuple eventBus : EVENT_BUSES) {
            if (ClassUtil.isAssignable(eventBus.get(0), event.getClass())) {
                Model model = eventBus.get(1);
                if (!Model.DISABLED_MODELS.contains(model)) {
                    Consumer<Event> listener = eventBus.get(2);
                    try {
                        listener.accept(event);
                    } catch (Throwable e) {
                        Util.handleException(e, false);
                        Util.sendMsgToOwner("模块 " + model.getName() + " 在处理事件 " + event.getClass().getSimpleName() + " 时出现异常，已被捕获: " + e);
                    }
                }
            }
        }
    }
}
