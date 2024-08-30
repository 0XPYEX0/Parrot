package me.xpyex.plugin.parrot.mirai.core.permission;

import java.util.TreeSet;
import java.util.stream.Collectors;

public interface Perms {
    static TreeSet<String> getLowerCaseSet(TreeSet<String> set) {
        return set.stream().map(String::toLowerCase).collect(Collectors.toCollection(TreeSet::new));
    }

    TreeSet<String> getPermissions();

    TreeSet<String> getDenyPerms();

    void save();
}
