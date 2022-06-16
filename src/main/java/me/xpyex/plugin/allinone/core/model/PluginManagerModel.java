package me.xpyex.plugin.allinone.core.model;

import me.xpyex.plugin.allinone.core.CommandHelper;
import me.xpyex.plugin.allinone.core.Model;

public class PluginManagerModel extends Model {
    @Override
    public void register() {
        registerCommand(((source, sender, label, args) -> {
            if (sender.getId() == 1723275529) {
                if (args.length == 0) {
                    CommandHelper helper = new CommandHelper(label)
                            .add("enable <模块>", "启用该模块")
                            .add("disable <模块>", "关闭该模块")
                            .add("list", "查询所有模块");
                    source.sendMessage(helper.toString());
                }
            } else {
                source.sendMessage("你没有权限");
            }
        }), "pl", "plugin");
    }
}
