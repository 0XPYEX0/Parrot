package me.xpyex.plugin.allinone.model.core;

import cn.hutool.json.JSONUtil;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.WeakHashMap;
import me.xpyex.plugin.allinone.api.CommandMenu;
import me.xpyex.plugin.allinone.core.CoreModel;
import me.xpyex.plugin.allinone.core.permission.GroupPerm;
import me.xpyex.plugin.allinone.core.permission.Perms;
import me.xpyex.plugin.allinone.core.permission.UserPerm;
import me.xpyex.plugin.allinone.utils.FileUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;

public class PermManager extends CoreModel {
    private static File GROUPS_FOLDER;
    private static File USERS_FOLDER;
    public static final HashMap<String, GroupPerm> GROUPS = new HashMap<>();
    private static final WeakHashMap<Long, UserPerm> USERS = new WeakHashMap<>();

    public void reload() {
        GROUPS_FOLDER = new File(getDataFolder(), "Groups");
        USERS_FOLDER = new File(getDataFolder(), "Users");
        GROUPS_FOLDER.mkdirs();
        USERS_FOLDER.mkdirs();

        GROUPS.clear();
        USERS.clear();

        for (File file : GROUPS_FOLDER.listFiles()) {
            if (!file.getName().endsWith(".json")) {
                continue;
            }
            try {
                String groupName = file.getName().substring(0, file.getName().lastIndexOf(".json"));
                GROUPS.put(groupName, JSONUtil.toBean(FileUtil.readFile(file), GroupPerm.class));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void register() {
        reload();

        registerCommand(Contact.class, (source, sender, label, args) -> {
            if (!sender.hasPerm(getName() + ".admin", false)) {
                source.sendMessage("你没有权限");
                return;
            }
            if (args.length == 0) {
                new CommandMenu(label)
                    .add("newGroup <Name> <isDefault>", "创建新的权限组")
                    .add("reload", "尝试重载所有权限内容")
                    .add("set <Group|User> <Name|ID> <Perm> <State>", "给组|用户修改权限状态")
                    .add("setAll <UserID> <true/false>", "给予用户所有权限")
                    .send(source);
                return;
            }
            if (args[0].equalsIgnoreCase("set")) {
                if (args.length < 5) {
                    source.sendMessage("参数不足");
                    return;
                }
                String type;
                if (args[1].equalsIgnoreCase("group")) {
                    type = "组";
                } else if (args[1].equalsIgnoreCase("user")) {
                    type = "用户";
                } else {
                    source.sendMessage("参数错误: " + args[1]);
                    return;
                }
                String id = args[2];
                String perm = args[3].toLowerCase();
                int state = Integer.parseInt(args[4]);
                Perms permInstance = type.equals("组") ? GROUPS.get(id) : getUserPerm(Long.parseLong(id));
                if (permInstance == null) {
                    source.sendMessage("错误: <" + type + " " + id + "> 不存在");
                    return;
                }
                switch (state) {
                    case -1:
                        permInstance.getPermissions().remove(perm);
                        permInstance.getDenyPerms().add(perm);
                    case 0:
                        permInstance.getDenyPerms().remove(perm);
                        permInstance.getPermissions().remove(perm);
                    case 1:
                        permInstance.getPermissions().add(perm);
                        permInstance.getDenyPerms().remove(perm);
                    default:
                        permInstance.save();
                        source.sendMessage("设置 <" + type + " " + id + "> 的权限 <" + perm + "> 状态为 <" + state + ">");
                }
            } else if (args[0].equalsIgnoreCase("setAll")) {
                if (sender.hasPerm(getName() + ".setOp", false)) {
                    source.sendMessage("你没有权限");
                    return;
                }
                if (args.length < 3) {
                    source.sendMessage("参数不足");
                    return;
                }
                long id = Long.parseLong(args[1]);
                boolean newState = Boolean.parseBoolean(args[2]);
                getUserPerm(id).setHasAllPerms(newState).save();
                source.sendMessage("已赋予 " + id + " 所有权限");
            } else if (args[0].equalsIgnoreCase("reload")) {
                reload();
                source.sendMessage("尝试重载");
            } else if (args[0].equalsIgnoreCase("newGroup")) {
                if (args.length < 3) {
                    source.sendMessage("参数不足");
                    return;
                }
                boolean isDefault = Boolean.parseBoolean(args[2]);
                File f = new File(GROUPS_FOLDER, args[1] + ".json");
                if (f.exists()) {
                    source.sendMessage("已存在同名权限组: " + args[1]);
                    return;
                }
                FileUtil.writeFile(f, JSONUtil.toJsonPrettyStr(new GroupPerm(args[1]).setDefaultGroup(isDefault)));
                reload();
                source.sendMessage("成功创建组: " + args[1]);
            }
        }, "permission", "permissions", "perm", "perms");

        registerCommand(Contact.class, (source, sender, label, args) -> {
            if (!info(sender.hasPerm(info(getName() + ".setOp"), false))) {
                source.sendMessage("你没有权限");
                return;
            }
            if (args.length == 0) {
                new CommandMenu(label)
                    .add("set <ID>", "给予用户管理员权限")
                    .add("unset <ID>", "剥夺用户管理员权限")
                    .send(source);
                return;
            }
            boolean argIsSet = args[0].equalsIgnoreCase("set");
            if (argIsSet || args[0].equalsIgnoreCase("unset")) {
                try {
                    long id = Long.parseLong(args[1]);
                    getUserPerm(id).setHasAllPerms(argIsSet).save();
                    source.sendMessage("将 " + id + " 的管理员权限设为 " + argIsSet);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException ignored) {
                    source.sendMessage("无效的对象");
                }
            } else {
                source.sendMessage("未知的命令参数");
            }
        }, "op");
    }

    public static boolean hasPerm(User user, String perm, boolean adminPass) {
        if (adminPass && user instanceof NormalMember && ((NormalMember) user).getPermission() != MemberPermission.MEMBER) {
            return true;
        }
        try {
            UserPerm userPerm = getUserPerm(user.getId());
            if (userPerm.getDenyPerms().contains(perm)) {
                return false;
            }
            if (userPerm.hasAllPerms())
                return true;
            for (String groupName : userPerm.getExtendsGroups()) {
                if (GROUPS.containsKey(groupName)) {
                    if (GROUPS.get(groupName).getPermissions().contains(perm)) {
                        return true;
                    }
                }
            }
            return userPerm.getPermissions().contains(perm);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static UserPerm getUserPerm(long id) throws IOException {
        if (!USERS.containsKey(id)) {
            File userFile = new File(USERS_FOLDER, id + ".json");
            if (!userFile.exists()) {
                USERS.put(id, new UserPerm(id));
            } else {
                USERS.put(id, JSONUtil.toBean(FileUtil.readFile(userFile), UserPerm.class));
            }
        }
        return USERS.get(id);
    }

    public static boolean hasPerm(GroupPerm group, String perm) {
        return (group.getPermissions().contains(perm)) && !group.getDenyPerms().contains(perm);
        //
    }
}
