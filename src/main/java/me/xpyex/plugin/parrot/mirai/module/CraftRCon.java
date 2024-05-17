package me.xpyex.plugin.parrot.mirai.module;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.graversen.minecraft.rcon.commands.SayCommand;
import io.graversen.minecraft.rcon.service.ConnectOptions;
import io.graversen.minecraft.rcon.service.MinecraftRconService;
import io.graversen.minecraft.rcon.service.RconDetails;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import me.xpyex.plugin.parrot.mirai.api.CommandMenu;
import me.xpyex.plugin.parrot.mirai.api.MessageBuilder;
import me.xpyex.plugin.parrot.mirai.core.command.CommandBus;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.BotOfflineEvent;

public class CraftRCon extends Module {
    private static final HashMap<String, MinecraftRconService> CACHE = new HashMap<>();

    private Optional<MinecraftRconService> getService(String name) throws IOException {
        MinecraftRconService service;
        if (!CACHE.containsKey(name)) {
            File file = new File(getDataFolder(), name + ".json");
            if (!file.exists()) {
                info("RCon尚未记录，请先添加");
                return Optional.empty();
            }
            JSONObject content = JSONUtil.parseObj(Files.readString(file.toPath()));
            RconDetails info = new RconDetails(content.getStr("hostname"), content.getInt("port"), content.getStr("password"));
            CACHE.put(name, new MinecraftRconService(
                info,
                ConnectOptions.defaults()
            ));
        }
        service = CACHE.get(name);
        return Optional.ofNullable(service);
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
                    .add("chat <ServerName> <Msg...>", "向RCon对话")
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
                RconDetails info = new RconDetails(args[2], Integer.parseInt(args[3]), args[4]);
                File outFile = new File(getDataFolder(), args[1] + ".json");
                Files.write(outFile.toPath(), JSONUtil.toJsonPrettyStr(info).getBytes());
                source.sendMessage("已添加RCon <" + args[1] + ">: " + info.getHostname() + ":" + info.getPort());
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
                getService(args[1]).ifPresent(service -> {
                    if (!service.isConnected()) {
                        service.connect();
                    }
                    service.minecraftRcon().ifPresent(rcon -> {
                        String cmd = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                        new MessageBuilder()
                            .plus("向 " + args[1] + " 发送命令 /" + cmd)
                            .plus("")
                            .plus("返回: ")
                            .plus(codeStr(rcon.sendSync(() -> cmd).getResponseString(), Charset.forName("GBK"), StandardCharsets.UTF_8))
                            .send(source);
                    });
                });
            } else if ("chat".equalsIgnoreCase(args[0])) {
                if (args.length < 3) {
                    source.sendMessage("参数不足");
                    CommandBus.dispatchCommand(source, sender, label);
                    return;
                }
                if (!sender.hasPerm(getName() + ".chat." + args[1])) {
                    source.sendMessage("你机霸谁？不听你的");
                    return;
                }
                getService(args[1]).ifPresent(service -> {
                    if (!service.isConnected()) {
                        service.connect();
                    }
                    service.minecraftRcon().ifPresent(rcon -> {
                        String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                        new MessageBuilder()
                            .plus("向 " + args[1] + " 发送语句 " + text)
                            .plus("")
                            .plus("返回: ")
                            .plus(codeStr(rcon.sendSync(new SayCommand(text)).getResponseString(), StandardCharsets.UTF_8, Charset.forName("GBK")))
                            .send(source);
                    });
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
                    CACHE.get(args[1]).disconnect();
                }
                CACHE.remove(args[1]);
                new File(getDataFolder(), args[1] + ".json").delete();
                source.sendMessage("已删除RCon " + args[1]);
            }
        }, "RCon");
        listenEvent(BotOfflineEvent.class, event -> {
            CACHE.forEach((name, service) -> {
                service.disconnect();
            });
        });
    }
}
