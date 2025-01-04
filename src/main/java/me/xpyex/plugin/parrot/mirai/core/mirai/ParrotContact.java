package me.xpyex.plugin.parrot.mirai.core.mirai;

import cn.evolvefield.onebot.sdk.enums.ActionPathEnum;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import me.xpyex.plugin.parrot.mirai.module.core.PermManager;
import me.xpyex.plugin.parrot.mirai.utils.MsgUtil;
import me.xpyex.plugin.parrot.mirai.utils.Util;
import me.xpyex.plugin.parrot.mirai.utils.ValueUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.Message;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.overflow.contact.RemoteBot;

@Getter
@ExtensionMethod({MsgUtil.class, PermManager.class})
public class ParrotContact<C extends Contact> {
    private static final File FILE_CACHE_FOLDER = new File("cache/file");

    static {
        if (!FILE_CACHE_FOLDER.exists()) {
            FILE_CACHE_FOLDER.mkdirs();
        }
    }

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
        getContact().sendMsg(message);
        //安全发送
    }

    public void sendMessage(Message message) {
        getContact().sendMsg(message);
        //安全发送
    }

    public boolean hasPerm(String perm) {
        return hasPerm(perm, null);
        //
    }

    public boolean hasPerm(String perm, MemberPermission adminPass) {
        perm = perm.toLowerCase();
        return getContactAsUser().hasPerm(perm, adminPass);
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
            return (Group) getContact();
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
        if (getContact() instanceof Member) {
            return (Member) getContact();
        }
        throw new ClassCastException("其中的对象不是Member");
    }

    private <T> T debug(T obj) {
        System.out.println(obj);
        return obj;
    }

    public void uploadFile(File file, String name, @Nullable String folder) throws Exception {
        RemoteBot bot = RemoteBot.getAsRemoteBot(Util.getBot());
        ValueUtil.mustTrue("file必须存在，且非文件夹", file::exists, file::isFile);
        ValueUtil.notNull("参数name不应为null", name);  //无需判空file，上方可catch NullPointerException
        if (isGroup()) {
            if (folder != null && !folder.trim().isEmpty()) {
                JSONArray folders = JSONUtil.parseObj(bot.executeAction(ActionPathEnum.GET_GROUP_ROOT_FILES.getPath(),
                    new JSONObject()
                        .set("group_id", getId())
                        .toString()
                )).getJSONArray("folders");

                if (!folders.contains(folder)) {
                    bot.executeAction(debug(ActionPathEnum.CREATE_GROUP_FILE_FOLDER.getPath()),
                        debug(new JSONObject()
                                  .set("group_id", getId())
                                  .set("name", folder)
                                  .set("parent_id", "/")
                                  .toString())
                    );
                }
            }
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

    public void uploadFile(URL url, String name, @Nullable String folder) throws Exception {
        ValueUtil.notNull("参数除folder外，不应为null", url, name);
        File f = new File(FILE_CACHE_FOLDER, name);
        URLConnection connection = url.openConnection();
        connection.connect();
        Files.copy(connection.getInputStream(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
        uploadFile(f, name, folder);
        f.deleteOnExit();
    }

    public void uploadFile(String url, String name, @Nullable String folder) throws Exception {
        ValueUtil.notNull("参数除folder外，不应为null", url, name);
        uploadFile(new URL(url), name, folder);
    }
}
