package me.xpyex.plugin.allinone.core;

import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.ClassUtil;
import java.util.ArrayList;
import java.util.function.Consumer;
import net.mamoe.mirai.event.Event;

public class EventBus {
    private static final ArrayList<Tuple> EVENT_BUSES = new ArrayList<>();

    public <_Event extends Event> EventBus(Class<_Event> eventType, Model model, Consumer<_Event> eventExecutor) {
        EVENT_BUSES.add(new Tuple(eventType, model, eventExecutor));
        //
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void callEvents(Event event) {
        for (Tuple eventBus : EVENT_BUSES) {
            if (ClassUtil.isAssignable(eventBus.get(0), event.getClass())) {
                Model model = eventBus.get(1);
                if (!Model.DISABLED_MODELS.contains(model)) {
                    Consumer listener = eventBus.get(2);
                    listener.accept(event);
                }
            }
        }
    }
}
