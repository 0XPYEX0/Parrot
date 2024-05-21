package me.xpyex.plugin.parrot.mirai.module;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;
import java.util.WeakHashMap;
import me.xpyex.plugin.parrot.mirai.api.CommandMenu;
import me.xpyex.plugin.parrot.mirai.api.MessageBuilder;
import me.xpyex.plugin.parrot.mirai.core.command.CommandBus;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.modulecode.rcon.Rcon;
import me.xpyex.plugin.parrot.mirai.utils.FileUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.BotOfflineEvent;

public class CraftRCon extends Module {
    private static final WeakHashMap<String, Rcon> CACHE = new WeakHashMap<>();

    private Optional<Rcon> getService(String name) throws IOException {
        if (!CACHE.containsKey(name)) {
            File file = new File(getDataFolder(), name + ".json");
            if (!file.exists()) {
                return Optional.empty();
            }
            JSONObject content = JSONUtil.parseObj(Files.readString(file.toPath()));
            Rcon rcon = new Rcon(content.getStr("hostname"), content.getInt("port"));
            rcon.login(content.getStr("password"));
            CACHE.put(name, rcon);
        }
        return Optional.ofNullable(CACHE.get(name));
    }

    private String codeStr(String str, Charset from, Charset to) {
        return new String(str.getBytes(from), to);
    }

    @Override
    public void register() throws Throwable {
        registerCommand(Contact.class, (source, sender, label, args) -> {
            //#RCon send ServerName Command...
            if (args.length == 0) {
                new CommandMenu(label).add("add <ServerName> <Host> <Port> <Password>", "添加一个RCon. 请在私聊进行，以面暴露password")
                    .add("send <ServerName> <Cmd...>", "发送一个命令到RCon")
                    .add("remove <ServerName>", "移除一个RCon")
                    .send(source);
                return;
            }
            if ("add".equalsIgnoreCase(args[0])) {
                if (!sender.hasPerm(getName() + ".add")) {
                    source.sendMessage("你机霸谁？不听你的");
                    return;
                }
                if (args.length < 5) {
                    source.sendMessage("参数不足");
                    CommandBus.dispatchCommand(source, sender, label);
                    return;
                }
                File outFile = new File(getDataFolder(), args[1] + ".json");
                FileUtil.writeFile(outFile, new JSONObject()
                                                .set("host", args[2])
                                                .set("port", Integer.parseInt(args[3]))
                                                .set("password", args[4])
                                                .toStringPretty()
                );
                source.sendMessage("已添加RCon <" + args[1] + ">: " + args[2] + ":" + args[3]);
            } else if ("send".equalsIgnoreCase(args[0])) {
                if (args.length < 3) {
                    source.sendMessage("参数不足");
                    CommandBus.dispatchCommand(source, sender, label);
                    return;
                }
                if (!sender.hasPerm(getName() + ".sendCmd." + args[1])) {
                    source.sendMessage("你机霸谁？不听你的");
                    return;
                }
                getService(args[1]).ifPresentOrElse(rcon -> {
                    String cmd = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    source.sendMessage("向 " + args[1] + " 发送命令 /" + cmd);rcon.send(cmd, s -> {
                        new MessageBuilder()
                            .plus("返回: ")
                            .plus(s)
                            .send(source);
                    });
                }, () -> {
                    source.sendMessage("RCon尚未记录，请先添加");
                });
            } else if ("remove".equalsIgnoreCase(args[0])) {
                if (!sender.hasPerm(getName() + ".remove")) {
                    source.sendMessage("你机霸谁？不听你的");
                    return;
                }
                if (args.length < 2) {
                    source.sendMessage("参数不足");
                    CommandBus.dispatchCommand(source, sender, label);
                    return;
                }
                if (CACHE.containsKey(args[1])) {
                    CACHE.get(args[1]).close();
                }
                CACHE.remove(args[1]);
                new File(getDataFolder(), args[1] + ".json").delete();
                source.sendMessage("已删除RCon " + args[1]);
            }
        }, "RCon");
        listenEvent(BotOfflineEvent.class, event -> {
            CACHE.forEach((name, service) -> {
                service.close();
            });
        });
    }
}
