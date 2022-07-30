package me.xpyex.plugin.allinone.utils;

public class StringUtil {
    public static String getStrBetweenKeywords(String _string, String _key1, String _key2) {
        int firstKeyIndex = _key1.length() + _string.indexOf(_key1);
        return _string.substring(firstKeyIndex, _string.substring(firstKeyIndex).contains(_key2) ? firstKeyIndex + _string.substring(firstKeyIndex).indexOf(_key2) : _string.length());
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
