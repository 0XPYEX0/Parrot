package me.xpyex.plugin.allinone.model;

import me.xpyex.plugin.allinone.api.CommandMessager;
import me.xpyex.plugin.allinone.core.Model;
import net.mamoe.mirai.contact.Group;

public class MohistQuestion extends Model {
    @Override
    public void register() {
        registerCommand(Group.class, ((source, sender, label, args) -> {
            if (args.length != 0) {
                if (args[0].equalsIgnoreCase("ess")) {
                    source.sendMessage("关于EssX官网红挂MohistMC：EssX团队为原教旨Spigot主义，采取自爆式手段阻拦含Forge的服务端加载，影响全部混合型服务端。MohistMC关注到此问题后，发布开源Fork版本移除此自爆式检测，代码全部开放允许审计，允许用户自由选择，遭到EssX团队恶意抹黑。现版本Mohist已对EssX加载进行额外处理，官方版本EssX亦可直接加载使用。");
                }
            }
        }), "MohistMC", "Mohist");
    }
}
