package me.xpyex.plugin.parrot.mirai.module;

import java.util.Arrays;
import lombok.experimental.ExtensionMethod;
import me.xpyex.plugin.parrot.mirai.api.CommandMenu;
import me.xpyex.plugin.parrot.mirai.core.command.CommandNode;
import me.xpyex.plugin.parrot.mirai.core.command.argument.ArgParser;
import me.xpyex.plugin.parrot.mirai.core.command.argument.UserParser;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;

@SuppressWarnings("unused")
@ExtensionMethod(ArgParser.class)
public class StaffTeam extends Module {
    @Override
    public void register() {
        registerCommand(Group.class, new CommandNode<Group>().setExecutor((source, sender, label, args) -> {
            new CommandMenu(label)
                .add("add <QQID>", "令群员成为管理员")
                .add("remove <QQID>", "令管理员成为普通群员")
                .send(source);
        }).child(CommandNode.of((source, sender, nodeArgSelf, argsLater) -> {
                UserParser.class.of().parse(() -> argsLater[0], NormalMember.class)  //命令为群命令，只打算拿到Member
                    .ifPresentOrElse(member -> {
                        member.modifyAdmin("add".equalsIgnoreCase(nodeArgSelf[1]));
                        source.sendMessage("已完成");
                    }, () -> source.sendMessage("群内无该成员"));

            }), "add", "remove"
        ).executableCheck((source, sender) -> {
            if (source.getContact().getBotPermission() != MemberPermission.OWNER) {
                source.sendMessage("Bot非群主，无法执行此操作");
                return false;
            }
            if (!sender.hasPerm(getName() + ".use")) {
                source.sendMessage("你没有权限");
                return false;
            }
            return true;
        }).notMatchedArg((source, sender, nodeArgSelf, argsLater) -> {
            source.sendMessage("未知子命令: " + Arrays.toString(argsLater));
        }), "staff", "StaffTeam", "admin", "administrator");
    }
}
