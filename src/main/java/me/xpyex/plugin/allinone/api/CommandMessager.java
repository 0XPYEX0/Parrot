package me.xpyex.plugin.allinone.api;

import java.util.ArrayList;

public class CommandMessager {
    private final ArrayList<String> messages = new ArrayList<>();

    public CommandMessager() {}

    public CommandMessager(String message) {
        messages.add(message);
        //
    }

    public CommandMessager plus(String message) {
        messages.add(message);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (String argument : messages) {
            result.append(argument).append("\n");
        }
        return result.substring(0, result.length() - 1);
    }
}
