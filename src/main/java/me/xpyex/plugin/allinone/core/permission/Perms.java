package me.xpyex.plugin.allinone.core.permission;

import java.util.ArrayList;

public interface Perms {
    ArrayList<String> getPermissions();

    ArrayList<String> getDenyPerms();

    void save();

    public static ArrayList<String> getLowerCaseList(ArrayList<String> list) {
        ArrayList<String> newOne = new ArrayList<>();
        for (String s : list) {
            newOne.add(s.toLowerCase());
        }
        return newOne;
    }
}
