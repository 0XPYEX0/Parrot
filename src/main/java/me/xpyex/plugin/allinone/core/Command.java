package me.xpyex.plugin.allinone.core;

import net.mamoe.mirai.contact.Contact;

public class Command<C extends Contact> {
    private final CommandExecutor<C> executor;
    private final String[] aliases;

    public Command(CommandExecutor<C> executor, String... aliases) {
        this.executor = executor;
        this.aliases = aliases;
    }

    public CommandExecutor<C> getExecutor() {
        return executor;
        //
    }

    public String[] getAliases() {
        return aliases;
        //
    }
}
