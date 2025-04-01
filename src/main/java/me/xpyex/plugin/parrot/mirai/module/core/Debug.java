package me.xpyex.plugin.parrot.mirai.module.core;

import me.xpyex.plugin.parrot.mirai.core.command.CommandNode;
import me.xpyex.plugin.parrot.mirai.core.module.CoreModule;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.utils.MsgUtil;
import me.xpyex.plugin.parrot.mirai.utils.Util;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.Nullable;

public class Debug extends CoreModule {
    private static boolean DEBUG = false;

    public void register() {
        registerCommand(Contact.class, CommandNode.of((source, sender, arguments) -> source.sendMessage("已切换Debug至 " + (DEBUG = !DEBUG) + " 模式")), "debug");
    }

    public static void handleException(Throwable e, boolean noticeOwner, @Nullable Event event, @Nullable Module module) {
        String message = "模块 " + (module == null ? "null" : module.getName()) + " 出现异常: " + e;
        if (!DEBUG) Module.getModule(Debug.class).info(message);
        else new RuntimeException(message, e).printStackTrace();

        if (DEBUG && noticeOwner) {
            String eventCause;
            if (event != null) {
                if (event instanceof MessageEvent) {
                    eventCause = (Util.isGroupEvent(event) ? "群聊-" : "私聊-") + ((MessageEvent) event).getSubject().getId();
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
