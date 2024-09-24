package me.xpyex.plugin.parrot.mirai.module;

import java.io.File;
import lombok.experimental.ExtensionMethod;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.utils.MsgUtil;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

@SuppressWarnings("unused")
@ExtensionMethod(MsgUtil.class)
public class PokeAt extends Module {
    private static final File IMAGE_FILE = new File("pictures/轻轻敲醒沉睡的心灵.png");
    private static Image IMAGE;

    @Override
    public void register() {
        executeOnce(BotOnlineEvent.class, event ->
                                              IMAGE = getBot().getAsFriend().uploadImage(ExternalResource.create(IMAGE_FILE).toAutoCloseable())
        );
        listenEvent(NudgeEvent.class, event -> {
            if (event.getTarget().getId() != event.getBot().getId()) {
                return;
            }
            MessageChain msg = new PlainText("检测到未知的外部撞击").plus("");
            msg.plus(IMAGE);
            event.getSubject().sendMsg(msg);
        });
    }
}
