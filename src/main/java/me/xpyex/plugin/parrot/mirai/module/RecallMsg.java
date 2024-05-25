package me.xpyex.plugin.parrot.mirai.module;

import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.module.core.PermManager;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.PermissionDeniedException;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageContent;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.QuoteReply;

public class RecallMsg extends Module {
    @Override
    public void register() throws Throwable {
        listenEvent(MessageEvent.class, event -> {
            QuoteReply quote = event.getMessage().get(QuoteReply.Key);
            if (quote != null) {
                MessageContent plain = event.getMessage().get(PlainText.Key);
                if (plain != null && "#recall".equalsIgnoreCase(plain.contentToString().trim())) {
                    if (!PermManager.hasPerm(event.getSender(), "RecallMsg.use", MemberPermission.ADMINISTRATOR)){
                        autoSendMsg(event, "你没有权限");
                        return;
                    }
                    try {
                        Mirai.getInstance().recallMessage(event.getBot(), quote.getSource());
                        autoSendMsg(event, "已撤回");
                    } catch (PermissionDeniedException ignored) {
                        autoSendMsg(event, "无法撤回: 权限不足");
                    } catch (IllegalStateException ignored) {
                        autoSendMsg(event, "原消息已撤回");
                    }
                }
            }
        });
    }
}
