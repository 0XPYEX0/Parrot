package me.xpyex.plugin.allinone.core;

import net.mamoe.mirai.contact.Contact;

public record Command<C extends Contact>(CommandExecutor<C> executor, String... aliases) {
}
