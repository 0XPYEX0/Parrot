package me.xpyex.plugin.allinone;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.cron.CronUtil;
import me.xpyex.plugin.allinone.core.Model;
import me.xpyex.plugin.allinone.core.model.PluginManagerModel;
import me.xpyex.plugin.allinone.utils.ReflectUtil;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.MiraiLogger;


public class Main extends JavaPlugin {
    public static MiraiLogger LOGGER;
    public static boolean tellOwner = false;
    public static Main INSTANCE;
    public Main() {
        super(new JvmPluginDescriptionBuilder("AllInOne","1.0.0")
                .id("me.xpyex.plugin.allinone.Main")
                .author("XPYEX")
                .info("Everything in this")
                .build()
        );
    }

    @Override
    public void onEnable() {
        CronUtil.setMatchSecond(true);
        CronUtil.start();

        INSTANCE = this;
        LOGGER = getLogger();
        LOGGER.info("插件主模块已加载");

        new PluginManagerModel();

        for (Class<?> modelClass : ReflectUtil.getClasses("me.xpyex.plugin.allinone.models")) {
            if (ClassUtil.isAssignable(modelClass, Model.class) || ClassUtil.isAssignable(Model.class, modelClass)) {
                try {
                    modelClass.newInstance();
                } catch (Exception e) {
                    Util.handleException(e);
                }
            }
        }

        LOGGER.info("已注册的所有模块: " + Model.MODELS);

        GlobalEventChannel.INSTANCE.registerListenerHost(new SimpleListenerHost(INSTANCE.getCoroutineContext()) {
            @EventHandler
            public void onEvent(Event event) {
                if (event instanceof MessageEvent && Util.getPlainText(((MessageEvent) event).getMessage()).startsWith("#")) {
                    Model.callCommands((MessageEvent) event, Util.getPlainText(((MessageEvent) event).getMessage()));
                }
                Model.callEvents(event);
                //
            }
        });  //广播事件

        new Thread(() -> {
            try {
                Thread.sleep(5000L);
            } catch (Exception ignored) {}
            Util.getBot().getFriend(1723275529L).sendMessage("已启动");
        }).start();
    }
}        