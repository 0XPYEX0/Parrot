package me.xpyex.plugin.allinone.api;

import java.util.HashMap;

/**
 * 命令帮助菜单，替开发者完成帮助菜单的任务
 */
public class CommandMenu {
    private final String command;
    private final HashMap<String, String> helpList = new HashMap<>();

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
        helpList.put(argument, help);
        return this;
    }

    /**
     * 拼接整个帮助菜单
     * @return 返回帮助菜单
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (String argument : helpList.keySet()) {
            result.append("#").append(command).append(" ").append(argument).append(" - ").append(helpList.get(argument)).append("\n");
        }
        return result.substring(0, result.length() - 1);
    }
}
