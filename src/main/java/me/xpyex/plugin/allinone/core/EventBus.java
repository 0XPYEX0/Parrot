package me.xpyex.plugin.allinone.core;

import cn.hutool.core.util.ClassUtil;
import java.util.ArrayList;
import java.util.function.Consumer;
import net.mamoe.mirai.event.Event;

public class EventBus {
    private final Class<? extends Event> eventType;
    private final Model model;
    private final Consumer<? extends Event> eventExecutor;
    private static final ArrayList<EventBus> EVENT_BUSES = new ArrayList<>();

    public EventBus(Class<? extends Event> eventType, Model model, Consumer<? extends Event> eventExecutor) {
        this.eventType = eventType;
        this.model = model;
        this.eventExecutor = eventExecutor;
        EVENT_BUSES.add(this);
    }

    public static void callEvents(Event event) {
        for (EventBus eventBus : EVENT_BUSES) {
            if (ClassUtil.isAssignable(eventBus.eventType, event.getClass())) {
                if (!Model.DISABLED_MODELS.contains(eventBus.model)) {
                    Consumer listener = eventBus.eventExecutor;
                    listener.accept(event);
                }
            }
        }
    }
}
