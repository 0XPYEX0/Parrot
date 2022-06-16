package me.xpyex.plugin.allinone.core.model;

import me.xpyex.plugin.allinone.core.CommandHelper;
import me.xpyex.plugin.allinone.core.Model;

public class RestartBroadcast extends Model {
    @Override
    public void register() {
        registerCommand(((source, sender, label, args) -> {
            if (sender.getId() != 1723275529L) {
                source.sendMessage("你没有权限");
                return;
            }
            if (args.length == 0) {
                CommandHelper helper = new CommandHelper(label)
                        .add("start", "添加一个重启任务，在10秒后重启")
                        .add("stop", "停止现有的重启任务")
                        .add("now", "立刻重启")
                        .add("exit", "退出机器人，不重启");
                source.sendMessage(helper.toString());
            } else if (args[0].equalsIgnoreCase("start")) {

            }
        }), "rbc");
    }
}
