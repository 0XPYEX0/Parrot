package me.xpyex.plugin.allinone.functions.informs;

import java.io.File;
import me.xpyex.plugin.allinone.Main;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;

public class PokeAt {
    public static void load() {
        GlobalEventChannel.INSTANCE.registerListenerHost(new SimpleListenerHost(Main.INSTANCE.getCoroutineContext()) {
            @EventHandler
            public void onPokeAt(NudgeEvent event) {
                if (event.getTarget().getId() != Util.getBot().getId()) {
                    return;
                }
                MessageChain msg = new PlainText("检测到未知的外部撞击").plus("");
                Image image = Contact.uploadImage(event.component3(), new File("pictures/轻轻唤醒沉睡的心灵.png"));
                msg.plus(image);
                event.component3().sendMessage(msg);
            }
        });
        Main.LOGGER.info("PokeAt模块已加载");
    }
}
