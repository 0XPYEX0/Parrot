package me.xpyex.plugin.allinone.core.model;

import java.util.TreeSet;
import me.xpyex.plugin.allinone.core.CommandMenu;
import me.xpyex.plugin.allinone.core.CoreModel;
import me.xpyex.plugin.allinone.core.Model;
import net.mamoe.mirai.contact.Contact;

public class PluginManagerModel extends CoreModel {
    @Override
    public void register() {
        registerCommand(Contact.class, ((source, sender, label, args) -> {
            if (sender.getId() == 1723275529) {
                if (args.length == 0) {
                    CommandMenu helper = new CommandMenu(label)
                            .add("enable <模块>", "启用该模块")
                            .add("disable <模块>", "关闭该模块")
                            .add("list", "查询所有模块");
                    source.sendMessage(helper.toString());
                } else if (args[0].equalsIgnoreCase("disable")) {
                    if (args.length == 1) {
                        source.sendMessage("参数不足");
                        return;
                    }
                    Model target = Model.getModel(args[1]);
                    if (target == null) {
                        source.sendMessage("模块不存在\n执行 #" + label + " list 查看所有列表");
                        return;
                    }
                    if (target instanceof CoreModel) {
                        source.sendMessage("不允许操作核心模块");
                        return;
                    }
                    target.disable();
                    source.sendMessage("已关闭 " + target.getName() + " 模块");
                } else if (args[0].equalsIgnoreCase("enable")) {
                    if (args.length == 1) {
                        source.sendMessage("参数不足");
                        return;
                    }
                    Model target = Model.getModel(args[1]);
                    if (target == null) {
                        source.sendMessage("模块不存在\n执行 #" + label + " list 查看所有列表");
                        return;
                    }
                    if (target instanceof CoreModel) {
                        source.sendMessage("不允许操作核心模块");
                        return;
                    }
                    target.enable();
                    source.sendMessage("已启用 " + target.getName() + " 模块");
                } else if (args[0].equalsIgnoreCase("list")) {
                    TreeSet<String> list = new TreeSet<>();
                    for (Model loadedModel : Model.LOADED_MODELS) {
                        list.add(loadedModel.getName());
                    }
                    source.sendMessage("所有模块列表: " + list);
                }
            } else {
                source.sendMessage("你没有权限");
            }
        }), "pl", "plugin");
    }

    @Override
    public String getName() {
        return "PluginManager";
        //
    }
}
