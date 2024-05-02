package me.xpyex.plugin.parrot.mirai.core.command;

import net.mamoe.mirai.contact.Contact;

public record Command<C extends Contact>(CommandExecutor<C> executor, String... aliases) {
}
