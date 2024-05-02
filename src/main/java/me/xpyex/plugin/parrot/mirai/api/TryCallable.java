package me.xpyex.plugin.parrot.mirai.api;

public interface TryCallable<T> {
    T call() throws Throwable;
}
