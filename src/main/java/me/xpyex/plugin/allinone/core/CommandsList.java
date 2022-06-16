package me.xpyex.plugin.allinone.core;

import java.util.HashMap;

public class CommandsList {
    private static final HashMap<String, Model> COMMAND_LIST = new HashMap<>();

    public static void register(Model model, String... commands) {
        for (String cmd : commands) {
            cmd = (cmd.startsWith("#") ? cmd : ("#" + cmd)).toLowerCase();
            COMMAND_LIST.put(cmd, model);
        }
    }

    public static boolean isCmd(String cmd) {
        return COMMAND_LIST.containsKey((cmd.startsWith("#") ? cmd : ("#" + cmd)).toLowerCase());
        //
    }

    public static boolean isCmd(Model model, String cmd) {
        cmd = (cmd.startsWith("#") ? cmd : ("#" + cmd)).toLowerCase();
        return (COMMAND_LIST.containsKey(cmd) && COMMAND_LIST.get(cmd).equals(model));
    }
}
