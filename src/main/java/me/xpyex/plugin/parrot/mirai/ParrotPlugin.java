package me.xpyex.plugin.parrot.mirai;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.cron.CronUtil;
import java.lang.reflect.Constructor;
import java.util.TreeSet;
import me.xpyex.plugin.parrot.mirai.core.command.CommandBus;
import me.xpyex.plugin.parrot.mirai.core.event.EventBus;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.utils.MsgUtil;
import me.xpyex.plugin.parrot.mirai.utils.ReflectUtil;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.MiraiLogger;

public class ParrotPlugin extends JavaPlugin {
    public static MiraiLogger LOGGER;
    public static ParrotPlugin INSTANCE;

    public ParrotPlugin() {
        super(new JvmPluginDescriptionBuilder("Parrot", "3.0")
                  .id("me.xpyex.plugin.parrot.mirai.ParrotPlugin")
                  .author("XPYEX")
                  .info("QQ Bot Functions in mirai")
                  .build()
        );
    }

    @Override
    public void onEnable() {
        CronUtil.setMatchSecond(true);
        CronUtil.start();  //启用HuTool定时任务

        INSTANCE = this;
        LOGGER = getLogger();
        LOGGER.info("插件主模块已加载");

        for (Class<?> moduleClass : ReflectUtil.getClasses("module")) {
            if (ClassUtil.isAssignable(Module.class, moduleClass)) {
                if (!ClassUtil.isNormalClass(moduleClass)) continue;
                try {
                    Constructor<?> constructor = moduleClass.getDeclaredConstructor();
                    boolean accessible = constructor.canAccess(null);
                    constructor.setAccessible(true);
                    constructor.newInstance();
                    constructor.setAccessible(accessible);
                } catch (Throwable e) {
                    e.printStackTrace();
                    LOGGER.error("加载模块 " + moduleClass.getSimpleName() + " 时出错: " + e);
                    if (e instanceof NoSuchMethodException) {  //缺少Module()构造方法，可能是别的参数的
                        LOGGER.error("该模块的构造方法不标准！Parrot无法构建模块实例");
                    }
                }
            }
        }

        {
            TreeSet<String> list = new TreeSet<>();
            for (Module loadedModule : Module.LOADED_MODELS.values()) {
                list.add(loadedModule.getName());
            }
            LOGGER.info("已注册的所有模块: " + list);
        }

        GlobalEventChannel.INSTANCE.registerListenerHost(new SimpleListenerHost(INSTANCE.getCoroutineContext()) {
            @EventHandler
            @SuppressWarnings("unused")
            public void onEvent(Event event) {
                if (!EventBus.callToCoreModule(event)) {  //该方法返回false则表示有
                    return;  //该事件已被CoreModule拦截不允许下发处理
                }

                if (event instanceof MessageEvent && MsgUtil.getPlainText(((MessageEvent) event).getMessage()).startsWith("#")) {
                    if (CommandBus.isCmd(MsgUtil.getPlainText(((MessageEvent) event).getMessage()).split(" ")[0].substring(1))) {
                        CommandBus.callCommands((MessageEvent) event, MsgUtil.getPlainText(((MessageEvent) event).getMessage()));
                        return;
                    }
                }
                EventBus.callEvents(event, Module.class);
            }
        });  //广播事件
    }
}