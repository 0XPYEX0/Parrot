package me.xpyex.plugin.allinone.utils;

public class StringUtil {
    public static String getStrBetweenKeywords(String _string, String _key1, String _key2) {
        int firstKeyIndex = _key1.length() + _string.indexOf(_key1);
        return _string.substring(firstKeyIndex).split(_key2)[0];
    }

    public static boolean startsWithIgnoreCase(String _string, String... _keys) {
        for (String s : _keys) {
            if (_string.toLowerCase().startsWith(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsIgnoreCase(String _string, String _key) {
        return _string.toLowerCase().contains(_key.toLowerCase());
        //
    }
}
