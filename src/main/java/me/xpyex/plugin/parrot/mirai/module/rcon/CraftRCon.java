package me.xpyex.plugin.parrot.mirai.module.rcon;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;
import java.util.WeakHashMap;
import me.xpyex.plugin.parrot.api.CommandMenu;
import me.xpyex.plugin.parrot.api.MessageBuilder;
import me.xpyex.plugin.parrot.mirai.core.command.CommandNode;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.module.rcon.api.Rcon;
import me.xpyex.plugin.parrot.mirai.utils.FileUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.BotOfflineEvent;

public class CraftRCon extends Module {
    private static final WeakHashMap<String, Rcon> CACHE = new WeakHashMap<>();

    private Optional<Rcon> getService(String name) {
        if (!CACHE.containsKey(name)) {
            File file = new File(getDataFolder(), name + ".json");
            if (!file.exists()) {
                return Optional.empty();
            }
            try {
                JSONObject content = JSONUtil.parseObj(Files.readString(file.toPath()));
                Rcon rcon = new Rcon(content.getStr("hostname"), content.getInt("port"));
                rcon.login(content.getStr("password"));
                CACHE.put(name, rcon);
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        return Optional.ofNullable(CACHE.get(name));
    }

    @Override
    public void register() throws Throwable {
        registerCommand(Contact.class,
            CommandNode.of((source, sender, arguments) -> {
                    new CommandMenu(arguments)
                        .add("add <ServerName> <Host> <Port> <Password>", "添加一个RCon. 请在私聊进行，以免暴露password")
                        .add("send <ServerName> <Cmd...>", "发送一个命令到RCon")
                        .add("remove <ServerName>", "移除一个RCon")
                        .send(source);
                })
                .child(CommandNode.of((source, sender, arguments) -> {
                    if (arguments.hasEnoughArg(4)) {
                        source.sendMessage("参数不足");
                        return;
                    }
                    File outFile = new File(getDataFolder(), arguments.getArgument(0) + ".json");
                    FileUtil.writeFile(outFile, new JSONObject()
                                                    .set("host", arguments.getArgument(1))
                                                    .set("port", arguments.getIntArg(2))
                                                    .set("password", arguments.getArgument(3))
                                                    .toStringPretty()
                    );
                    source.sendMessage("已添加RCon <" + arguments.getArgument(0) + ">: " + arguments.getArgument(1) + ":" + arguments.getArgument(2));
                }).permission(getName() + ".add", "你机霸谁？不听你的"), "add")
                .child(CommandNode.of((source, sender, arguments) -> {
                    getService(arguments.getArgument(0)).ifPresentOrElse(rcon -> {
                        String cmd = String.join(" ", Arrays.copyOfRange(arguments.getArguments(), 1, arguments.getArguments().length));
                        source.sendMessage("向 " + arguments.getArgument(0) + " 发送命令 /" + cmd);
                        rcon.send(cmd, s -> {
                            new MessageBuilder()
                                .plus("返回: ")
                                .plus(s)
                                .send(source);
                        });
                    }, () -> {
                        source.sendMessage("连接失败，或是RCon尚未记录");
                    });
                }).executableCheckWithArg((source, sender, arguments) -> {
                    if (arguments.hasEnoughArg(2)) {
                        source.sendMessage("参数不足");
                        return false;
                    }
                    if (!sender.hasPerm(getName() + ".sendCmd." + arguments.getArgument(0))) {
                        source.sendMessage("你机霸谁？不听你的");
                        return false;
                    }
                    return true;
                }), "send")
                .child(CommandNode.of((source, sender, arguments) -> {
                    if (CACHE.containsKey(arguments.getArgument(0))) {
                        CACHE.get(arguments.getArgument(0)).close();
                    }
                    CACHE.remove(arguments.getArgument(0));
                    new File(getDataFolder(), arguments.getArgument(0) + ".json").delete();
                    source.sendMessage("已删除RCon " + arguments.getArgument(0));
                }).executableCheckWithArg((source, sender, arguments) -> {
                    if (!sender.hasPerm(getName() + ".remove")) {
                        source.sendMessage("你机霸谁？不听你的");
                        return false;
                    }
                    if (!arguments.hasMoreArg()) {
                        source.sendMessage("参数不足");
                        return false;
                    }
                    return true;
                }), "remove")
            , "RCon");
        listenEvent(BotOfflineEvent.class, event -> {
            CACHE.forEach((name, service) -> {
                service.close();
            });
        });
    }
}