package me.xpyex.plugin.parrot.mirai.modulecode.rcon;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

/**
 * Rcon包处理程序
 */
@RequiredArgsConstructor
public class RconPacketHandler extends SimpleChannelInboundHandler<RconPacket> {

    private final Rcon rcon;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RconPacket msg) {
        if (msg.getRequestId() == -1) {
            throw new IllegalStateException("登录失败");
        }
        if (msg.getType() == 2) {
            System.out.println("登录成功");
            rcon.logged = true;
            return;
        }

        // 如果等待响应队列中有
        Consumer<String> responseHandler = rcon.waitResponseQueue.remove(msg.getRequestId());
        if (responseHandler != null) {
            responseHandler.accept(msg.getMessage());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace(System.out);
        ctx.close();
    }
}
