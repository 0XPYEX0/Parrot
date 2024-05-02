package me.xpyex.plugin.parrot.mirai.module;

import me.xpyex.plugin.parrot.mirai.api.CommandMenu;
import me.xpyex.plugin.parrot.mirai.core.command.argument.ArgParser;
import me.xpyex.plugin.parrot.mirai.core.command.argument.UserParser;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;

@SuppressWarnings("unused")
public class StaffTeam extends Module {
    @Override
    public void register() {
        registerCommand(Group.class, ((source, sender, label, args) -> {
            if (source.getContact().getBotPermission() != MemberPermission.OWNER) {
                source.sendMessage("Bot非群主，无法执行此操作");
                return;
            }
            if (!sender.hasPerm(getName() + ".use")) {
                source.sendMessage("你没有权限");
                return;
            }
            if (args.length == 0) {
                new CommandMenu(label)
                    .add("add <QQID>", "令群员成为管理员")
                    .add("remove <QQID>", "令管理员成为普通群员")
                    .send(source);
                return;
            }
            ArgParser.of(UserParser.class).parse(() -> args[1], NormalMember.class)  //命令为群命令，只打算拿到Member
                .ifPresentOrElse(member -> {
                    if ("add".equalsIgnoreCase(args[0])) {
                        member.modifyAdmin(true);
                    } else if ("remove".equalsIgnoreCase(args[0])) {
                        member.modifyAdmin(false);
                    } else {
                        source.sendMessage("未知子命令");
                        return;
                    }
                    source.sendMessage("已完成");
                }, () -> source.sendMessage("群内无该成员"));
        }), "staff", "StaffTeam", "admin", "administrator");
    }
}
