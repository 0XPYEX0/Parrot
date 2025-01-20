package me.xpyex.plugin.parrot.reachable;

public abstract class ParrotGroup<T> extends ParrotReachable<T> {
    public ParrotGroup(T handle, long id) {
        super(handle, id);
    }
}
