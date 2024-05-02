package me.xpyex.plugin.allinone.module;

import java.io.File;
import me.xpyex.plugin.allinone.core.module.Module;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

@SuppressWarnings("unused")
public class PokeAt extends Module {
    private static final File IMAGE_FILE = new File("pictures/轻轻敲醒沉睡的心灵.png");
    private static Image IMAGE;

    @Override
    public void register() {
        executeOnce(BotOnlineEvent.class, event ->
                                              IMAGE = getBot().getAsFriend().uploadImage(ExternalResource.create(IMAGE_FILE))
        );
        listenEvent(NudgeEvent.class, event -> {
            if (event.getTarget().getId() != event.getBot().getId()) {
                return;
            }
            MessageChain msg = new PlainText("检测到未知的外部撞击").plus("");
            msg.plus(IMAGE);
            event.getSubject().sendMessage(msg);
        });
    }
}
