package me.xpyex.plugin.allinone.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;
import me.xpyex.plugin.allinone.Main;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;

/**
 * 这是所有模块的根类，所有模块应继承Model类以实现自动注册及所有管理
 *
 * 覆写 getName()         方法 - 自定义模块名
 * 覆写 register()        方法 - 注册
 *
 * 调用 listenEvent()     方法 - 监听Mirai事件
 * 调用 registerCommand() 方法 - 注册AllInOne命令
 * 调用 runTaskLater()    方法 - 延时执行任务
 * 调用 runTaskTimer()    方法 - 创建循环任务
 * 调用 disable()         方法 - 禁用本模块
 * 调用 enable()          方法 - 启用本模块
 * 调用 info()            方法 - 向控制台发送信息
 */
public abstract class Model {
    public boolean DEFAULT_DISABLED = false;
    public static final HashMap<String, Model> LOADED_MODELS = new HashMap<>();
    public static final HashSet<Model> DISABLED_MODELS = new HashSet<>();
    private static final HashMap<Model, HashSet<UUID>> TASKS = new HashMap<>();

    public Model() {
        Main.LOGGER.info("正在加载 " + getName() + " 模块");
        TASKS.put(this, new HashSet<>());
        try {
            register();
            LOADED_MODELS.put(getName(), this);
        } catch (Exception e) {
            e.printStackTrace();
            Main.LOGGER.error("加载模块 " + getName() + " 时出错: " + e);
            return;
        }
        if (this.DEFAULT_DISABLED) {
            this.disable();
            Main.LOGGER.info("模块 " + getName() + " 注册时选用默认关闭，已关闭它");
        }
        Main.LOGGER.info("成功加载 " + getName() + " 模块");
        Main.LOGGER.info(" ");
    }

    public abstract void register();

    /*
    从此往下为模块方法
     */

    public final <C extends Contact> void registerCommand(Class<C> contactType, CommandExecutor<C> exec, String... aliases) {
        for (String s : aliases) {
            if (s.contains(" ")) {
                throw new IllegalArgumentException("注册的命令不应包含空格，应作为参数判断");
            }
        }
        CommandsList.register(this, aliases);
        CommandBus.takeInBus(contactType, this, exec);
        Main.LOGGER.info(getName() + " 模块注册命令: " + Arrays.toString(aliases) + ", 命令监听范围: " + contactType.getSimpleName());
    }

    public final <E extends Event> void listenEvent(Class<E> eventType, Consumer<E> listener) {
        EventBus.takeInBus(eventType, this, listener);
        Main.LOGGER.info(getName() + " 模块注册监听事件: " + eventType.getSimpleName());
    }

    public String getName() {
        return this.getClass().getSimpleName();
        //
    }

    public final boolean disable() {
        return DISABLED_MODELS.add(this);
        //
    }

    public final boolean enable() {
        return DISABLED_MODELS.remove(this);
        //
    }

    public final void info(String s) {
        Main.LOGGER.info("[" + getName() + "] " + s);
        //
    }

    public final void info(Throwable e) {
        Main.LOGGER.info(e);
        //
    }

    /*
    从此往下为工具方法
     */

    @SuppressWarnings("unchecked")
    public static <M extends Model> M getModel(String name) {
        if (name == null || name.trim().isEmpty()) return null;

        if (LOADED_MODELS.containsKey(name)) return (M) LOADED_MODELS.get(name);

        for (String s : LOADED_MODELS.keySet()) {
            if (s.equalsIgnoreCase(name)) return (M) LOADED_MODELS.get(s);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public final  <C extends Contact> C getRealSender(MessageEvent event) {
        return (C) Util.getRealSender(event);
        //
    }

    public final void autoSendMsg(MessageEvent event, Message msg) {
        if (msg == null) return;

        getRealSender(event).sendMessage(msg);
    }

    public final void autoSendMsg(MessageEvent event, String msg) {
        if (msg == null || msg.isEmpty()) return;

        autoSendMsg(event, new PlainText(msg));
    }

    public final void runTaskLater(Runnable r, long seconds) {
        new Thread(() -> {
            if (seconds != 0L) {
                try {
                    Thread.sleep(seconds * 1000L);
                } catch (Exception ignored) {
                }
            }

            try {
                r.run();
            } catch (Exception e) {
                handleException(e);
            }
        }, "AllInOne-Task-" + this.getName()).start();
    }

    public final UUID runTaskTimer(Runnable r, long repeatPeriodSeconds) {
        return runTaskTimer(r, repeatPeriodSeconds, 0);
        //
    }

    public final UUID runTaskTimer(Runnable r, long repeatPeriodSeconds, long waitSeconds) {
        if (r == null) {
            return null;
        }
        if (repeatPeriodSeconds <= 0) {
            throw new IllegalArgumentException("周期为0将堵塞任务线程");
        }
        if (waitSeconds < 0) {
            throw new IllegalArgumentException("你可以不填这个参数的，谢谢");
        }
        UUID uuid = UUID.randomUUID();
        TASKS.get(this).add(uuid);

        runTaskLater(() -> {
            while (TASKS.get(this).contains(uuid)) {
                try {
                    r.run();
                } catch (Exception e) {
                    handleException(e);
                }

                try {
                    Thread.sleep(repeatPeriodSeconds * 1000L);
                } catch (Exception ignored) { }
            }
        }, waitSeconds);
        return uuid;
    }

    public final boolean shutdownRepeatTask(UUID uuid) {
        if (uuid == null) return false;

        return TASKS.get(this).remove(uuid);
    }

    public final Bot getBot() {
        return Bot.getInstances().get(0);
        //
    }

    public final void handleException(Throwable e) {
        handleException(e, true);
        //
    }

    public final void handleException(Throwable e, boolean noticeOwner) {
        e.printStackTrace();
        if (noticeOwner) {
            sendMsgToOwner("在执行 " + e.getStackTrace()[0].getClassName() + " 类的方法 " +
                    e.getStackTrace()[0].getMethodName() + " 时出错: " +
                    e + "\n" +
                    "该代码位于该类的第 " + e.getStackTrace()[0].getLineNumber() + " 行");
        }
    }

    public final String getPlainText(MessageChain message) {
        return Util.getPlainText(message);
        //
    }

    public final void sendMsgToOwner(String msg) {
        if (msg == null || msg.isEmpty()) return;

        sendMsgToOwner(new PlainText(msg));
    }

    public final void sendMsgToOwner(Message msg) {
        if (msg == null) return;

        getBot().getFriend(Util.OWNER_ID).sendMessage(msg);
    }
}
