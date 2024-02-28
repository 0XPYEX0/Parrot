package me.xpyex.plugin.allinone.module;

import me.xpyex.plugin.allinone.api.CommandMenu;
import me.xpyex.plugin.allinone.core.module.Module;
import me.xpyex.plugin.allinone.utils.MsgUtil;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;

@SuppressWarnings("unused")
public class StaffTeam extends Module {
    @Override
    public void register() {
        registerCommand(Group.class, ((source, sender, label, args) -> {
            if (source.getContact().getBotPermission() != MemberPermission.OWNER) {
                MsgUtil.sendMsg(source, "Bot非群主，无法执行此操作");
                return;
            }
            if (!sender.hasPerm(getName() + ".use", MemberPermission.ADMINISTRATOR)) {
                if (args.length == 0) {
                    new CommandMenu(label)
                        .add("add <QQID>", "令群员成为管理员")
                        .add("remove <QQID>", "令管理员成为普通群员")
                        .send(source);
                } else if (args.length == 1) {
                    MsgUtil.sendMsg(source, "参数不足");
                } else {
                    try {
                        long targetID = Long.parseLong(args[1]);
                        NormalMember target = source.getContact().get(targetID);
                        if (target == null) {
                            MsgUtil.sendMsg(source, "群内无该成员");
                            return;
                        }
                        if (args[0].equalsIgnoreCase("add")) {
                            target.modifyAdmin(true);
                        } else if (args[0].equalsIgnoreCase("remove")) {
                            target.modifyAdmin(false);
                        } else {
                            MsgUtil.sendMsg(source, "未知子命令");
                            return;
                        }
                        MsgUtil.sendMsg(source, "已完成");
                    } catch (NumberFormatException ignored) {
                        MsgUtil.sendMsg(source, "请输入正确的QQ号");
                    }
                }
            } else {
                MsgUtil.sendMsg(source, "你没有权限");
            }
        }), "staff", "StaffTeam");
    }
}
