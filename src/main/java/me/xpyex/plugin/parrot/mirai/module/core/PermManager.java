package me.xpyex.plugin.parrot.mirai.module.core;

import cn.hutool.json.JSONUtil;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.WeakHashMap;
import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;
import me.xpyex.plugin.parrot.mirai.api.CommandMenu;
import me.xpyex.plugin.parrot.mirai.core.command.CommandNode;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.ArgParser;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.GroupParser;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.UserParser;
import me.xpyex.plugin.parrot.mirai.core.module.CoreModule;
import me.xpyex.plugin.parrot.mirai.core.permission.GroupPerm;
import me.xpyex.plugin.parrot.mirai.core.permission.Perms;
import me.xpyex.plugin.parrot.mirai.core.permission.QGroupPerm;
import me.xpyex.plugin.parrot.mirai.core.permission.UserPerm;
import me.xpyex.plugin.parrot.mirai.utils.FileUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import org.jetbrains.annotations.Nullable;

@ExtensionMethod({Perms.class, Arrays.class, ArgParser.class})
public class PermManager extends CoreModule {
    public static final HashMap<String, GroupPerm> GROUPS = new HashMap<>();
    private static final WeakHashMap<Long, UserPerm> USERS = new WeakHashMap<>();
    private static final WeakHashMap<Long, QGroupPerm> QQ_GROUPS = new WeakHashMap<>();
    private static File GROUPS_FOLDER;
    private static File USERS_FOLDER;
    private static File QQ_GROUPS_FOLDER;

    public static boolean hasPerm(User user, String perm, @Nullable MemberPermission adminPass) {
        if (user == null || perm == null || perm.isEmpty()) {
            return false;  //空检查
        }
        if (adminPass != null && user instanceof Member member && member.getPermission().getLevel() >= adminPass.getLevel()) {
            return true;  //用户在群内权限允许规避权限检查
        }
        if (user instanceof Member member) {
            QGroupPerm qGroupPerm = getQGroupPerm(member.getGroup().getId());
            if (qGroupPerm.deniedPerm(perm)) {  //QQ群内禁止此权限
                return false;
            }
            if (getUserPerm(user.getId()).deniedPerm(perm)) {  //用户被禁止此权限
                return false;
            }
            if (qGroupPerm.hasPerm(perm)) {  //QQ群内允许
                return true;
            }

            for (String groupName : qGroupPerm.getExtendsGroups()) {  //QQ群依赖于哪个权限组
                if (GROUPS.containsKey(groupName)) {
                    GroupPerm groupPerm = GROUPS.get(groupName);
                    if (groupPerm.deniedPerm(perm)) return false;
                    if (groupPerm.hasPerm(perm)) {
                        return true;
                    }
                }
            }
        }
        return hasPerm(user.getId(), perm);
    }

