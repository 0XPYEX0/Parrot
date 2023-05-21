package me.xpyex.plugin.allinone.model;

import me.xpyex.plugin.allinone.core.Model;
import me.xpyex.plugin.allinone.utils.MsgUtil;
import net.mamoe.mirai.contact.Group;

@SuppressWarnings("unused")
public class SetNameCard extends Model {
    @Override
    public void register() {
        registerCommand(Group.class, ((source, sender, label, args) -> {
            if (sender.getId() != 1723275529L) {
                MsgUtil.sendMsg(source, "你没有权限");
                return;
            }
            if (args.length == 0) {
                MsgUtil.sendMsg(source, "参数不足");
                return;
            }
            source.getContact().getBotAsMember().setNameCard(String.join(" ", args));
            MsgUtil.sendMsg(source, "已修改");
        }), "setNameCard", "nameCard");
        DEFAULT_DISABLED = true;
    }
}
