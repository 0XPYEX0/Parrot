package me.xpyex.plugin.allinone.api;

public interface TryConsumer<T> {
    public void accept(T object) throws Throwable;
}
