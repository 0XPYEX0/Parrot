package me.xpyex.plugin.allinone.core.mirai;

import java.util.WeakHashMap;
import lombok.Getter;
import me.xpyex.plugin.allinone.module.core.PermManager;
import me.xpyex.plugin.allinone.utils.MsgUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.Message;

public class ContactTarget<C extends Contact> {
    //
    @Getter
    private final C contact;
    private final WeakHashMap<String, Boolean> PERM_CACHE = new WeakHashMap<>();
    //
    @Getter
    private final long createdTime = System.currentTimeMillis();

    public ContactTarget(C contact) {
        this.contact = contact;
        //
    }

    public void sendMessage(String message) {
        MsgUtil.sendMsg(getContact(), message);
        //安全发送
    }

    public void sendMessage(Message message) {
        MsgUtil.sendMsg(getContact(), message);
        //安全发送
    }

    public boolean hasPerm(String perm) {
        return hasPerm(perm, null);
        //
    }

    public boolean hasPerm(String perm, MemberPermission adminPass) {
        perm = perm.toLowerCase();
        if (!(getContact() instanceof User)) {
            throw new IllegalStateException("群对象并不会记录权限");
        }
        if (!PERM_CACHE.containsKey(perm)) {
            PERM_CACHE.put(perm, PermManager.hasPerm((User) getContact(), perm, adminPass));
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
