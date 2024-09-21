package me.xpyex.plugin.parrot.mirai.module.rcon.api;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Rcon数据包帧处理程序
 */
public class RconBasedFrameHandler extends ByteToMessageCodec<RconPacket> {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    @Override
    protected void encode(ChannelHandlerContext ctx, RconPacket msg, ByteBuf out) {
        byte[] payload = msg.getMessage().getBytes(DEFAULT_CHARSET);
        out.writeIntLE(payload.length + 10); // 数据长度 + 十个 数据包格式长度
        out.writeIntLE(msg.getRequestId()); // 请求ID
        out.writeIntLE(msg.getType()); // 数据包类型
        out.writeBytes(payload); // 数据
        out.writeBytes(new byte[2]); // 结尾固定两个byte
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 数据长度不足四位 无法取出第一个int 则跳过
        if (in.readableBytes() < 4) {
            return;
        }

        // 缓存内无法读出完整数据包 则跳过
        int packetLength = in.readIntLE();
        if (in.readableBytes() < packetLength) {
            in.readerIndex(in.readerIndex() - 4); // 重置索引
            return;
        }

        // 读出完整数据 封装成包
        ByteBuf packetBuf = in.readBytes(packetLength);
        int requestId = packetBuf.readIntLE();
        int packetType = packetBuf.readIntLE();
        CharSequence payload = packetBuf.readCharSequence(packetLength - 10, StandardCharsets.UTF_8);
        if (payload.length() > 0) {
            payload = payload.subSequence(0, payload.length() - 1); // 如果读取到的消息长度不为0 则移除结尾的换行符
        }
        out.add(RconPacket.of(requestId, packetType, String.valueOf(payload)));
    }
}
