package me.xpyex.plugin.allinone.core.permission;

import java.util.ArrayList;

public interface Perms {
    ArrayList<String> getPermissions();
    ArrayList<String> getDenyPerms();
    void save();
}
