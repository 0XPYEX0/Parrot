package me.xpyex.plugin.parrot.utils;

import java.util.Collection;
import me.xpyex.plugin.parrot.api.TryCallable;

public class StringUtil {
    public static String getStrBetweenKeywords(String _string, String _key1, String _key2) {
        int firstKeyIndex = _key1.length() + _string.indexOf(_key1);
        return _string.substring(firstKeyIndex, _string.substring(firstKeyIndex).contains(_key2) ? firstKeyIndex + _string.substring(firstKeyIndex).indexOf(_key2) : _string.length());
    }

    public static boolean startsWithIgnoreCaseOr(String _string, String... _keys) {
        for (String s : _keys) {
            if (_string.toLowerCase().startsWith(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsIgnoreCaseOr(String _string, String... _key) {
        if (_string == null || _key == null) return false;
        for (String s : _key) {
            if (_string.toLowerCase().contains(s.toLowerCase())) return true;
        }
        return false;
    }

    public static boolean containsIgnoreCaseOr(String str, Collection<String> collection) {
        return containsIgnoreCaseOr(str, collection.toArray(new String[0]));
    }

    public static boolean equalsIgnoreCaseOr(String target, String... contents) {
        if (target == null || contents == null) return false;

        for (String s : contents) {
            if (s.equalsIgnoreCase(target))
                return true;
        }
        return false;
    }

    public static boolean equalsIgnoreCaseOr(TryCallable<String> target, String... contents) {
        try {
            return equalsIgnoreCaseOr(target.call(), contents);
        } catch (Throwable e) {
            return false;
        }
        //
    }

    public static boolean equalsIgnoreCase(String target, TryCallable<String> another) {
        return equalsIgnoreCaseOr(another, target);
    }
}
