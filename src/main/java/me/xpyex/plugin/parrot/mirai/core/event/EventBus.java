package me.xpyex.plugin.parrot.mirai.core.event;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.ClassUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import me.xpyex.plugin.parrot.mirai.api.TryConsumer;
import me.xpyex.plugin.parrot.mirai.core.module.CoreModule;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.utils.ExceptionUtil;
import me.xpyex.plugin.parrot.mirai.utils.MsgUtil;
import net.mamoe.mirai.event.AbstractEvent;
import net.mamoe.mirai.event.Event;

public class EventBus {
    private static final ArrayList<Tuple> LISTENERS = new ArrayList<>();
    private static final ArrayList<Pair<Class<Event>, TryConsumer<Event>>> ONCE_EVENTS = new ArrayList<>();

    public static <E extends Event> void takeInBus(Class<E> eventType, Module module, TryConsumer<E> eventExecutor) {
        LISTENERS.add(new Tuple(eventType, module, eventExecutor));
        //
    }

    public static boolean callToCoreModule(Event event) {  //先将事件发送到CoreModule审核，看是否需要取消
        callEvents(event, CoreModule.class);  //正常的listenEvent()
        for (Module module : Module.LOADED_MODELS.values()) {
            if (module.isCore()) {
                if (!((CoreModule) module).acceptEvent(event)) return false;  //如果返回 false， 说明事件被拦截，则此处返回false
            }
        }
        return true;
    }

    /**
     * 对模块广播事件
     *
     * @param event      事件
     * @param moduleType 可接收到该事件的模块类型
     * @param <M>        模块
     */
    public static <M extends Module> void callEvents(Event event, Class<M> moduleType) {
        if (!ONCE_EVENTS.isEmpty()) {
            Iterator<Pair<Class<Event>, TryConsumer<Event>>> iterator = ONCE_EVENTS.iterator();
            while (iterator.hasNext()) {
                Pair<Class<Event>, TryConsumer<Event>> pair = iterator.next();
                if (pair.getKey().isInstance(event)) {
                    try {
                        pair.getValue().accept(event);
                    } catch (Throwable e) {
                        ExceptionUtil.handleException(e, false);
                        MsgUtil.sendMsgToOwner("在处理单次事件 " + event.getClass().getSimpleName() + " 时出现异常，已被捕获: " + e);
                    }
                    iterator.remove();
                }
            }
        }
        for (Tuple eventBus : LISTENERS) {
            if (ClassUtil.isAssignable(eventBus.get(0), event.getClass())) {
                Module module = eventBus.get(1);
                if (moduleType.isInstance(module) && module.isEnabled()) {
                    TryConsumer<Event> listener = eventBus.get(2);
                    try {
                        listener.accept(event);
                    } catch (Throwable e) {
                        ExceptionUtil.handleException(e, false);
                        StringBuilder eventName = new StringBuilder();
                        Class<?> coreClass = event.getClass();
                        while (!coreClass.isInterface() && coreClass != Object.class) {
                            eventName.insert(0, "." + coreClass.getSimpleName());
                            coreClass = coreClass.getSuperclass();
                            if (coreClass == null || (!ClassUtil.isAssignable(AbstractEvent.class, coreClass)) || AbstractEvent.class.equals(coreClass)) {
                                if (!eventName.toString().isEmpty()) {
                                    eventName.delete(0, 1);
                                }
                                break;
                            }
                        }
                        MsgUtil.sendMsgToOwner("模块 " + module.getName() + " 在处理事件 " + eventName + " 时出现异常，已被捕获: " + e);
                    }
                }
            }
        }
    }

    public static List<String> getEvents(Module module) {
        ArrayList<String> list = new ArrayList<>();
        for (Tuple bus : LISTENERS) {
            if (bus.get(1) == module) {
                list.add(((Class<?>) bus.get(0)).getSimpleName());
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public static <E extends Event> void executeOnce(Class<E> eventType, TryConsumer<E> executor) {
        ONCE_EVENTS.add(new Pair<>((Class<Event>) eventType, (TryConsumer<Event>) executor));
        //
    }
}
