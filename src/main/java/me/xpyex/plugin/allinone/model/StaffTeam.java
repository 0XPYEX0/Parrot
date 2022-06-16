package me.xpyex.plugin.allinone.model;

import me.xpyex.plugin.allinone.core.CommandHelper;
import me.xpyex.plugin.allinone.core.Model;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;

public class StaffTeam extends Model {
    @Override
    public void register() {
        registerCommand(((source, sender, label, args) -> {
            if (!(source instanceof Group)) {
                source.sendMessage("该命令只能在群内使用");
                return;
            }
            if (((Group) source).getBotPermission().getLevel() != 2) {
                source.sendMessage("Bot非群主，无法执行此操作");
                return;
            }
            if (sender.getId() == 1723275529L) {
                if (args.length == 0) {
                    CommandHelper helper = new CommandHelper(label)
                            .add("add <QQID>", "令群员成为管理员")
                            .add("remove <QQID>", "令管理员成为普通群员");
                    source.sendMessage(helper.toString());
                } else if (args.length == 1) {
                    source.sendMessage("参数不足");
                }
                else {
                    try {
                        long targetID = Long.parseLong(args[1]);
                        NormalMember target = ((Group) source).get(targetID);
                        if (target == null) {
                            source.sendMessage("群内无该成员");
                            return;
                        }
                        if (args[0].equalsIgnoreCase("add")) {
                            target.modifyAdmin(true);
                        } else if (args[1].equalsIgnoreCase("remove")) {
                            target.modifyAdmin(false);
                        } else {
                            source.sendMessage("未知子命令");
                            return;
                        }
                        source.sendMessage("已完成");
                    } catch (NumberFormatException ignored) {
                        source.sendMessage("请输入正确的QQ号");
                    }
                }
            } else {
                source.sendMessage("你没有权限");
            }
        }), "staff", "StaffTeam");
    }
}
