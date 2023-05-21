package me.xpyex.plugin.allinone.core.mirai;

import java.util.WeakHashMap;
import me.xpyex.plugin.allinone.core.Permissions;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.Message;

public class ContactTarget<C extends Contact> {
    private final C contact;
    private final WeakHashMap<String, Boolean> PERM_CACHE = new WeakHashMap<>();

    public ContactTarget(C contact) {
        this.contact = contact;
        //
    }

    public C getContact() {
        return contact;
        //
    }

    public void sendMessage(String message) {
        getContact().sendMessage(message);
        //
    }

    public void sendMessage(Message message) {
        getContact().sendMessage(message);
        //
    }

    public boolean hasPerm(String perm) {
        if (!(getContact() instanceof User)) {
            throw new IllegalStateException("群对象并不会记录权限");
        }
        if (!PERM_CACHE.containsKey(perm)) {
            PERM_CACHE.put(perm, Permissions.userHasPermission((User) getContact(), perm));
        }
        return PERM_CACHE.get(perm);
    }

    public long getId() {
        return getContact().getId();
        //
    }

    public boolean isGroup() {
        return getContact() instanceof Group;
        //
    }
}
