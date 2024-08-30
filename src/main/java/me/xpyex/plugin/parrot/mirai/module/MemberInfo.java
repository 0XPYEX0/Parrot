package me.xpyex.plugin.parrot.mirai.module;

import java.util.Arrays;
import lombok.experimental.ExtensionMethod;
import me.xpyex.plugin.parrot.mirai.core.command.argument.ArgParser;
import me.xpyex.plugin.parrot.mirai.core.command.argument.UserParser;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.utils.MsgUtil;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;

@SuppressWarnings("unused")
@ExtensionMethod(ArgParser.class)
public class MemberInfo extends Module {
    @Override
    public void register() {
        registerCommand(Group.class, ((source, sender, label, args) -> {
            if (!sender.hasPerm(getName() + ".use", MemberPermission.ADMINISTRATOR)) {
                MsgUtil.sendMsg(source, "你没有权限");
                return;
            }
            UserParser.class.of().parse(args[0], NormalMember.class).ifPresentOrElse(member -> {
                if (member.getId() != source.getContact().getBot().getId() /* 不是修改Bot本身 */ && source.getContact().getBotPermission().getLevel() < MemberPermission.ADMINISTRATOR.getLevel()) {
                    source.sendMessage("Bot群内权限不足");
                    return;
                }
                member.setNameCard(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                MsgUtil.sendMsg(source, "已修改");
            }, () -> MsgUtil.sendMsg(source, "参数不足"));
        }), "setNameCard", "nameCard");

        registerCommand(Group.class, (source, sender, label, args) -> {
            if (!sender.hasPerm(getName() + ".use", MemberPermission.ADMINISTRATOR)) {
                MsgUtil.sendMsg(source, "你没有权限");
                return;
            }
            UserParser.class.of().parse(args[0], NormalMember.class).ifPresentOrElse(member -> {
                if (member.getId() != source.getContact().getBot().getId() /* 不是修改Bot本身 */ && source.getContact().getBotPermission() != MemberPermission.OWNER  /* 头衔要群主才能改 */) {
                    source.sendMessage("Bot群内权限不足");
                    return;
                }
                member.setSpecialTitle(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                MsgUtil.sendMsg(source, "已修改");
            }, () -> MsgUtil.sendMsg(source, "参数不足"));
        }, "prefix", "groupPrefix");
    }
}
