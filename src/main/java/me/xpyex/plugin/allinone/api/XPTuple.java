package me.xpyex.plugin.allinone.api;

import cn.hutool.core.lang.Tuple;

public class XPTuple extends Tuple {

    public XPTuple(Object... objects) {
        super(objects);
    }

    public <T> T get(int index, Class<T> type) {
        if (get(index).getClass() != type) {
            return null;
        }
        return get(index);
    }
}
