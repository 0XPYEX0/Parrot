package me.xpyex.plugin.parrot.mirai.core.command.argument;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import me.xpyex.plugin.parrot.mirai.api.TryCallable;
import net.mamoe.mirai.contact.Contact;
import org.jetbrains.annotations.NotNull;

public abstract class ArgParser {
    protected static final HashMap<Class<? extends ArgParser>, ArgParser> PARSERS = new HashMap<>();
    private static final AtomicReference<Contact> parseObj = new AtomicReference<>();

    protected ArgParser() {
        PARSERS.put(this.getClass(), this);
        //
    }

    @SuppressWarnings("unchecked")
    public static <T extends ArgParser> T of(Class<T> clazz) {
        try {
            if (PARSERS.containsKey(clazz)) return (T) PARSERS.get(clazz);
            return clazz.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException("无法构建Parser实例", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getParseObj() {
        return (T) parseObj.get();
        //
    }

    public static void setParseObj(Contact parseObj) {
        ArgParser.parseObj.set(parseObj);
        //
    }

    @NotNull
    public abstract Optional<?> parse(String arg);

    @NotNull
    public Optional<?> parse(TryCallable<String> callable) {
        try {
            return parse(callable.call());
        } catch (Throwable e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <T> Optional<T> parse(TryCallable<String> callable, Class<T> clazz) {
        try {
            return (Optional<T>) parse(callable);
        } catch (ClassCastException ignored) {
            return Optional.empty();
        }
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <T> Optional<T> parse(String arg, Class<T> clazz) {
        try {
            return (Optional<T>) parse(arg);
        } catch (ClassCastException ignored) {
            return Optional.empty();
        }
    }
}
