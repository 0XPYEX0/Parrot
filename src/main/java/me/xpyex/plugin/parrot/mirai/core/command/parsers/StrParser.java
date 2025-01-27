package me.xpyex.plugin.parrot.mirai.core.command.parsers;

import java.util.Optional;
import me.xpyex.plugin.parrot.api.TryCallable;
import org.jetbrains.annotations.NotNull;

public class StrParser extends ArgParser {
    @NotNull
    @Override
    public Optional<String> parse(String arg) {
        return arg != null && !arg.isEmpty() ? Optional.of(arg) : Optional.empty();
        //
    }

    @NotNull
    @Override
    public Optional<String> parse(TryCallable<String> callable) {
        return parse(callable, String.class);
        //
    }
}
