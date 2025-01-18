package me.xpyex.plugin.parrot.api;

public interface TripleFunction<A, B, C, R> {
    R apply(A a, B b, C c);
}
