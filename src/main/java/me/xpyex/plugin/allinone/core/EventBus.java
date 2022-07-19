package me.xpyex.plugin.allinone.core;

import cn.hutool.core.util.ClassUtil;
import java.util.ArrayList;
import java.util.function.Consumer;
import me.xpyex.plugin.allinone.api.TripleVar;
import net.mamoe.mirai.event.Event;

public class EventBus {
    private static final ArrayList<TripleVar> EVENT_BUSES = new ArrayList<>();

    public <T extends Event> EventBus(Class<T> eventType, Model model, Consumer<T> eventExecutor) {
        EVENT_BUSES.add(new TripleVar<>(eventType, model, eventExecutor));
        //
    }

    public static <T extends Event> void callEvents(Event event) {
        for (TripleVar<Class<T>, Model, Consumer<T>> eventBus : EVENT_BUSES) {
            if (ClassUtil.isAssignable(eventBus.getVar1(), event.getClass())) {
                if (!Model.DISABLED_MODELS.contains(eventBus.getVar2())) {
                    Consumer listener = eventBus.getVar3();
                    listener.accept(event);
                }
            }
        }
    }
}
