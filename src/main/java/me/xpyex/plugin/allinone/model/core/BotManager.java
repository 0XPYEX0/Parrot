package me.xpyex.plugin.allinone.model.core;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import me.xpyex.plugin.allinone.api.CommandMenu;
import me.xpyex.plugin.allinone.api.CommandMessager;
import me.xpyex.plugin.allinone.core.CoreModel;
import me.xpyex.plugin.allinone.utils.MsgUtil;
import me.xpyex.plugin.allinone.utils.StringUtil;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent;
import net.mamoe.mirai.event.events.GroupEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.event.events.NewFriendRequestEvent;
import net.mamoe.mirai.event.events.NudgeEvent;

@SuppressWarnings("unused")
public class BotManager extends CoreModel {
    private static final ArrayList<String> IGNORED_LIST = new ArrayList<>();

    @Override
    public void register() {
        registerCommand(Contact.class, ((source, sender, label, args) -> {
            if (sender.getId() != Util.OWNER_ID) {
                MsgUtil.sendMsg(source, "你没有权限");
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
                        MsgUtil.sendMsg(source, "参数不足，请填写ID");
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
                        if (source.isGroup()) {
                            id = source.getContact().getId();
                        } else {
                            MsgUtil.sendMsg(source, "该命令不在群内执行，不可使用this替代符");
                            return;
                        }
                    } else {
                        id = Long.parseLong(args[2]);
                    }
                    group = Util.getBot().getGroupOrFail(id);
                } catch (NumberFormatException ignored) {
                    MsgUtil.sendMsg(source, "填入的群号非整数");
                    return;
                } catch (NoSuchElementException ignored) {
                    MsgUtil.sendMsg(source, "机器人并未进入指定群，无法操作");
                    return;
                }
                if (args[1].equalsIgnoreCase("quit")) {
                    MsgUtil.sendMsg(source, "执行操作: 退出群 " + group.getId());
                    group.quit();
                    return;
                }
                if (args[1].equalsIgnoreCase("ignore")) {
                    MsgUtil.sendMsg(source, "执行操作: 忽略群 " + group.getId());
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
                        MsgUtil.sendMsg(source, "参数不足，请填写ID");
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
                    friend = Util.getBot().getFriendOrFail(Long.parseLong(args[2]));
                } catch (NumberFormatException ignored) {
                    MsgUtil.sendMsg(source, "填入的群号非整数");
                    return;
                } catch (NoSuchElementException ignored) {
                    MsgUtil.sendMsg(source, "机器人并非指定QQ的好友，无法操作");
                    return;
                }
                if (StringUtil.equalsIgnoreCaseOr(args[1], "del", "delete")) {
                    if (friend.getId() == Util.OWNER_ID) {
                        MsgUtil.sendMsg(source, "不允许删除该好友");
                        return;
                    }
                    MsgUtil.sendMsg(source, "执行操作: 删除好友 " + friend.getId());
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
                        MsgUtil.sendMsg(source, "参数不足，请填写ID");
                        return;
                    }
                }
                try {
                    long id = Long.parseLong(args[2]);
                    if (args[1].equalsIgnoreCase("ignore")) {
                        if (id == Util.OWNER_ID) {
                            MsgUtil.sendMsg(source, "不允许屏蔽该用户");
                            return;
                        }
                        MsgUtil.sendMsg(source, "执行操作: 忽略用户 " + id);
                        IGNORED_LIST.add("User-" + id);
                        return;
                    }
                } catch (NumberFormatException ignored) {
                    MsgUtil.sendMsg(source, "填入的QQ号非整数");
                    return;
                }
            }


            MsgUtil.sendMsg(source, "未知子命令，请执行 #" + label + " 查看帮助");
        }), "BotManager", "Bot");
        listenEvent(BotInvitedJoinGroupRequestEvent.class, event -> {
            if (event.getInvitorId() == Util.OWNER_ID) {
                event.accept();
            }
        });
        listenEvent(NewFriendRequestEvent.class, event -> MsgUtil.sendMsgToOwner(
            event.getFromNick() + " (" + event.getFromId() + ")\n"
                + "请求添加好友" +
                "\n" +
                "申请理由: " + event.getMessage()
        ));
    }

    @Override
    @SuppressWarnings("all")
    public boolean acceptEvent(Event event) {
        if (event instanceof GroupEvent && IGNORED_LIST.contains("Group-" + ((GroupEvent) event).getGroup().getId()))
            return false;

        if (event instanceof MessageEvent && IGNORED_LIST.contains("User-" + ((MessageEvent) event).getSender().getId()))
            return false;

        if (event instanceof NudgeEvent && (IGNORED_LIST.contains("User-" + ((NudgeEvent) event).getFrom().getId()) || IGNORED_LIST.contains("Group-" + ((NudgeEvent) event).getSubject().getId())))
            return false;
        return true;
    }
}
