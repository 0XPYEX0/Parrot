package me.xpyex.plugin.parrot.mirai.utils;

import me.xpyex.plugin.parrot.mirai.core.module.Module;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.Nullable;

public class ExceptionUtil {
    public static void handleException(Throwable e, boolean noticeOwner, @Nullable Event event, @Nullable Module module) {
        e.printStackTrace();
        if (noticeOwner) {
            String eventCause;
            if (event != null) {
                if (event instanceof MessageEvent) {
                    eventCause = (Util.isGroupEvent(event) ? "群聊-" : "私聊-") + MsgUtil.getRealSender((MessageEvent) event).getId();
                } else {
                    eventCause = "事件-" + event.getClass().getSimpleName() + "\n详细信息: " + event;
                }
            } else {
                eventCause = "未知事件";
            }
            MsgUtil.sendMsgToOwner("模块 " + (module == null ? "null" : module.getName()) +
                                       " 在执行 " + e.getStackTrace()[0].getClassName() + " 类的方法 " +
                                       e.getStackTrace()[0].getMethodName() + " 时出错: " +
                                       e + "\n" +
                                       "该代码位于该类的第 " + e.getStackTrace()[0].getLineNumber() + " 行" +
                                       "\n" + "该错误由 " + eventCause + " 触发");
        }
    }
}
