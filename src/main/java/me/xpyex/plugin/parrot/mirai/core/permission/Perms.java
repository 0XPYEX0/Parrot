package me.xpyex.plugin.parrot.mirai.core.permission;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.stream.Collectors;

public interface Perms {
    static TreeSet<String> getLowerCaseSet(TreeSet<String> set) {
        return set.stream().map(String::toLowerCase).collect(Collectors.toCollection(TreeSet::new));
    }

    TreeSet<String> getPermissions();

    TreeSet<String> getDenyPerms();

    void save();

    default boolean deniedPerm(String perm) {
        TreeSet<String> lowerCaseSet = getLowerCaseSet(getDenyPerms());
        if (lowerCaseSet.contains(perm)) {
            return true;
        }
        ArrayList<String> list = new ArrayList<>();
        for (String permNode : perm.toLowerCase().split("\\.")) {
            list.add(permNode);
            if (lowerCaseSet.contains(String.join(".", list) + ".*")) {
                return true;
            }
        }
        return false;
    }

    default boolean hasPerm(String perm) {
        TreeSet<String> lowerCaseSet = getLowerCaseSet(getPermissions());
        if (lowerCaseSet.contains(perm)) {
            return true;
        }
        ArrayList<String> list = new ArrayList<>();
        for (String permNode : perm.toLowerCase().split("\\.")) {
            list.add(permNode);
            if (lowerCaseSet.contains(String.join(".", list) + ".*")) {
                return true;
            }
        }
        return false;
    }
}
