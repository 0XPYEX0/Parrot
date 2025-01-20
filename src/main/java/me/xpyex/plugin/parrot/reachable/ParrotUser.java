package me.xpyex.plugin.parrot.reachable;

public abstract class ParrotUser<T> extends ParrotReachable<T> {
    public ParrotUser(T handle, long id) {
        super(handle, id);
    }
}
