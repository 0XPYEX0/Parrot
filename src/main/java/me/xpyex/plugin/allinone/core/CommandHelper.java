package me.xpyex.plugin.allinone.core;

import java.util.HashMap;

public class CommandHelper {
    private final String command;
    private final HashMap<String, String> helpList = new HashMap<>();

    public CommandHelper(String command) {
        this.command = command;
        //
    }

    public CommandHelper add(String argument, String help) {
        helpList.put(argument, help);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (String argument : helpList.keySet()) {
            result.append("#").append(command).append(" ").append(argument).append(" - ").append(helpList.get(argument)).append("\n");
        }
        return result.substring(0, result.length() - 1);
    }
}
