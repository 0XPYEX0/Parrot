package me.xpyex.plugin.parrot.mirai.core.mirai;

import cn.evole.onebot.sdk.enums.ActionPathEnum;
import cn.hutool.json.JSONObject;
import java.io.File;
import lombok.Getter;
import lombok.SneakyThrows;
import me.xpyex.plugin.parrot.mirai.module.core.PermManager;
import me.xpyex.plugin.parrot.mirai.utils.MsgUtil;
import me.xpyex.plugin.parrot.mirai.utils.Util;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.Message;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.overflow.contact.RemoteBot;

@Getter
public class ParrotContact<C extends Contact> {
    private final C contact;
    private final long createdTime = System.currentTimeMillis();

    private ParrotContact(C contact) {
        this.contact = contact;
        //
    }

    @NotNull
    @Contract("_ -> new")
    public static <C extends Contact> ParrotContact<C> of(C contact) {
        return new ParrotContact<>(contact);
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
        return PermManager.hasPerm(getContactAsUser(), perm, adminPass);
    }

    public long getId() {
        return getContact().getId();
        //
    }

    public boolean isGroup() {
        return getContact() instanceof Group;
        //
    }

    public Group getContactAsGroup() {
        if (isGroup()) {
            return (Group) this.getContact();
        }
        throw new ClassCastException("其中的对象不是Group");
    }

    public User getContactAsUser() {
        if (getContact() instanceof User) {
            return (User) getContact();
        }
        throw new ClassCastException("其中的对象不是User");
    }

    public Member getContactAsMember() {
        //
        if (getContact() instanceof Member) {
            return (Member) getContact();
        }
        throw new ClassCastException("其中的对象不是Member");
    }

    private <T> T debug(T obj) {
        System.out.println(obj);
        return obj;
    }

    @SneakyThrows
    public void uploadFile(File file, String name, String folder) {
        RemoteBot bot = RemoteBot.getAsRemoteBot(Util.getBot());
        if (isGroup()) {
            bot.executeAction(debug(ActionPathEnum.UPLOAD_GROUP_FILE.getPath()),
                debug(new JSONObject()
                    .set("group_id", getId())
                    .set("file", file.getAbsolutePath())
                    .set("name", name)
                    .set("folder", folder)
                    .toString())
                );
        } else {
            bot.executeAction(ActionPathEnum.UPLOAD_PRIVATE_FILE.getPath(),
                new JSONObject()
                    .set("user_id", getId())
                    .set("file", file.getAbsolutePath())
                    .set("name", name)
                    .toString()
                );
        }
    }
}
