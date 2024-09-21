package me.xpyex.plugin.parrot.mirai.module;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import me.xpyex.plugin.parrot.mirai.api.CommandMenu;
import me.xpyex.plugin.parrot.mirai.api.MessageBuilder;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.utils.ExceptionUtil;
import me.xpyex.plugin.parrot.mirai.utils.ValueUtil;
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
                source.sendMessage("您这服务器是不是有点多了");
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
                    source.sendMessage("解析失败");
                }
            } catch (NumberFormatException ignored) {
                source.sendMessage("您这端口还带非整数的啊？");
            } catch (Exception e) {
                source.sendMessage("解析失败，出现异常: " + e);
                handleException(e, false, null);
            }
        }), "mc", "mcPing", "ping");
    }

    public static class Pinger {
        private String address;
        private int port;
        private int timeout;
        private int pingVersion;
        private int protocolVersion;
        private String gameVersion;
        private String motd;
        private int playersOnline;
        private int maxPlayers;

        public Pinger(String address, int port) {
            this.address = "localhost";
            this.port = 25565;
            this.timeout = 2000;
            this.pingVersion = -1;
            this.protocolVersion = -1;
            this.playersOnline = -1;
            this.maxPlayers = -1;
            this.setAddress(address);
            this.setPort(port);
        }

        public String getAddress() {
            return this.address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getPort() {
            return this.port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getTimeout() {
            return this.timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public int getPingVersion() {
            return this.pingVersion;
        }

        private void setPingVersion(int pingVersion) {
            this.pingVersion = pingVersion;
        }

        public int getProtocolVersion() {
            return this.protocolVersion;
        }

        private void setProtocolVersion(int protocolVersion) {
            this.protocolVersion = protocolVersion;
        }

        public String getGameVersion() {
            return this.gameVersion;
        }

        private void setGameVersion(String gameVersion) {
            this.gameVersion = gameVersion;
        }

        public String getMotd() {
            return this.motd;
        }

        private void setMotd(String motd) {
            this.motd = motd;
        }

        public int getPlayersOnline() {
            return this.playersOnline;
        }

        private void setPlayersOnline(int playersOnline) {
            this.playersOnline = playersOnline;
        }

        public int getMaxPlayers() {
            return this.maxPlayers;
        }

        private void setMaxPlayers(int maxPlayers) {
            this.maxPlayers = maxPlayers;
        }

        public boolean fetchData() {
            try {
                Socket socket = new Socket();
                socket.setSoTimeout(this.timeout);
                socket.connect(new InetSocketAddress(this.getAddress(), this.getPort()), this.getTimeout());
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                InputStream inputStream = socket.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_16BE);
                dataOutputStream.write(new byte[]{-2, 1});
                int packetId = inputStream.read();
                if (packetId == -1) {
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        ExceptionUtil.handleException(ex, true, null, null);
                    }
                    socket = null;
                    return false;
                }
                if (packetId != 255) {
                    try {
                        socket.close();
                    } catch (IOException ex2) {
                        ExceptionUtil.handleException(ex2, true, null, null);
                    }
                    socket = null;
                    return false;
                }
                int length = inputStreamReader.read();
                if (length == -1) {
                    try {
                        socket.close();
                    } catch (IOException ex3) {
                        ExceptionUtil.handleException(ex3, true, null, null);
                    }
                    socket = null;
                    return false;
                }
                if (length == 0) {
                    try {
                        socket.close();
                    } catch (IOException ex4) {
                        ExceptionUtil.handleException(ex4, true, null, null);
                    }
                    socket = null;
                    return false;
                }
                char[] chars = new char[length];
                if (inputStreamReader.read(chars, 0, length) != length) {
                    try {
                        socket.close();
                    } catch (IOException ex5) {
                        ExceptionUtil.handleException(ex5, true, null, null);
                    }
                    return false;
                }
                String string = new String(chars);
                if (string.startsWith("§")) {
                    final String[] data = string.split("\u0000");
                    this.setPingVersion(Integer.parseInt(data[0].substring(1)));
                    this.setProtocolVersion(Integer.parseInt(data[1]));
                    this.setGameVersion(data[2]);
                    this.setMotd(data[3]);
                    this.setPlayersOnline(Integer.parseInt(data[4]));
                    this.setMaxPlayers(Integer.parseInt(data[5]));
                } else {
                    final String[] data = string.split("§");
                    this.setMotd(data[0]);
                    this.setPlayersOnline(Integer.parseInt(data[1]));
                    this.setMaxPlayers(Integer.parseInt(data[2]));
                }
                dataOutputStream.close();
                outputStream.close();
                inputStreamReader.close();
                inputStream.close();
                socket.close();
            } catch (IOException exception) {
                ExceptionUtil.handleException(exception, true, null, null);
                return false;
            }
            return true;
        }
    }
}
