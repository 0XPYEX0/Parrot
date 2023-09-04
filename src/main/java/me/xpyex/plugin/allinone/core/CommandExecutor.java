package me.xpyex.plugin.allinone.core;

import me.xpyex.plugin.allinone.core.mirai.ContactTarget;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;

@FunctionalInterface
public interface CommandExecutor<C extends Contact> {
    void execute(ContactTarget<C> source, ContactTarget<User> sender, String label, String[] args) throws Throwable;
}
