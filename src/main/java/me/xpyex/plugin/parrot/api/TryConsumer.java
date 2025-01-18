package me.xpyex.plugin.parrot.api;

public interface TryConsumer<T> {
    void accept(T object) throws Throwable;
}
