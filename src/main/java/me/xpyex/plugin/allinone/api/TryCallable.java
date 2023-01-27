package me.xpyex.plugin.allinone.api;

public interface TryCallable<T> {
    public T call() throws Throwable;
}
