package me.xpyex.plugin.allinone.utils;

public class StringUtil {
    public static String getStrBeforeChar(String _string, String _key, String _char) {
        int keyIndex = _key.length() + _string.indexOf(_key);
        return _string.substring(keyIndex, _string.substring(keyIndex).contains(_char) ? keyIndex + _string.substring(keyIndex).indexOf(_char) : _string.length()).split("\n")[0];
    }
}
