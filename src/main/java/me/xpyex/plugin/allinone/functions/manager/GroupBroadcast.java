package me.xpyex.plugin.allinone.functions.manager;

import java.io.IOException;
import java.net.URL;
import me.xpyex.plugin.allinone.Main;
import me.xpyex.plugin.allinone.commands.CommandsList;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.announcement.AnnouncementImage;
import net.mamoe.mirai.contact.announcement.AnnouncementParametersBuilder;
import net.mamoe.mirai.contact.announcement.OfflineAnnouncement;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.ExternalResource;

public class GroupBroadcast {
    public static void load() {
        Main.LOGGER.info("GroupBroadcast模块已加载");
        CommandsList.register(GroupBroadcast.class, "/broadcast", "/QGBC");
        GlobalEventChannel.INSTANCE.registerListenerHost(new SimpleListenerHost(Main.INSTANCE.getCoroutineContext()) {
            @EventHandler
            public void onMsg(MessageEvent event) {
                String[] cmd = Util.getPlainText(event.getMessage()).split("\n");
                if (CommandsList.isCmd(GroupBroadcast.class, cmd[0])) {
                    if (!Util.canExecute(event)) {
                        Util.autoSendMsg(event, "你没有权限");
                        return;
                    }
                    try {
                        Group targetGroup = null;
                        if (Util.isGroupEvent(event)) {
                            targetGroup = ((GroupMessageEvent) event).getGroup();
                        } else {
                            if (!Util.getPlainText(event.getMessage()).contains("群号:")) {
                                Util.autoSendMsg(event, "需要指定群号");
                                return;
                            }
                            for (String parameter : cmd) {
                                if (!parameter.startsWith("群号:")) {
                                    continue;
                                }
                                targetGroup = event.getBot().getGroup(Long.parseLong(parameter.substring(3)));
                                break;
                            }
                        }
                        if (targetGroup == null) {
                            Util.autoSendMsg(event, "群不存在");
                            return;
                        }
                        if (targetGroup.getBotPermission() == MemberPermission.MEMBER) {
                            Util.autoSendMsg(event, "bot没有权限");
                            return;
                        }
                        AnnouncementParametersBuilder builder = new AnnouncementParametersBuilder();
                        String mainText = "";
                        boolean disabledFunc = false;
                        for (String parameter : cmd) {
                            if (parameter.equals(cmd[0])) {
                                continue;
                            }
                            if (parameter.startsWith("群号:")) {
                                continue;
                            }
                            if (parameter.startsWith("图片:")) {
                                AnnouncementImage image = targetGroup.getAnnouncements().uploadImage(ExternalResource.create(new URL(parameter.substring(3)).openStream()));
                                builder.image(image);
                            } else if (parameter.equals("显示更改群名片")) {
                                if (disabledFunc) {
                                    Util.autoSendMsg(event, "需要确认与显示更改群名片冲突，将仅生效其中之一");
                                }
                                builder.showEditCard(true);
                                disabledFunc = true;
                            } else if (parameter.equals("需要确认")) {
                                if (disabledFunc) {
                                    Util.autoSendMsg(event, "需要确认与显示更改群名片冲突，将仅生效其中之一");
                                }
                                builder.requireConfirmation(true);
                                disabledFunc = true;
                            } else if (parameter.equals("发给新成员")) {
                                builder.sendToNewMember(true);
                            } else if (parameter.equals("置顶")) {
                                builder.isPinned(true);
                            } else {
                                mainText = mainText + parameter;
                            }
                        }
                        if (mainText.isEmpty() && builder.image() == null) {
                            return;
                        }
                        targetGroup.getAnnouncements().publish(OfflineAnnouncement.create(mainText, builder.build()));
                    } catch (NumberFormatException ignored) {
                        Util.autoSendMsg(event, "参数类型错误: 群号非整型");
                    } catch (IOException ignored) {
                        Util.autoSendMsg(event, "无法获取图片");
                    }
                }
            }
        });
    }
}
