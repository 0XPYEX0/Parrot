package me.xpyex.plugin.allinone.core;

import net.mamoe.mirai.contact.Contact;

public interface CommandExecutor {
    public void execute(Contact source, Contact sender, String label, String[] args);
}
