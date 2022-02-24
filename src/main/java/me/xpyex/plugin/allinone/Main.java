package me.xpyex.plugin.allinone;

import cn.hutool.cron.CronUtil;
import me.xpyex.plugin.allinone.functions.autosignvpn.AutoSign;
import me.xpyex.plugin.allinone.functions.manager.*;
import me.xpyex.plugin.allinone.functions.music.MiraiMusic;
import me.xpyex.plugin.allinone.functions.networktasks.BiliBili;
import me.xpyex.plugin.allinone.functions.informs.MsgToOwner;
import me.xpyex.plugin.allinone.functions.informs.Repeater;
import me.xpyex.plugin.allinone.functions.informs.Tell;
import me.xpyex.plugin.allinone.functions.botchecker.BotChecker;
import me.xpyex.plugin.allinone.functions.nowankday.NoWankDay;
import me.xpyex.plugin.allinone.functions.qqlists.Nide8Blacklist;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.utils.MiraiLogger;


public class Main extends JavaPlugin {
    public static MiraiLogger logger;
    public static boolean tellOwner = false;
    public static Main INSTANCE;
    public Main() {
        super(new JvmPluginDescriptionBuilder("AllInOne","1.0.0").id("me.xpyex.plugin.allinone.Main").author("XPYEX").info("Everything in this").build());
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        logger = getLogger();
        logger.info("[AllInOne] 插件主模块已加载");
        BotChecker.enableMode = true;
        logger.info(" BotChecker模块已加载");
        GroupBroadcast.load();
        JoinAcceptor.load();
        logger.info(" JoinAcceptor模块已加载");
        logger.info(" MsgToOwner模块已加载");
        MiraiMusic.load();
        Nide8Blacklist.load();
        NoWankDay.load();
        logger.info(" NoWankDay模块已加载");
        logger.info(" 已启用NoWankDayTimer任务");
        logger.info(" QiongJu模块已加载");
        Repeater.load();
        logger.info(" Repeater模块已加载");
        StaffTeam.load();
        logger.info(" Tell模块已加载");
        logger.info(" CoreCmds核心组件已加载");
        logger.info(" PluginManager核心组件已加载");
        CronUtil.setMatchSecond(true);
        CronUtil.start();
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
                AutoSign.Execute(event);
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
        });
    }
}        