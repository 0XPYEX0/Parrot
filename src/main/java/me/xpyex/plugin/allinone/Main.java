package me.xpyex.plugin.allinone;

import me.xpyex.plugin.allinone.functions.botchecker.BotChecker;
import me.xpyex.plugin.allinone.functions.informs.MsgToOwner;
import me.xpyex.plugin.allinone.functions.informs.PokeAt;
import me.xpyex.plugin.allinone.functions.informs.Repeater;
import me.xpyex.plugin.allinone.functions.informs.Tell;
import me.xpyex.plugin.allinone.functions.manager.CoreCmds;
import me.xpyex.plugin.allinone.functions.manager.GroupBroadcast;
import me.xpyex.plugin.allinone.functions.manager.JoinAcceptor;
import me.xpyex.plugin.allinone.functions.manager.PluginManager;
import me.xpyex.plugin.allinone.functions.manager.StaffTeam;
import me.xpyex.plugin.allinone.functions.music.MiraiMusic;
import me.xpyex.plugin.allinone.functions.networktasks.BiliBili;
import me.xpyex.plugin.allinone.functions.nowankday.NoWankDay;
import me.xpyex.plugin.allinone.functions.qqlists.Nide8Blacklist;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.BotOfflineEvent;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MemberJoinRequestEvent;
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
        INSTANCE = this;
        LOGGER = getLogger();

        LOGGER.info("[AllInOne] 插件主模块已加载");
        LOGGER.info(" BotChecker模块已加载");
        GroupBroadcast.load();
        JoinAcceptor.load();
        LOGGER.info(" JoinAcceptor模块已加载");
        LOGGER.info(" MsgToOwner模块已加载");
        MiraiMusic.load();
        Nide8Blacklist.load();
        NoWankDay.load();
        LOGGER.info(" NoWankDay模块已加载");
        LOGGER.info(" 已启用NoWankDayTimer任务");
        Repeater.load();
        LOGGER.info(" Repeater模块已加载");
        StaffTeam.load();
        LOGGER.info(" Tell模块已加载");
        LOGGER.info(" CoreCmds核心组件已加载");
        LOGGER.info(" PluginManager核心组件已加载");
        PokeAt.load();
        GlobalEventChannel.INSTANCE.registerListenerHost(new SimpleListenerHost(INSTANCE.getCoroutineContext()) {
            @EventHandler
            public void onAsk(MemberJoinRequestEvent e) {
                JoinAcceptor.Execute(e);
            }
            @EventHandler
            public void onGroupMsg(GroupMessageEvent event) {
                Repeater.Execute(event);
                NoWankDay.Execute(event);
            }
            @EventHandler
            public void onMsg(MessageEvent event) {
                CoreCmds.Execute(event);
                BiliBili.Execute(event);
                MsgToOwner.Execute(event);
                BotChecker.Execute(event); //调整优先级
                PluginManager.Execute(event);
                Tell.Execute(event);
            }
            @EventHandler
            public void onBotLogin(BotOnlineEvent event) {
                if (!tellOwner) {
                    MsgToOwner.sendMsgToOwner("已成功重启");
                    tellOwner = true;
                }
            }
            @EventHandler
            public void onJoinGroup(MemberJoinEvent event) {
                BotChecker.Execute(event);
                JoinAcceptor.Execute(event);
            }

            @EventHandler
            public void onShutDown(BotOfflineEvent event) {
                Util.cacheFolder.delete();
            }
        });
    }
}        