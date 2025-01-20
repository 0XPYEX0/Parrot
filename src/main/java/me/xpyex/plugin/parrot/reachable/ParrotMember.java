package me.xpyex.plugin.parrot.reachable;

public abstract class ParrotMember<T> extends ParrotUser<T> {
    public ParrotMember(T handle, long id) {
        super(handle, id);
    }

    public static class MemberPerm {
        public static final int NORMAL = 0;
        public static final int ADMIN = 1;
        public static final int OWNER = 2;
    }
}
