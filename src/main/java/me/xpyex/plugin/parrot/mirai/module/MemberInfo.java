package me.xpyex.plugin.parrot.mirai.module;

import java.util.Arrays;
import lombok.experimental.ExtensionMethod;
import me.xpyex.plugin.parrot.mirai.core.command.CommandNode;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.ArgParser;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.UserParser;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;

@SuppressWarnings("unused")
@ExtensionMethod(ArgParser.class)
public class MemberInfo extends Module {
    @Override
    public void register() {
        registerCommand(Group.class, CommandNode.<Group>of((source, sender, arguments) -> {
            arguments.getArgument(0, UserParser.class, NormalMember.class).ifPresentOrElse(member -> {
                if (member.getId() != source.getContact().getBot().getId() /* 不是修改Bot本身 */ && source.getContact().getBotPermission().getLevel() < MemberPermission.ADMINISTRATOR.getLevel()) {
                    source.sendMessage("Bot群内权限不足");
                    return;
                }
                member.setNameCard(String.join(" ", Arrays.copyOfRange(arguments.getArguments(), 1, arguments.getArguments().length)));
                source.sendMessage("已修改");
            }, () -> source.sendMessage("参数不足"));
        }).permission(getName() + ".use", MemberPermission.ADMINISTRATOR), "setNameCard", "nameCard");

        registerCommand(Group.class, CommandNode.<Group>of((source, sender, arguments) -> {
            arguments.getArgument(0, UserParser.class, NormalMember.class).ifPresentOrElse(member -> {
                if (member.getId() != source.getContact().getBot().getId() /* 不是修改Bot本身 */ && source.getContact().getBotPermission() != MemberPermission.OWNER  /* 头衔要群主才能改 */) {
                    source.sendMessage("Bot群内权限不足");
                    return;
                }
                member.setSpecialTitle(String.join(" ", Arrays.copyOfRange(arguments.getArguments(), 1, arguments.getArguments().length)));
                source.sendMessage("已修改");
            }, () -> source.sendMessage("参数不足"));
        }).permission(getName() + ".use", MemberPermission.ADMINISTRATOR), "prefix", "groupPrefix");
    }
}
