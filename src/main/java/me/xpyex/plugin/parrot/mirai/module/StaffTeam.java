package me.xpyex.plugin.parrot.mirai.module;

import lombok.experimental.ExtensionMethod;
import me.xpyex.plugin.parrot.api.CommandMenu;
import me.xpyex.plugin.parrot.mirai.core.command.CommandNode;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.ArgParser;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.UserParser;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;

@SuppressWarnings("unused")
@ExtensionMethod(ArgParser.class)
public class StaffTeam extends Module {
    @Override
    public void register() {
        registerCommand(Group.class, CommandNode.<Group>of((source, sender, arguments) -> {
            new CommandMenu(arguments)
                .add("add <QQID>", "令群员成为管理员")
                .add("remove <QQID>", "令管理员成为普通群员")
                .send(source);
        }).child(CommandNode.of((source, sender, arguments) -> {
                arguments.getArgument(0, UserParser.class, NormalMember.class)//命令为群命令，只打算拿到Member
                    .ifPresentOrElse(member -> {
                        member.modifyAdmin("add".equalsIgnoreCase(arguments.getLabelReverse(0)));
                        source.sendMessage("已完成");
                    }, () -> source.sendMessage("群内无该成员"));

            }), "add", "remove"
        ).executableCheck((source, sender) -> {
            if (source.getContact().getBotPermission() != MemberPermission.OWNER) {
                source.sendMessage("Bot非群主，无法执行此操作");
                return false;
            }
            return true;
        }).notMatchedArg((source, sender, arguments) -> {
            source.sendMessage("未知子命令: " + arguments.buildArguments());
        }).permission(getName() + ".use"), "staff", "StaffTeam", "admin", "administrator");
    }
}
