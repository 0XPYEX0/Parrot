package me.xpyex.plugin.allinone.core;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.io.File;
import me.xpyex.plugin.allinone.Main;
import me.xpyex.plugin.allinone.utils.FileUtil;
import net.mamoe.mirai.contact.User;

public class Permissions {
    private static final File USER_PERM_FILE = new File(Main.INSTANCE.getDataFolder(), "perms/users.json");
    private static final File GROUPS_FILE = new File(Main.INSTANCE.getDataFolder(), "perms/groups.json");
    public static final JSONObject USERS;
    public static final JSONObject GROUPS;
    static {
        if (!USER_PERM_FILE.exists()) {
            FileUtil.writeFile(USER_PERM_FILE, JSONUtil.toJsonPrettyStr(new JSONObject()), true);
        }
        if (!GROUPS_FILE.exists()) {
            FileUtil.writeFile(GROUPS_FILE, JSONUtil.toJsonPrettyStr(new JSONObject()), true);
        }
        USERS = new JSONObject(FileUtil.readFile(USER_PERM_FILE));
        GROUPS = new JSONObject(FileUtil.readFile(GROUPS_FILE));
    }

    public static boolean userHasPermission(User user, String perm) {
        if (!USERS.containsKey(user.getId() + "")) {
            return false;
        }
        JSONObject userPerm = USERS.getJSONObject(user.getId() + "");
        return userPerm.getJSONArray("permissions").contains(perm) || groupHasPermission(userPerm.getStr("group"), perm);
    }

    public static boolean groupHasPermission(String group, String perm) {
        if (!GROUPS.containsKey(group)) {
            return false;
        }
        if (GROUPS.getJSONObject(group).getJSONArray("permissions").contains(perm)) {
            return true;
        }
        for (Object parentGroup : GROUPS.getJSONObject(group).getJSONArray("depends")) {
            if (groupHasPermission(String.valueOf(parentGroup), perm)) {
                return true;
            }
        }
        return false;
    }
}
