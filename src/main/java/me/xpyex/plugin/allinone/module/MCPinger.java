package me.xpyex.plugin.allinone.module;

import me.xpyex.plugin.allinone.api.CommandMenu;
import me.xpyex.plugin.allinone.api.MessageBuilder;
import me.xpyex.plugin.allinone.core.module.Module;
import me.xpyex.plugin.allinone.modulecode.mcpinger.Pinger;
import me.xpyex.plugin.allinone.utils.MsgUtil;
import me.xpyex.plugin.allinone.utils.ValueUtil;
import net.mamoe.mirai.contact.Contact;

public class MCPinger extends Module {
    @Override
    public void register() {
        registerCommand(Contact.class, ((source, sender, label, args) -> {
            if (args.length == 0) {
                new CommandMenu(label)
                    .add("<IP>", "获取服务器信息")
                    .send(source);
                return;
            }
            if (args.length != 1) {
                MsgUtil.sendMsg(source, "您这服务器是不是有点多了");
                return;
            }
            try {
                String ip = args[0];
                String address;
                int port = 25565;
                if (ip.contains(":")) {
                    address = ip.split(":")[0];
                    port = Integer.parseInt(ip.split(":")[1]);
                } else {
                    address = ip;
                }
                Pinger pinger = new Pinger(address, port);
                pinger.setTimeout(10000);
                if (pinger.fetchData()) {
                    new MessageBuilder()
                        .plus("IP: " + pinger.getAddress() + ":" + pinger.getPort())
                        .plus("在线人数: " + ValueUtil.getOrDefault(pinger.getPlayersOnline(), 0) + "/" + ValueUtil.getOrDefault(pinger.getMaxPlayers(), 0))
                        .plus("MOTD: " + ValueUtil.getOrDefault(pinger.getMotd(), ""))
                        .plus("游戏版本: " + ValueUtil.getOrDefault(pinger.getGameVersion(), ""))
                        .plus("协议版本: " + ValueUtil.getOrDefault(pinger.getProtocolVersion(), ""))
                        .send(source);
                } else {
                    MsgUtil.sendMsg(source, "解析失败");
                }
            } catch (NumberFormatException ignored) {
                MsgUtil.sendMsg(source, "您这端口还带非整数的啊？");
            } catch (Exception e) {
                MsgUtil.sendMsg(source, "解析失败，出现异常: " + e);
                handleException(e, false);
            }
        }), "mc", "mcPing", "ping");
    }
}
