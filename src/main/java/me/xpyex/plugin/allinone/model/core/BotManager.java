package me.xpyex.plugin.allinone.model.core;

import java.util.ArrayList;
import me.xpyex.plugin.allinone.api.CommandMenu;
import me.xpyex.plugin.allinone.api.CommandMessager;
import me.xpyex.plugin.allinone.core.CoreModel;
import me.xpyex.plugin.allinone.utils.StringUtil;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;

public class BotManager extends CoreModel {
    public static final ArrayList<String> IGNORED_LIST = new ArrayList<>();

    @Override
    public void register() {
        registerCommand(Contact.class, ((source, sender, label, args) -> {
            if (sender.getId() != Util.OWNER_ID) {
                source.sendMessage("你没有权限");
                return;
            }



            if (args.length == 0) {  //根帮助
                CommandMenu menu = new CommandMenu(label)
                    .add("group", "群相关操作")
                    .add("friend", "好友相关操作")
                    .add("user", "用户相关操作");
                menu.send(source);
                return;
            }



            if (args[0].equalsIgnoreCase("group")) {  //群相关
                if (args.length == 1) {
                    CommandMenu menu = new CommandMenu(label + " group")
                        .add("quit <ID>", "令机器人退出该群")
                        .add("ignore <ID>", "忽略该群触发的事件")
                        .add("list", "列出该机器人加入的所有群");
                    menu.send(source);
                    return;
                }
                if (args.length == 2) {
                    if (StringUtil.equalsIgnoreCaseOr(args[1], "quit", "ignore")) {
                        source.sendMessage("参数不足，请填写ID");
                        return;
                    }
                    if (args[1].equalsIgnoreCase("list")) {
                        CommandMessager messager = new CommandMessager();
                        messager.plus("机器人加入的群列表: ");
                        for (Group g : Util.getBot().getGroups()) {
                            messager.plus(g.getName() + " (" + g.getId() + ")");
                        }
                        messager.send(source);
                        return;
                    }
                }
                Group group;
                try {
                    long id;
                    if (args[2].equalsIgnoreCase("this")) {
                        if (source instanceof Group) {
                            id = source.getId();
                        } else {
                            source.sendMessage("该命令不在群内执行，不可使用this替代符");
                            return;
                        }
                    } else {
                        id = Long.parseLong(args[2]);
                    }
                    group = Util.getBot().getGroupOrFail(id);
                } catch (NumberFormatException ignored) {
                    source.sendMessage("填入的群号非整数");
                    return;
                } catch (NullPointerException ignored) {
                    source.sendMessage("机器人并未进入指定群，无法操作");
                    return;
                }
                if (args[1].equalsIgnoreCase("quit")) {
                    source.sendMessage("执行操作: 退出群 " + group.getId());
                    group.quit();
                    return;
                }
                if (args[1].equalsIgnoreCase("ignore")) {
                    source.sendMessage("执行操作: 忽略群 " + group.getId());
                    IGNORED_LIST.add("Group-" + group.getId());
                    return;
                }
            }



            if (args[0].equalsIgnoreCase("friend")) {  //好友相关
                if (args.length == 1) {
                    CommandMenu menu = new CommandMenu(label + " friend")
                                           .add("delete <ID>", "令机器人删除该好友")
                                           .add("list", "列出该机器人的好友列表");
                    menu.send(source);
                    return;
                }
                if (args.length == 2) {
                    if (StringUtil.equalsIgnoreCaseOr(args[1], "del", "delete")) {
                        source.sendMessage("参数不足，请填写ID");
                        return;
                    }
                    if (args[1].equalsIgnoreCase("list")) {
                        CommandMessager messager = new CommandMessager();
                        messager.plus("机器人的好友列表: ");
                        for (Friend f : Util.getBot().getFriends()) {
                            messager.plus(f.getNick() + " (" + f.getId() + ")");
                        }
                        messager.send(source);
                        return;
                    }
                }
                Friend friend;
                try {
                    long id = Long.parseLong(args[2]);
                    friend = Util.getBot().getFriendOrFail(id);
                } catch (NumberFormatException ignored) {
                    source.sendMessage("填入的群号非整数");
                    return;
                } catch (NullPointerException ignored) {
                    source.sendMessage("机器人并未进入指定群，无法操作");
                    return;
                }
                if (StringUtil.equalsIgnoreCaseOr(args[1], "del", "delete")) {
                    if (friend.getId() == Util.OWNER_ID) {
                        source.sendMessage("不允许删除该好友");
                        return;
                    }
                    source.sendMessage("执行操作: 删除好友 " + friend.getId());
                    friend.delete();
                    return;
                }
            }



            if (args[0].equalsIgnoreCase("user")) {  //用户相关
                if (args.length == 1) {
                    CommandMenu menu = new CommandMenu(label + " user")
                        .add("ignore <ID>", "忽略该用户触发的事件");
                    menu.send(source);
                    return;
                }
                if (args.length == 2) {
                    if (args[1].equalsIgnoreCase("ignore")) {
                        source.sendMessage("参数不足，请填写ID");
                        return;
                    }
                }
                try {
                    long id = Long.parseLong(args[2]);
                    if (args[1].equalsIgnoreCase("ignore")) {
                        if (id == Util.OWNER_ID) {
                            source.sendMessage("不允许屏蔽该用户");
                            return;
                        }
                        source.sendMessage("执行操作: 忽略用户 " + id);
                        IGNORED_LIST.add("User-" + id);
                        return;
                    }
                } catch (NumberFormatException ignored) {
                    source.sendMessage("填入的QQ号非整数");
                    return;
                }
            }



            source.sendMessage("未知子命令，请执行 #" + label + " 查看帮助");
        }), "BotManager", "Bot");
    }
}
