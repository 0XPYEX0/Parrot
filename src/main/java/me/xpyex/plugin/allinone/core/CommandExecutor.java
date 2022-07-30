package me.xpyex.plugin.allinone.core;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;

public interface CommandExecutor<_Contact extends Contact> {
    public void execute(_Contact source, User sender, String label, String[] args);
}
