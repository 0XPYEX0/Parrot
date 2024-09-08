package me.xpyex.plugin.parrot.mirai.api;

import java.util.ArrayList;
import me.xpyex.plugin.parrot.mirai.core.mirai.ParrotContact;
import me.xpyex.plugin.parrot.mirai.utils.MsgUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.PlainText;

/**
 * 消息列表
 * 帮助开发者快速地发送换行消息
 */
public class MessageBuilder {
    private final ArrayList<String> messages = new ArrayList<>();

    public MessageBuilder() {
    }

    public MessageBuilder(String message) {
        messages.add(message);
        //
    }

    /**
     * 新增一行
     *
     * @param message 参数
     * @return 返回自身，制造链式
     */
    public MessageBuilder plus(String message) {
        messages.add(message);
        return this;
    }

    public MessageBuilder plus(boolean condition, String message) {
        if (condition) messages.add(message);
        return this;
    }

    /**
     * 获取最终结果
     *
     * @return 字符串结果
     */
    @Override
    public String toString() {
        return String.join("\n", messages);
        //
    }

    public PlainText toMessage() {
        return new PlainText(toString());
        //
    }

    /**
     * 直接发送给目标
     *
     * @param target 接收信息的目标
     */
    public void send(Contact target) {
        MsgUtil.sendMsg(target, this.toString());
        //
    }

    public void send(ParrotContact<?> target) {
        send(target.getContact());
        //
    }

    /**
     * 通过事件直接发送给消息来源
     *
     * @param event 目标事件
     */
    public void send(MessageEvent event) {
        send(MsgUtil.getRealSender(event));
        //
    }
}
