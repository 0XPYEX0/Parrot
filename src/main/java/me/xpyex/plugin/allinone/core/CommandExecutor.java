package me.xpyex.plugin.allinone.core;

import net.mamoe.mirai.contact.Contact;

public interface CommandExecutor <T extends Contact> {
    public void execute(T source, Contact sender, String label, String[] args);
}
