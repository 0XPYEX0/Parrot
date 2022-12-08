package me.xpyex.plugin.allinone.core;

import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.ClassUtil;
import java.util.ArrayList;
import java.util.function.Consumer;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.event.AbstractEvent;
import net.mamoe.mirai.event.Event;

public class EventBus {
    private static final ArrayList<Tuple> EVENT_BUSES = new ArrayList<>();

    public static <E extends Event> void takeInBus(Class<E> eventType, Model model, Consumer<E> eventExecutor) {
        EVENT_BUSES.add(new Tuple(eventType, model, eventExecutor));
        //
    }

    public static boolean callToCoreModel(Event event) {  //先将事件发送到CoreModel审核，看是否需要取消
        callEvents(event,CoreModel.class);
        for (Model model : Model.LOADED_MODELS.values()) {
            if (model.isCore()) {
                if (!((CoreModel) model).interceptEvent(event)) return false;  //如果返回 false， 说明事件被拦截，则此处返回false
            }
        }
        return true;
    }

    /**
     * 对模块广播事件
     * @param event 事件
     * @param modelType 可接收到该事件的模块类型
     * @param <M> 模块
     */
    public static <M extends Model> void callEvents(Event event, Class<M> modelType) {
        for (Tuple eventBus : EVENT_BUSES) {
            if (ClassUtil.isAssignable(eventBus.get(0), event.getClass())) {
                Model model = eventBus.get(1);
                if (modelType.isInstance(model) && model.isEnabled()) {
                    Consumer<Event> listener = eventBus.get(2);
                    try {
                        listener.accept(event);
                    } catch (Throwable e) {
                        Util.handleException(e, false);
                        StringBuilder eventName = new StringBuilder();
                        Class<?> coreClass = event.getClass();
                        while (!coreClass.isInterface()) {
                            eventName.insert(0, "." + coreClass.getSimpleName());
                            coreClass = coreClass.getSuperclass();
                            if (coreClass == null || (!ClassUtil.isAssignable(AbstractEvent.class, coreClass)) || AbstractEvent.class.equals(coreClass)) {
                                if (eventName.toString().length() != 0) {
                                    eventName.delete(0, 1);
                                }
                                break;
                            }
                        }
                        Util.sendMsgToOwner("模块 " + model.getName() + " 在处理事件 " + eventName + " 时出现异常，已被捕获: " + e);
                    }
                }
            }
        }
    }

    public static String[] getEvents(Model model) {
        ArrayList<String> list = new ArrayList<>();
        for (Tuple bus : EVENT_BUSES) {
            if (bus.get(1) == model) {
                list.add(((Class<?>) bus.get(0)).getSimpleName());
            }
        }
        return list.toArray(new String[0]);
    }
}
