package me.xpyex.plugin.allinone.model;

import java.io.File;
import me.xpyex.plugin.allinone.core.Model;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;

@SuppressWarnings("unused")
public class JoinMessage extends Model {
    private static final File IMAGE_NEW_MEMBER_FILE = new File("pictures/地位-1.jpg");
    public static final File IMAGE_HOW_TO_ASK_FILE = new File("pictures/提问の艺术.png");
    public static final File IMAGE_DONT_FLY_FILE = new File("pictures/一步登天.png");

    @Override
    public void register() {
        listenEvent(MemberJoinEvent.class, (event) -> {
            Image newMember = event.getGroup().uploadImage(ExternalResource.create(IMAGE_NEW_MEMBER_FILE).toAutoCloseable());
            event.getGroup().sendMessage(newMember);
            if (event.getGroupId() == 906768617) {
                Image howToAsk = event.getGroup().uploadImage(ExternalResource.create(IMAGE_HOW_TO_ASK_FILE).toAutoCloseable());
                Image doNotFly = event.getGroup().uploadImage(ExternalResource.create(IMAGE_DONT_FLY_FILE).toAutoCloseable());
                event.getGroup().sendMessage(new At(event.getMember().getId())
                        .plus(" 欢迎入群\n" +
                                "提问前请先阅读文档\n" +
                                "文档: https://skripthub.net/docs\n" +
                                "不看文档提问一律视为伸手党\n" +
                                "不看文档提问一律视为伸手党\n" +
                                "不看文档提问一律视为伸手党\n" +
                                "本群禁止派发广告！")
                        .plus(howToAsk)
                        .plus("先学会走再学飞")
                        .plus(doNotFly)
                );
            }
        });
    }
}
