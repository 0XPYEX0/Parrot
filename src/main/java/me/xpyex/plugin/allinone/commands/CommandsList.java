package me.xpyex.plugin.allinone.commands;

import java.util.HashMap;

public class CommandsList {
    static HashMap<Class<?>, String> list = new HashMap<>();
    public static void register(Class<?> Class , String... cmds) {
        for (String cmd : cmds) {
            cmd = (cmd.startsWith("/") ? cmd : ("/" + cmd)).toLowerCase();
            list.put(Class, cmd);
        }
    }

    public static HashMap<Class<?>, String> getList() {
        return list;
    }

    public static boolean isCmd(String cmd) {
        return list.containsValue(cmd.toLowerCase());
    }

    public static boolean isCmd(Class<?> Class, String cmd) {
        return (list.containsKey(Class) && list.get(Class).equalsIgnoreCase(cmd));
    }
}
