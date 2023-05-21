package me.xpyex.plugin.allinone.model.core;

import me.xpyex.plugin.allinone.core.CoreModel;
import net.mamoe.mirai.contact.Contact;

public class PermManager extends CoreModel {
    @Override
    public void register() {
        registerCommand(Contact.class, (source, sender, label, args) -> {

        }, "permission", "permissions", "perm", "perms");
    }
}
