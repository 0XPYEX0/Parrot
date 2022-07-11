package me.xpyex.plugin.allinone.utils;

public class StringUtil {
    public static String getStrBetweenChars(String _string, String _char1, String _char2) {
        int keyIndex = _char1.length() + _string.indexOf(_char1);
        return _string.substring(keyIndex, _string.substring(keyIndex).contains(_char2) ? keyIndex + _string.substring(keyIndex).indexOf(_char2) : _string.length()).split("\n")[0];
    }
}
