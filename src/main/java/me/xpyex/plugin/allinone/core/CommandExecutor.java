package me.xpyex.plugin.allinone.core;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;

public interface CommandExecutor <T extends Contact> {
    public void execute(T source, User sender, String label, String[] args);
}
