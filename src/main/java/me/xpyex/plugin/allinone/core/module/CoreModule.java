package me.xpyex.plugin.allinone.core.module;

import net.mamoe.mirai.event.Event;

public abstract class CoreModule extends Module {  //仅用于判断是否为核心模块
    /**
     * 是否拦截事件被其它模块处理
     * 换言之: 是否接受(accept)该事件
     * 该方法应当被子类覆写
     *
     * @param event 传入的事件
     * @return false则拦截，true则正常处理
     */
    public boolean acceptEvent(Event event) {
        return true;
        //
    }
}
