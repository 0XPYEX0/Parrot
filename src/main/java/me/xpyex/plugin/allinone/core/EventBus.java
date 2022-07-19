package me.xpyex.plugin.allinone.core;

import cn.hutool.core.util.ClassUtil;
import java.util.ArrayList;
import java.util.function.Consumer;
import me.xpyex.plugin.allinone.api.XPTuple;
import net.mamoe.mirai.event.Event;

public class EventBus {
    private static final ArrayList<XPTuple> EVENT_BUSES = new ArrayList<>();

    public <T extends Event> EventBus(Class<T> eventType, Model model, Consumer<T> eventExecutor) {
        EVENT_BUSES.add(new XPTuple(eventType, model, eventExecutor));
        //
    }

    public static <T extends Event> void callEvents(Event event) {
        for (XPTuple eventBus : EVENT_BUSES) {
            if (ClassUtil.isAssignable(eventBus.get(1), event.getClass())) {
                if (!Model.DISABLED_MODELS.contains(eventBus.get(2, Model.class))) {
                    Consumer listener = eventBus.get(3);
                    listener.accept(event);
                }
            }
        }
    }
}
