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
                    CommandMessager messager = new CommandMessager("有关EssentialsX在其官网发布的信息:")
                            .plus("EssentialsX的开发团队在其官网发布公告，其大意为Mohist诱导用户下载包含恶意代码的非官方EssentialsX插件")
                            .plus("")
                            .plus("事件起因: EssentialsX的开发团队为纯Spigot派系，他们故意不支持任何Hybrid(Forge+Bukkit混合类型)的服务端核心")
                            .plus("他们在代码中检查Forge是否存在，如存在则取消自身加载.")
                            .plus("MohistMC团队关注到该问题后，发布了一份修改版本，使EssentialX能够Mohist上正常加载(即去除其检测)，其它功能未被修改")
                            .plus("该修改版本的EssentialsX在 https://github.com/KR33PY/Essentials 开源")
                            .plus("Mohist并没有强制要求用户必须使用MohistMC团队修改过后的EssentialsX，用户自由选择使用什么版本")
                            .plus("")
                            .plus("如各位亲眼所见，EssentialsX的开发团队仅因Hybrid核心不符合他们的开发理念，便加以阻拦、破坏开发生态、散布虚假信息抹黑Mohist")
                            .plus("Mohist绝无任何恶意损害各位用户的利益的意图")
                            .plus("如今Mohist已对此做了兼容，无需再使用修改版本的EssentialsX.各位用户在Mohist上使用官方版本的EssentialsX即可")
                            .plus("[PS: 造谣一张嘴，辟谣跑断腿 (逃]");
                    source.sendMessage(messager.toString());
                }
            }
        }), "MohistMC", "Mohist");
    }
}
