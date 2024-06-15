package me.xpyex.plugin.parrot.mirai.modulecode.rcon;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Rcon {
    private final String host;
    private final int port;
    private final EventLoopGroup group;
    boolean logged;
    Map<Integer, Consumer<String>> waitResponseQueue = new ConcurrentHashMap<>();
    private Channel channel;
    private AtomicInteger request = new AtomicInteger(0); // 这里使用线程安全的Int. netty的EventLoopGroup是线程池

    public Rcon(String host, int port) {
        this.host = host;
        this.port = port;
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new RconChannelInitializer(this));
        try {
            ChannelFuture connect = bootstrap.connect(host, port).sync();
            channel = connect.channel();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public void login(String password) {
        channel.writeAndFlush(new RconPacket.Login(request.getAndIncrement(), password));
    }

    public void send(String command) {
        sendPacket(new RconPacket.Command(request.getAndIncrement(), command));
    }

    public void send(String command, Consumer<String> responseHandler) {
        int requestId = request.getAndIncrement();
        waitResponseQueue.put(requestId, responseHandler);
        sendPacket(new RconPacket.Command(requestId, command));
    }

    private void sendPacket(RconPacket packet) {
        if (logged) {
            channel.writeAndFlush(packet);
        }
    }

    public void close() {
        group.shutdownGracefully();
    }
}
