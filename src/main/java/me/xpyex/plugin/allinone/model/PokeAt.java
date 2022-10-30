package me.xpyex.plugin.allinone.model;

import java.io.File;
import me.xpyex.plugin.allinone.core.Model;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;

@SuppressWarnings("unused")
public class PokeAt extends Model {
    private static final File IMAGE_FILE = new File("pictures/轻轻敲醒沉睡的心灵.png");
    private static Image IMAGE;

    @Override
    public void register() {
        runTaskLater(() ->
                         IMAGE = Contact.uploadImage(Util.getOwner(), IMAGE_FILE),
            5);
        listenEvent(NudgeEvent.class, (event) -> {
            if (event.getTarget().getId() != event.getBot().getId()) {
                return;
            }
            MessageChain msg = new PlainText("检测到未知的外部撞击").plus("");
            msg.plus(IMAGE);
            event.component3().sendMessage(msg);
        });
    }
}
