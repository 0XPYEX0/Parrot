package me.xpyex.plugin.parrot.mirai.module;

import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.utils.StringUtil;
import net.mamoe.mirai.event.events.MessageEvent;

public class AndIAskYou extends Module {
    @Override
    public void register() throws Throwable {
        this.DEFAULT_DISABLED = true;
        listenEvent(MessageEvent.class, event -> {
            if (StringUtil.containsIgnoreCaseOr(event.getMessage().contentToString(), "男的女的", "你是男的还是女的", "怎么尖尖的")) {
                event.getSubject().sendMessage("那我问你\uD83E\uDD13\uD83D\uDC46");
            } else if (event.getMessage().contentToString().equals("那我问你")) {
                event.getSubject().sendMessage("那你问吧\uD83E\uDD13\uD83D\uDC46");
            }
        });
    }
}
