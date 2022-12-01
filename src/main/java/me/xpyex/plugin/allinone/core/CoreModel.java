package me.xpyex.plugin.allinone.core;

import net.mamoe.mirai.event.Event;

public abstract class CoreModel extends Model {  //仅用于判断是否为核心模块
    /**
     * 是否拦截事件被其它模块处理
     * 应当被子类覆写
     * @param event 传入的事件
     * @return false则拦截，true则正常处理
     */
    public boolean interceptEvent(Event event) {
        return true;
        //
    }
}
