package me.xpyex.plugin.parrot.mirai.module.rcon.api;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import lombok.RequiredArgsConstructor;

/**
 * Rcon通道初始化
 */
@RequiredArgsConstructor
public class RconChannelInitializer extends ChannelInitializer<Channel> {

    private final Rcon rcon;

    @Override
    protected void initChannel(Channel ch) {
        ch.pipeline()
            .addLast(new RconBasedFrameHandler()) // 添加Rcon帧处理器
            .addLast(new RconPacketHandler(rcon)); // 添加Rcon包处理器
    }
}