    public static boolean hasPerm(long id, String perm) {
        UserPerm userPerm = getUserPerm(id);
        if (userPerm.deniedPerm(perm)) {
            return false;
        }
        if (userPerm.hasAllPerms())
            return true;
        if (userPerm.hasPerm(perm)) {
            return true;
        }
        for (String groupName : userPerm.getExtendsGroups()) {
            if (GROUPS.containsKey(groupName)) {
                GroupPerm groupPerm = GROUPS.get(groupName);
                if (groupPerm.deniedPerm(perm)) return false;
                if (groupPerm.hasPerm(perm)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SneakyThrows
    private static UserPerm getUserPerm(long id) {
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

    @SneakyThrows
    private static QGroupPerm getQGroupPerm(long id) {
        if (!QQ_GROUPS.containsKey(id)) {
            File qGroupFile = new File(QQ_GROUPS_FOLDER, id + ".json");
            if (!qGroupFile.exists()) {
                QQ_GROUPS.put(id, new QGroupPerm(id));
            } else {
                QQ_GROUPS.put(id, JSONUtil.toBean(FileUtil.readFile(qGroupFile), QGroupPerm.class));
            }
        }
        return QQ_GROUPS.get(id);
    }

    @SneakyThrows
    public void reload() {
        GROUPS_FOLDER = new File(getDataFolder(), "Groups");
        USERS_FOLDER = new File(getDataFolder(), "Users");
        QQ_GROUPS_FOLDER = new File(getDataFolder(), "QQGroups");
        GROUPS_FOLDER.mkdirs();
        USERS_FOLDER.mkdirs();
        QQ_GROUPS_FOLDER.mkdirs();

        GROUPS.clear();
        USERS.clear();
        QQ_GROUPS.clear();

        for (File file : GROUPS_FOLDER.listFiles()) {
            if (file.getName().endsWith(".json")) {
                String groupName = file.getName().substring(0, file.getName().lastIndexOf(".json"));
                GROUPS.put(groupName, JSONUtil.toBean(FileUtil.readFile(file), GroupPerm.class));
            }
        }
    }

    @Override
    public void register() {
        reload();

        registerCommand(Contact.class,
            CommandNode.of((source, sender, arguments) -> {
                    new CommandMenu(arguments)
                        .add("newGroup <Name> <isDefault>", "创建新的权限组")
                        .add("reload", "尝试重载所有权限内容")
                        .add("set <Group|User|QGroup> <Name|ID> <Perm> <State>", "给<组|用户|QQ群>修改权限状态")
                        .add("setAll <UserID> <true/false>", "给予用户所有权限")
                        .send(source);
                })
                .executableCheck((source, sender) -> {
                    if (!sender.hasPerm(getName() + ".admin")) {
                        source.sendMessage("你没有权限");
                        return false;
                    }
                    return true;
                })
                .child(CommandNode.of((source, sender, arguments) -> {
                    if (arguments.hasEnoughArg(4)) {
                        source.sendMessage("参数不足");
                        return;
                    }
                    String type = switch (arguments.getArgument(0).toLowerCase()) {
                        case "group" -> "组";
                        case "user" -> "用户";
                        case "qgroup", "qqgroup" -> "群";
                        default -> null;
                    };
                    if (type == null) {
                        source.sendMessage("参数错误: " + arguments.getArgument(0));
                        return;
                    }
                    String id = arguments.getArgument(1);
                    String perm = arguments.getArgument(2).toLowerCase();
                    int state = Integer.parseInt(arguments.getArgument(3));
                    Perms permInstance = switch (type) {
                        case "组" -> GROUPS.get(id);
                        case "用户" -> getUserPerm(UserParser.class.of().getParsedId(id));
                        case "群" -> getQGroupPerm(GroupParser.class.of().getParsedId(id));
                        default -> null;
                    };
                    if (permInstance == null) {
                        source.sendMessage("错误: <" + type + " " + id + "> 不存在");
                        return;
                    }
                    TreeSet<String> denied = Perms.getLowerCaseSet(permInstance.getDenyPerms());
                    TreeSet<String> permitted = Perms.getLowerCaseSet(permInstance.getPermissions());
                    if (switch (state) {
                        case -1 -> permitted.remove(perm) | denied.add(perm);
                        case 0 -> permitted.remove(perm) | denied.remove(perm);
                        case 1 -> permitted.add(perm) | denied.remove(perm);
                        default -> false;
                    }) {
                        source.sendMessage("设置 <" + type + " " + id + "> 的权限 <" + perm + "> 状态为 <" + state + ">");
                    } else {
                        source.sendMessage("设置 <" + type + " " + id + "> 的权限 <" + perm + "> 失败: 无变化");
                    }
                    permInstance.getDenyPerms().clear();
                    permInstance.getDenyPerms().addAll(denied);
                    permInstance.getPermissions().clear();
                    permInstance.getPermissions().addAll(permitted);
                    permInstance.save();
                }), "set")
                .child(CommandNode.of((source, sender, arguments) -> {
                    if (arguments.hasEnoughArg(3)) {
                        source.sendMessage("参数不足");
                        return;
                    }
                    long id = Long.parseLong(arguments.getArgument(0));
                    boolean newState = "true".equalsIgnoreCase(arguments.getArgument(1));
                    getUserPerm(id).setHasAllPerms(newState).save();
                    source.sendMessage("已设定 " + id + " 管理员权限为 " + newState);
                }).executableCheck((source, sender) -> {
                    if (!sender.hasPerm(getName() + ".setOp")) {
                        source.sendMessage("你没有权限");
                        return false;
                    }
                    return true;
                }), "setAll", "op")
                .child(CommandNode.of((source, sender, arguments) -> {
                    reload();
                    source.sendMessage("尝试重载");
                }), "reload")
                .child(CommandNode.of((source, sender, arguments) -> {
                    if (arguments.getArguments().length < 2) {
                        source.sendMessage("参数不足");
                        return;
                    }
                    File f = new File(GROUPS_FOLDER, arguments.getArgument(0) + ".json");
                    if (f.exists()) {
                        source.sendMessage("已存在同名权限组: " + arguments.getArgument(0));
                        return;
                    }
                    FileUtil.writeFile(f, JSONUtil.toJsonPrettyStr(new GroupPerm(arguments.getArgument(0)).setDefaultGroup(arguments.boolArg(1))));
                    reload();
                    source.sendMessage("成功创建组: " + arguments.getArgument(0));
                }), "newGroup")
            , "permission", "permissions", "perm", "perms", "permManager");

        executeOnce(BotOnlineEvent.class, event -> {
            hasPerm(getBot().getAsFriend(), "test", null);  //初始化Perm类
        });
    }
}
