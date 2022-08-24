package me.xpyex.plugin.allinone.core;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;

@FunctionalInterface
public interface CommandExecutor<C extends Contact> {
    public void execute(C source, User sender, String label, String[] args);
}
