package me.xpyex.plugin.parrot.mirai.module.core;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import lombok.experimental.ExtensionMethod;
import me.xpyex.plugin.parrot.mirai.api.CommandMenu;
import me.xpyex.plugin.parrot.mirai.api.MessageBuilder;
import me.xpyex.plugin.parrot.mirai.core.command.CommandNode;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.ArgParser;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.GroupParser;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.UserParser;
import me.xpyex.plugin.parrot.mirai.core.module.CoreModule;
import me.xpyex.plugin.parrot.mirai.utils.Util;
import net.mamoe.mirai.console.MiraiConsole;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent;
import net.mamoe.mirai.event.events.GroupEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.event.events.NewFriendRequestEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.event.events.UserEvent;

@SuppressWarnings("unused")
@ExtensionMethod(ArgParser.class)
public class BotManager extends CoreModule {
    private static final ArrayList<String> IGNORED_LIST = new ArrayList<>();
    private static final ArrayList<NewFriendRequestEvent> REQUESTS = new ArrayList<>();

    @Override
    public void register() {
        registerCommand(Contact.class,
            CommandNode.of((source, sender, arguments) -> {
                    new CommandMenu(arguments)
                        .add("group", "群相关操作")
                        .add("friend", "好友相关操作")
                        .add("user", "用户相关操作")
                        .add("end|exit|shutdown|stop", "关闭Bot，自动重启")
                        .send(source);
                }).executableCheck((source, sender) -> {
                    if (!sender.hasPerm(getName() + ".use")) {
                        source.sendMessage("你没有权限");
                        return false;
                    }
                    return true;
                })
                .child(CommandNode.of((source, sender, arguments) -> {
                            new CommandMenu(arguments)
                                .add("quit <ID>", "令机器人退出该群")
                                .add("ignore <ID>", "忽略该群触发的事件")
                                .add("list", "列出该机器人加入的所有群")
                                .send(source);
                        })
                           .child(CommandNode.of((source, sender, arguments) -> {
                               MessageBuilder messager = new MessageBuilder();
                               messager.plus("机器人加入的群列表: ");
                               getBot().getGroups().forEach(group -> messager.plus(group.getName() + " (" + group.getId() + ")"));
                               messager.send(source);
                           }), "list")
                           .child(CommandNode.of((source, sender, arguments) -> {
                               arguments.getArgument(0, GroupParser.class, Group.class)
                                   .ifPresentOrElse(group -> {
                                           source.sendMessage("执行操作: 忽略群 " + group.getId());
                                           IGNORED_LIST.add("Group-" + group.getId());
                                       }, () ->
                                              new MessageBuilder()
                                                  .plus("群不存在")
                                                  .plus("原因可能是: ")
                                                  .plus("①群不存在，即群号输入有误")
                                                  .plus("②Bot不在指定群内")
                                                  .send(source)
                                   );
                           }), "quit")
                           .child(CommandNode.of((source, sender, arguments) -> {
                               arguments.getArgument(0, GroupParser.class, Group.class)
                                   .ifPresentOrElse(group -> {
                                           source.sendMessage("执行操作: 忽略群 " + group.getId());
                                           IGNORED_LIST.add("Group-" + group.getId());
                                       }, () ->
                                              new MessageBuilder()
                                                  .plus("群不存在")
                                                  .plus("原因可能是: ")
                                                  .plus("①群不存在，即群号输入有误")
                                                  .plus("②Bot不在指定群内")
                                                  .send(source)
                                   );
                           }), "ignore")
                    , "group")
                .child(CommandNode.of((source, sender, arguments) -> {
                            new CommandMenu(arguments)
                                .add("delete <ID>", "令机器人删除该好友")
                                .add("list", "列出该机器人的好友列表")
                                .send(source);
                        })
                           .child(CommandNode.of((source, sender, arguments) -> {
                               MessageBuilder messager = new MessageBuilder();
                               messager.plus("机器人的好友列表: ");
                               for (Friend f : getBot().getFriends()) {
                                   messager.plus(f.getNick() + " (" + f.getId() + ")");
                               }
                               messager.send(source);
                           }), "list")
                           .child(CommandNode.of((source, sender, arguments) -> {
                               try {
                                   NewFriendRequestEvent event = REQUESTS.get(Integer.parseInt(arguments.getArgument(0)));
                                   if ("accept".equalsIgnoreCase(arguments.getLabelReverse(0))) {
                                       event.accept();
                                   } else {
                                       event.reject(false);
                                   }
                                   new MessageBuilder()
                                       .plus("已处理编号为 " + arguments.getArgument(0) + " 的好友申请")
                                       .plus("ID: " + event.getFromId())
                                       .plus("Nick: " + event.getFromNick())
                                       .plus("Group: " + event.getFromGroupId())
                                       .send(source);
                                   REQUESTS.remove(Integer.parseInt(arguments.getArgument(0)));
                               } catch (NoSuchElementException |
                                        NumberFormatException ignored) {
                                   source.sendMessage("没有这条申请");
                               }
                           }), "accept", "deny")
                           .child(CommandNode.of((source, sender, arguments) -> {
                               arguments.getArgument(0, UserParser.class, Friend.class)
                                   .ifPresentOrElse(friend -> {
                                       if (PermManager.hasPerm(friend, "BotManager.admin", null)) {
                                           source.sendMessage("不允许删除该好友");
                                           return;
                                       }
                                       source.sendMessage("执行操作: 删除好友 " + friend.getId());
                                       friend.delete();
                                   }, () -> {
                                       new MessageBuilder("不存在该好友")
                                           .plus("可能性如下: ")
                                           .plus("①填入的QQ号非整数")
                                           .plus("②机器人并非指定QQ的好友，无法操作");
                                   });
                           }), "del", "delete")
                    , "friend")
                .child(CommandNode.of((source, sender, arguments) -> {
                            new CommandMenu(arguments)
                                .add("ignore <ID>", "忽略该用户触发的事件")
                                .send(source);
                        })
                           .child(CommandNode.of((source, sender, arguments) -> {
                               arguments.getArgument(0, UserParser.class, User.class).ifPresentOrElse(user -> {
                                   if (PermManager.hasPerm(user, "PermManager.admin", null)) {
                                       source.sendMessage("不允许屏蔽该用户");
                                       return;
                                   }
                                   source.sendMessage("执行操作: 忽略用户 " + user.getId());
                                   IGNORED_LIST.add("User-" + user.getId());
                               }, () -> {
                                   source.sendMessage("参数不足，请填入ID");
                               });
                           }), "ignore")
                    , "user")
                .child(CommandNode.of((source, sender, arguments) -> {
                    source.sendMessage("开始重启");
                    MiraiConsole.shutdown();
                }), "shutdown", "exit", "stop", "end")
                .child(CommandNode.of((source, sender, arguments) -> {
                    new MessageBuilder()
                        .plus("当前运行环境信息: ")
                        .plus("系统: " + System.getProperty("os.name"))
                        .plus("已分配内存: " + Runtime.getRuntime().totalMemory() / 1024 / 1024 + "MB")
                        .plus("剩余可用内存: " + Runtime.getRuntime().freeMemory() / 1024 / 1024 + "/" + Runtime.getRuntime().maxMemory() / 1024 / 1024 + "MB")
                        .send(source);
                }), "info")
                .notMatchedArg((source, sender, arguments) -> {
                        source.sendMessage("未知子命令，请执行 #" + arguments.getLabel(0) + " 查看帮助");
                    }
                )
            , "BotManager", "Bot");

        listenEvent(BotInvitedJoinGroupRequestEvent.class, event -> {
            User user = event.getInvitor();
            String perm = getName() + ".invite";
            if (PermManager.hasPerm(user, perm, null)) {
                event.accept();
            }
        });

        listenEvent(NewFriendRequestEvent.class, event -> {
            REQUESTS.add(event);
            new MessageBuilder()
                .plus(event.getFromNick() + " (" + event.getFromId() + ")")
                .plus("请求添加好友")
                .plus("申请理由: " + event.getMessage())
                .plus("执行")
                .plus("#bot friend accept " + (REQUESTS.size() - 1) + "  同意申请")
                .plus("或")
                .plus("#bot friend deny " + (REQUESTS.size() - 1) + "  拒绝申请")
                .send(Util.getOwner());
        });
    }

    @Override
    public boolean acceptEvent(Event event) {
        if (event instanceof GroupEvent ge && IGNORED_LIST.contains("Group-" + ge.getGroup().getId()))
            return false;

        if (event instanceof UserEvent ue && IGNORED_LIST.contains("User-" + ue.getUser().getId()))
            return false;

        if (event instanceof MessageEvent me && IGNORED_LIST.contains("User-" + me.getSender().getId()))
            return false;

        if (event instanceof NudgeEvent ne && (IGNORED_LIST.contains("User-" + ne.getFrom().getId()) || IGNORED_LIST.contains("Group-" + ne.getSubject().getId())))
            return false;
        return true;
    }
}
