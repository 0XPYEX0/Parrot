package me.xpyex.plugin.parrot.reachable;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class ParrotReachable<T> {
    private T handle;
    private long id;
    private final long createdTime = System.currentTimeMillis();

    public abstract void sendMessage(String message);
    public abstract boolean hasPerm(String s);
    public abstract String getName();
}
