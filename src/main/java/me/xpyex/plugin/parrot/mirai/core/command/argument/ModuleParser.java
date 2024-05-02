package me.xpyex.plugin.parrot.mirai.core.command.argument;

import java.util.Optional;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import org.jetbrains.annotations.NotNull;

public class ModuleParser extends ArgParser {
    @NotNull
    @Override
    public Optional<Module> parse(String arg) {
        return Optional.ofNullable(Module.getModule(arg));
        //
    }
}
