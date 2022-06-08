package me.xpyex.plugin.allinone.commands;

import java.util.HashMap;

public class CommandsList {
    private static final HashMap<Class<?>, String> COMMAND_LIST = new HashMap<>();

    public static void register(Class<?> Class , String... cmds) {
        for (String cmd : cmds) {
            cmd = (cmd.startsWith("/") ? cmd : ("/" + cmd)).toLowerCase();
            COMMAND_LIST.put(Class, cmd);
        }
    }

    public static HashMap<Class<?>, String> getCommandList() {
        return COMMAND_LIST;
        //
    }

    public static boolean isCmd(String cmd) {
        return COMMAND_LIST.containsValue(cmd.toLowerCase());
        //
    }

    public static boolean isCmd(Class<?> Class, String cmd) {
        return (COMMAND_LIST.containsKey(Class) && COMMAND_LIST.get(Class).equalsIgnoreCase(cmd));
    }
}
