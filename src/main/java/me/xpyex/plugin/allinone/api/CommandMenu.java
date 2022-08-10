package me.xpyex.plugin.allinone.api;

import cn.hutool.core.lang.Pair;
import java.util.ArrayList;
import net.mamoe.mirai.contact.Contact;

/**
 * 命令帮助菜单，替开发者完成帮助菜单的任务
 */
public class CommandMenu {
    private final String command;
    private final ArrayList<Pair<String, String>> helpList = new ArrayList<>();

    /**
     * 构造函数
     * @param command 帮助的主命令
     */
    public CommandMenu(String command) {
        this.command = command;
        //
    }

    /**
     * 添加一行帮助
     * @param argument 参数
     * @param help 对应参数的帮助
     * @return 返回自身，制造链式调用
     */
    public CommandMenu add(String argument, String help) {
        helpList.add(new Pair<>(argument, help));
        return this;
    }

    /**
     * 拼接整个帮助菜单
     * @return 返回帮助菜单
     */
    @Override
    public String toString() {
        CommandMessager messager = new CommandMessager();
        for (Pair<String, String> pair : helpList) {
            messager.plus("#" + command + " " + pair.getKey() + " - " + pair.getValue());
        }
        return messager.toString();
    }

    public void send(Contact target) {
        target.sendMessage(this.toString());
        //
    }
}
