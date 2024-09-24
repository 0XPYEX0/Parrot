package me.xpyex.plugin.parrot.mirai.module;

import java.io.File;
import java.util.Random;
import lombok.experimental.ExtensionMethod;
import me.xpyex.plugin.parrot.mirai.api.MessageBuilder;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.utils.MsgUtil;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;

@SuppressWarnings("unused")
@ExtensionMethod(MsgUtil.class)
public class JoinMessage extends Module {
    private static final File IMAGE_HOW_TO_ASK_FILE = new File("pictures/提问の艺术.png");
    private static final File IMAGE_DONT_FLY_FILE = new File("pictures/一步登天.png");
    private static final File IMAGE_NEW_MEMBER_FILE = new File("pictures/地位-1.jpg");
    private static final Random RANDOM = new Random();

    @Override
    public void register() {
        listenEvent(MemberJoinEvent.class, event -> {
            try (ExternalResource newMemberImage = ExternalResource.create(IMAGE_NEW_MEMBER_FILE)) {
                Image newMember = event.getGroup().uploadImage(newMemberImage);
                event.getGroup().sendMsg(newMember);
            }
            if (event.getGroupId() == 906768617) {
                runTaskLater(() -> {
                    event.getGroup().sendMsg(
                        new At(event.getMember().getId())
                            .plus(new MessageBuilder()
                                      .plus(" 欢迎入群")
                                      .plus("提问前请先阅读文档")
                                      .plus("文档: https://skripthub.net/docs")
                                      .plus("不看文档提问一律视为伸手党")
                                      .plus("不看文档提问一律视为伸手党")
                                      .plus("不看文档提问一律视为伸手党")
                                      .plus("本群禁止派发广告！")
                                      .toMessage())
                            .plus("先学会走再学飞")
                    );
                    try (ExternalResource howToAskImage = ExternalResource.create(IMAGE_HOW_TO_ASK_FILE)) {
                        Image howToAsk = event.getGroup().uploadImage(howToAskImage);
                        event.getGroup().sendMsg(howToAsk);
                    }
                    try (ExternalResource dontFlyImage = ExternalResource.create(IMAGE_DONT_FLY_FILE)) {
                        Image doNotFly = event.getGroup().uploadImage(dontFlyImage);
                        event.getGroup().sendMsg(doNotFly);
                    }
                }, RANDOM.nextInt(15));
            }
        });
    }
}
