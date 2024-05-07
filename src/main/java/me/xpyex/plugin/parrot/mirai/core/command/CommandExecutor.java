package me.xpyex.plugin.parrot.mirai.core.command;

import java.util.Optional;
import java.util.WeakHashMap;
import me.xpyex.plugin.parrot.mirai.core.mirai.ParrotContact;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;

@FunctionalInterface
public interface CommandExecutor<C extends Contact> {
    WeakHashMap<Long, MessageEvent> EVENT_POOL = new WeakHashMap<>();

    void execute(ParrotContact<C> source, ParrotContact<User> sender, String label, String... args) throws Throwable;

    /**
     * 获取触发命令的事件
     *
     * @param contact 需要传入命令的一个ContactTarget，获取其对应的MessageEvent
     * @return 仅当ContactTarget不由MessageEvent生成[如，命令由dispatchCommand()方法触发]时，Optional内部为null，否则均有值
     */
    default Optional<MessageEvent> getEvent(ParrotContact<C> contact) {
        System.out.println(EVENT_POOL);
        System.out.println(contact.getCreatedTime());
        return Optional.ofNullable(EVENT_POOL.get(contact.getCreatedTime()));
    }
}
