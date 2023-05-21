package me.xpyex.plugin.allinone.api;

public interface TryCallable<T> {
    T call() throws Throwable;
}
