package me.xpyex.plugin.parrot.mirai.api;

public interface TryConsumer<T> {
    void accept(T object) throws Throwable;
}
