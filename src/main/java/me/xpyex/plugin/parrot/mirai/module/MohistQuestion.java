package me.xpyex.plugin.parrot.mirai.module;

import me.xpyex.plugin.parrot.api.MessageBuilder;
import me.xpyex.plugin.parrot.mirai.core.command.CommandNode;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import net.mamoe.mirai.contact.Group;

@SuppressWarnings("unused")
public class MohistQuestion extends Module {
    @Override
    public void register() {
        registerCommand(Group.class,
            CommandNode.<Group>of()
                .child(CommandNode.of((source, sender, arguments) -> {
                    new MessageBuilder("关于EssentialsX官网红挂MohistMC:")
                        .plus("EssentialsX开发团队为原教旨Spigot主义，采取自爆式手段阻拦含Forge的服务端加载，影响全部混合型服务端.")
                        .plus("MohistMC关注到此问题后，发布开源Fork版本移除此自爆式检测，代码全部开放允许审计，允许用户自由选择，遭到EssentialsX团队恶意抹黑.")
                        .plus("修改版本的EssentialsX开源地址: https://github.com/KR33PY/Essentials")
                        .plus("现版本Mohist已对EssentialsX加载进行额外处理，官方版本EssentialsX亦可直接加载使用.")
                        .plus("")
                        .plus("如各位所见，EssentialsX开发团队仅因Forge+Bukkit服务端不符合其开发理念，便极力阻拦、破坏开发生态，甚至散播谣言抹黑Mohist，其心可诛")
                        .plus("尽管如此，我们依然推荐有财力的服主使用更强大、功能更完善、更方便使用的CMI来替代EssentialsX")
                        .plus("")
                        .plus("[PS: 造谣一张嘴，辟谣跑断腿 (逃]")
                        .send(source);
                }), "ess")
                .child(CommandNode.of((source, sender, arguments) -> {
                    new MessageBuilder()
                        .plus("如何捐助？")
                        .plus("https://afdian.com/@MohistMC")
                        .send(source);
                }), "donate")
            , "MohistMC", "Mohist");

    }
}
