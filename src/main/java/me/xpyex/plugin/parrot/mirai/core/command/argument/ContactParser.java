package me.xpyex.plugin.parrot.mirai.core.command.argument;

import net.mamoe.mirai.contact.Contact;

public abstract class ContactParser extends ArgParser {
    public long getParsedId(String arg) {
        return parse(arg, Contact.class).map(Contact::getId).orElse(0L);
    }
}
