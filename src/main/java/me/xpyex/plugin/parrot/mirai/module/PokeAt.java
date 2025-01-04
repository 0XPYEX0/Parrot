package me.xpyex.plugin.parrot.mirai.module;

import java.io.File;
import java.util.WeakHashMap;
import lombok.experimental.ExtensionMethod;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.module.core.PermManager;
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
    private static final WeakHashMap<Long, Long> LAST_NUDGE = new WeakHashMap<>();
    private static final int COOLDOWN = 60 * 8;
    private static final int GLOBAL_COOLDOWN = (int) (60 * 2.5);
    private static Image image;
    private static long LAST_NUDGE_GLOBAL = 0;

    @Override
    public void register() {
        executeOnce(BotOnlineEvent.class, event -> {
            try (ExternalResource image = ExternalResource.create(IMAGE_FILE)) {
                PokeAt.image = getBot().getAsFriend().uploadImage(image);
            }
        });
        listenEvent(NudgeEvent.class, event -> {
            if (event.getTarget().getId() != event.getBot().getId()) {
                return;
            }
            long now = System.currentTimeMillis();
            try (ExternalResource image = ExternalResource.create(IMAGE_FILE)) {
                PokeAt.image = event.getSubject().uploadImage(image);
            }

            long differenceGlobal = now - LAST_NUDGE_GLOBAL;
            if (differenceGlobal < GLOBAL_COOLDOWN * 1000 && !PermManager.hasPerm(event.getFrom().getId(), "Nudge.noCooldown"))
                return;  //全局冷却中

            long difference = now - LAST_NUDGE.getOrDefault(event.getFrom().getId(), now);
            if (difference == 0 || difference >= COOLDOWN * 1000 || PermManager.hasPerm(event.getFrom().getId(), "Nudge.noCooldown")) {  //个人冷却结束
                MessageChain msg = new PlainText("检测到未知的外部撞击").plus("");
                msg.plus(image);
                event.getSubject().sendMsg(msg);
                LAST_NUDGE.put(event.getFrom().getId(), now);
                LAST_NUDGE_GLOBAL = now;
            }
        });
    }
}
