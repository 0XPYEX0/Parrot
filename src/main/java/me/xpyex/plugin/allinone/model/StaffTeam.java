package me.xpyex.plugin.allinone.model;

import me.xpyex.plugin.allinone.api.CommandMenu;
import me.xpyex.plugin.allinone.core.Model;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;

@SuppressWarnings("unused")
public class StaffTeam extends Model {
    @Override
    public void register() {
        registerCommand(Group.class, ((source, sender, label, args) -> {
            if (source.getBotPermission().getLevel() != 2) {
                Util.sendMsg(source, "Bot非群主，无法执行此操作");
                return;
            }
            if (sender.getId() == 1723275529L) {
                if (args.length == 0) {
                    new CommandMenu(label)
                            .add("add <QQID>", "令群员成为管理员")
                            .add("remove <QQID>", "令管理员成为普通群员")
                            .send(source);
                } else if (args.length == 1) {
                    Util.sendMsg(source, "参数不足");
                }
                else {
                    try {
                        long targetID = Long.parseLong(args[1]);
                        NormalMember target = source.get(targetID);
                        if (target == null) {
                            Util.sendMsg(source, "群内无该成员");
                            return;
                        }
                        if (args[0].equalsIgnoreCase("add")) {
                            target.modifyAdmin(true);
                        } else if (args[1].equalsIgnoreCase("remove")) {
                            target.modifyAdmin(false);
                        } else {
                            Util.sendMsg(source, "未知子命令");
                            return;
                        }
                        Util.sendMsg(source, "已完成");
                    } catch (NumberFormatException ignored) {
                        Util.sendMsg(source, "请输入正确的QQ号");
                    }
                }
            } else {
                Util.sendMsg(source, "你没有权限");
            }
        }), "staff", "StaffTeam");
    }
}
