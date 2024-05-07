package me.xpyex.plugin.parrot.mirai.core.command.argument;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class StrParser extends ArgParser {
    @NotNull
    @Override
    public Optional<String> parse(String arg) {
        return Optional.ofNullable(arg);
        //
    }
}
