package me.xpyex.plugin.allinone.api;

public interface TryConsumer<T> {
    void accept(T object) throws Throwable;
}
