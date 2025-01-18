package me.xpyex.plugin.parrot.api;

public interface TryCallable<T> {
    T call() throws Throwable;
}
