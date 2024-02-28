package me.xpyex.plugin.allinone.core.command;

import java.util.WeakHashMap;
import me.xpyex.plugin.allinone.core.mirai.ContactTarget;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;

@FunctionalInterface
public interface CommandExecutor<C extends Contact> {
    WeakHashMap<Long, MessageEvent> EVENT_POOL = new WeakHashMap<>();

    void execute(ContactTarget<C> source, ContactTarget<User> sender, String label, String[] args) throws Throwable;

    default MessageEvent getEvent(ContactTarget<C> contact) {
        System.out.println(EVENT_POOL);
        System.out.println(contact.getCreatedTime());
        return EVENT_POOL.get(contact.getCreatedTime());
    }
}
