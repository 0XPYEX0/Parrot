package me.xpyex.plugin.allinone.module.core;

import java.util.Arrays;
import java.util.TreeSet;
import me.xpyex.plugin.allinone.api.CommandMenu;
import me.xpyex.plugin.allinone.api.MessageBuilder;
import me.xpyex.plugin.allinone.core.command.CommandBus;
import me.xpyex.plugin.allinone.core.module.CoreModule;
import me.xpyex.plugin.allinone.core.event.EventBus;
import me.xpyex.plugin.allinone.core.module.Module;
import me.xpyex.plugin.allinone.utils.MsgUtil;
import net.mamoe.mirai.contact.Contact;

@SuppressWarnings("unused")
public class PluginManager extends CoreModule {
    @Override
    public void register() {
        registerCommand(Contact.class, ((source, sender, label, args) -> {
            if (sender.hasPerm(getName() + ".use")) {
                if (args.length == 0) {
                    new CommandMenu(label)
                        .add("enable <模块>", "启用该模块")
                        .add("disable <模块>", "禁用该模块")
                        .add("list", "查询所有模块")
                        .send(source);
                } else if (args[0].equalsIgnoreCase("disable")) {
                    if (args.length == 1) {
                        MsgUtil.sendMsg(source, "参数不足");
                        return;
                    }
                    Module target = Module.getModule(args[1]);
                    if (target == null) {
                        MsgUtil.sendMsg(source, "模块不存在\n执行 #" + label + " list 查看所有列表");
                        return;
                    }
                    if (target.isCore()) {
                        MsgUtil.sendMsg(source, "不允许操作核心模块");
                        return;
                    }
                    if (target.disable()) {
                        MsgUtil.sendMsg(source, "已禁用 " + target.getName() + " 模块");
                    } else {
                        MsgUtil.sendMsg(source, "模块 " + target.getName() + " 已被禁用，无需重复禁用");
                    }
                } else if (args[0].equalsIgnoreCase("enable")) {
                    if (args.length == 1) {
                        MsgUtil.sendMsg(source, "参数不足");
                        return;
                    }
                    Module target = Module.getModule(args[1]);
                    if (target == null) {
                        MsgUtil.sendMsg(source, "模块不存在\n执行 #" + label + " list 查看所有列表");
                        return;
                    }
                    if (target.isCore()) {
                        MsgUtil.sendMsg(source, "不允许操作核心模块");
                        return;
                    }
                    if (target.enable()) {
                        MsgUtil.sendMsg(source, "已启用 " + target.getName() + " 模块");
                    } else {
                        MsgUtil.sendMsg(source, "模块 " + target.getName() + " 已被启用，无需重复启用");
                    }
                } else if (args[0].equalsIgnoreCase("list")) {
                    TreeSet<String> list = new TreeSet<>();
                    for (Module loadedModule : Module.LOADED_MODELS.values()) {
                        list.add(loadedModule.getName() + (loadedModule.isDisabled() ? "(未启用)" : ""));
                    }
                    MsgUtil.sendMsg(source, "所有模块列表: " + list);
                } else if (args[0].equalsIgnoreCase("info")) {
                    if (args.length == 1) {
                        MsgUtil.sendMsg(source, "参数不足");
                        return;
                    }
                    Module target = Module.getModule(args[1]);
                    if (target == null) {
                        MsgUtil.sendMsg(source, "模块不存在\n执行 #" + label + " list 查看所有列表");
                        return;
                    }
                    new MessageBuilder("模块 " + target.getName())
                        .plus("已注册的命令: " + Arrays.toString(CommandBus.getCommands(target)))
                        .plus("监听的事件: " + Arrays.toString(EventBus.getEvents(target)))
                        .send(source);
                } else {
                    MsgUtil.sendMsg(source, "未知子命令");
                }
            } else {
                MsgUtil.sendMsg(source, "你没有权限");
            }
        }), "pl", "plugin", "module");
    }
}
