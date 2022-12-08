package me.xpyex.plugin.allinone;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.cron.CronUtil;
import java.io.File;
import java.io.PrintWriter;
import java.util.TreeSet;
import me.xpyex.plugin.allinone.core.CommandBus;
import me.xpyex.plugin.allinone.core.EventBus;
import me.xpyex.plugin.allinone.core.Model;
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
    public static Main INSTANCE;

    public Main() {
        super(new JvmPluginDescriptionBuilder("AllInOne","3.0.1")
                .id("me.xpyex.plugin.allinone.Main")
                .author("XPYEX")
                .info("Everything in this")
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

        for (Class<?> modelClass : ReflectUtil.getClasses("model")) {
            if (ClassUtil.isAssignable(Model.class, modelClass)) {
                try {
                    modelClass.newInstance();
                } catch (Throwable e) {
                    e.printStackTrace();
                    LOGGER.error("加载模块 " + modelClass.getSimpleName() + " 时出错: " + e);
                }
            }
        }

        {
            TreeSet<String> list = new TreeSet<>();
            for (Model loadedModel : Model.LOADED_MODELS.values()) {
                list.add(loadedModel.getName());
            }
            LOGGER.info("已注册的所有模块: " + list);
        }

        GlobalEventChannel.INSTANCE.registerListenerHost(new SimpleListenerHost(INSTANCE.getCoroutineContext()) {
            @EventHandler
            @SuppressWarnings("unused")
            public void onEvent(Event event) {
                if (!EventBus.callToCoreModel(event)) {  //该方法返回false则表示有
                    return;  //该事件已被CoreModel拦截不允许下发处理
                }

                if (event instanceof MessageEvent && Util.getPlainText(((MessageEvent) event).getMessage()).startsWith("#")) {
                    if (CommandBus.isCmd(Util.getPlainText(((MessageEvent) event).getMessage()).split(" ")[0])) {
                        CommandBus.callCommands((MessageEvent) event, Util.getPlainText(((MessageEvent) event).getMessage()));
                        return;
                    }
                }
                EventBus.callEvents(event, Model.class);
            }
        });  //广播事件

        outBatFiles();
    }

    public static void outBatFiles() {
        File restartFile = new File("RestartJava.bat");
        File stopFile = new File("StopJava.bat");
        try {
            if (!restartFile.exists()) {
                restartFile.createNewFile();
                PrintWriter out = new PrintWriter(restartFile);
                out.println("taskkill /f /im java.exe");
                out.print("start jre/bin/java -jar mcl.jar %* ");
                out.flush();
                out.close();
            }
            if (!stopFile.exists()) {
                stopFile.createNewFile();
                PrintWriter out = new PrintWriter(stopFile, "UTF-8");
                out.print("taskkill /f /im java.exe");
                out.flush();
                out.close();
            }
        } catch (Exception ignored) {}
    }
}        