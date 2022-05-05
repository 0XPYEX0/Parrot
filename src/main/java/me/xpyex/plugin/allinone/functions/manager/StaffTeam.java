package me.xpyex.plugin.allinone.functions.manager;

import me.xpyex.plugin.allinone.Main;
import me.xpyex.plugin.allinone.utils.Util;
import me.xpyex.plugin.allinone.commands.CommandsList;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.HashSet;

public class StaffTeam {
    public static final HashSet<String> CHECK_ARGS = new HashSet<>();
    static {
        CHECK_ARGS.add("add");
        CHECK_ARGS.add("remove");
    }
    public static void load() {
        CommandsList.register(StaffTeam.class, "/StaffTeam");
        Main.LOGGER.info("StaffTeam模块已加载");
        GlobalEventChannel.INSTANCE.registerListenerHost(new SimpleListenerHost(Main.INSTANCE.getCoroutineContext()) {
            @EventHandler
            public void onMsg(MessageEvent event) {
                String[] cmd = Util.getPlainText(event.getMessage()).split(" ");
                if (CommandsList.isCmd(StaffTeam.class, cmd[0])) {
                    if (event.getSender().getId() != 1723275529L) {
                        Util.autoSendMsg(event, "你无权进行该操作");
                        return;
                    }
                    if (cmd.length < 3) {
                        String plus = Util.isGroupEvent(event) ? "" : " <群号>";
                        Util.autoSendMsg(event, cmd[0] + " add <成员QQ号>" + plus + "\n"
                                + cmd[0] + " remove <成员QQ号>" + plus);
                        return;
                    }
                    if (!CHECK_ARGS.contains(cmd[1].toLowerCase())) {
                        Util.autoSendMsg(event, "未知子命令");
                        return;
                    }
                    boolean state = cmd[1].equalsIgnoreCase("add");
                    try {
                        if (event instanceof GroupMessageEvent) {
                            if (((GroupMessageEvent) event).getGroup().getBotAsMember().getPermission() != MemberPermission.OWNER) {
                                Util.autoSendMsg(event, "Bot无权进行该操作");
                                return;
                            }
                            NormalMember member = ((GroupMessageEvent) event).getGroup().get(Long.parseLong(cmd[2]));
                            if (member == null) {
                                Util.autoSendMsg(event, "无法找到成员");
                                return;
                            }
                            member.modifyAdmin(state);
                        } else {
                            Group targetGroup = event.getBot().getGroup(Long.parseLong(cmd[3]));
                            if (targetGroup == null || targetGroup.getBotAsMember().getPermission() != MemberPermission.OWNER) {
                                Util.autoSendMsg(event, "Bot无权进行该操作");
                                return;
                            }
                            NormalMember member = targetGroup.get(Long.parseLong(cmd[2]));
                            if (member == null) {
                                Util.autoSendMsg(event, "无法找到成员");
                                return;
                            }
                            member.modifyAdmin(state);
                        }
                        Util.autoSendMsg(event, "已完成");
                    } catch (NumberFormatException ignored) {
                        Util.autoSendMsg(event, "参数错误: 类型非整型");
                    }
                }
            }
        });
    }
}
