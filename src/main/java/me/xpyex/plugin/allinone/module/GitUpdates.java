package me.xpyex.plugin.allinone.module;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import me.xpyex.plugin.allinone.api.CommandMenu;
import me.xpyex.plugin.allinone.api.MessageBuilder;
import me.xpyex.plugin.allinone.core.Module;
import me.xpyex.plugin.allinone.modulecode.git.GitInfo;
import me.xpyex.plugin.allinone.modulecode.git.ReleasesUpdate;
import me.xpyex.plugin.allinone.utils.FileUtil;
import me.xpyex.plugin.allinone.utils.MsgUtil;
import me.xpyex.plugin.allinone.utils.Util;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.message.data.PlainText;

public class GitUpdates extends Module {
    @Override
    public void register() throws Throwable {
        File urls = new File(getDataFolder(), "urls.json");
        if (!urls.exists()) {
            urls.createNewFile();
            FileUtil.writeFile(urls, JSONUtil.toJsonPrettyStr(new ReleasesUpdate()));
        }
        JSONObject i = JSONUtil.parseObj(FileUtil.readFile(urls));
        ReleasesUpdate.setInstance(JSONUtil.toBean(i, ReleasesUpdate.class));

        registerCommand(Contact.class, (source, sender, label, args) -> {
            if (!sender.hasPerm(getName() + ".use", MemberPermission.ADMINISTRATOR)) {
                source.sendMessage("你没有权限");
                return;
            }
            if (args.length == 0) {
                new CommandMenu(label)
                    .add("add <GitHub|Gitee> <Owner/RepoName>", "订阅指定Git平台的仓库，push|发布releases时推送")
                    .send(source);
                return;
            }
            if ("add".equalsIgnoreCase(args[0])) {
                if (args.length < 3) {  //updates add GitHub Owner/RepoName
                    source.sendMessage("参数不足");
                    return;
                }
                if (!args[2].contains("/")) {
                    source.sendMessage("需要填入 <用户名/仓库名> 的格式");
                }
                Map<Long, List<GitInfo>> map;
                if (source.getContact() instanceof Group) {
                    map = ReleasesUpdate.getInstance().getGroups();
                } else {
                    map = ReleasesUpdate.getInstance().getUsers();
                }
                if (!map.containsKey(source.getId())) {
                    map.put(source.getId(), new ArrayList<>());
                }
                map.get(source.getId()).add(new GitInfo(GitInfo.SupportedGits.valueOf(args[1]), args[2]));
                FileUtil.writeFile(urls, JSONUtil.toJsonPrettyStr(ReleasesUpdate.getInstance()));
                source.sendMessage("已订阅该Repo");
            }
        }, "updates", "gitUpdates");
        runTaskTimer(() -> {
            ReleasesUpdate.getInstance().getGroups().forEach((ID, URLs) -> {
                for (GitInfo info : URLs) {
                    if (info.getType() == GitInfo.SupportedGits.GitHub) {
                        JSONObject result = new JSONObject(HttpRequest.get("https://api.github.com/repos/" + info.getRepo() + "/releases/latest")
                                                               .header("Accept", "application/vnd.github+json")
                                                               .header("X-GitHub-Api-Version", "2022-11-28")
                                                               .execute().body()
                        );
                        if (!ReleasesUpdate.getInstance().getCache().containsKey(info.getRepo()) || !ReleasesUpdate.getInstance().getCache().get(info.getRepo()).equalsIgnoreCase(result.getStr("tag_name"))) {
                            //此时就是检查到相对自身而言的“新版本”
                            ReleasesUpdate.getInstance().getCache().put(info.getRepo(), result.getStr("tag_name"));
                            Optional.ofNullable(Util.getBot().getGroup(ID))
                                .ifPresent(group -> {
                                    group.sendMessage(MsgUtil.getForwardMsgBuilder(Util.getBot().getAsFriend())
                                                          .add(Util.getBot(), new PlainText(new MessageBuilder()
                                                                                  .plus(info.getRepo().split("/")[1] + " 发布了新Release:")
                                                                                  .plus("版本: " + result.getStr("tag_name"))
                                                                                  .plus("")
                                                                                  .plus("更新内容: ")
                                                                                  .plus(result.getStr("body").substring(0, Math.min(2000, result.getStr("body").length())))
                                                                                  .plus("")
                                                                                  .plus("发布页面: " + result.getStr("html_url"))
                                                                                  .toString()))
                                                          .build());

                                    try {
                                        FileUtil.writeFile(urls, JSONUtil.toJsonPrettyStr(ReleasesUpdate.getInstance()));
                                    } catch (IOException e) {
                                        handleException(e);
                                    }
                                });
                        }
                    }
                }
            });
            ReleasesUpdate.getInstance().getUsers().forEach((ID, URLs) -> {
                for (GitInfo info : URLs) {
                    if (info.getType() == GitInfo.SupportedGits.GitHub) {
                        JSONObject result = new JSONObject(HttpRequest.get("https://api.github.com/repos/" + info.getRepo() + "/releases/latest")
                                                               .header("Accept", "application/vnd.github+json")
                                                               .header("X-GitHub-Api-Version", "2022-11-28")
                                                               .execute().body()
                        );
                        if (!ReleasesUpdate.getInstance().getCache().containsKey(info.getRepo()) || !ReleasesUpdate.getInstance().getCache().get(info.getRepo()).equalsIgnoreCase(result.getStr("tag_name"))) {
                            //此时就是检查到相对自身而言的“新版本”
                            ReleasesUpdate.getInstance().getCache().put(info.getRepo(), result.getStr("tag_name"));
                            Optional.ofNullable(Util.getBot().getGroup(ID))
                                .ifPresent(group -> {
                                    group.sendMessage(MsgUtil.getForwardMsgBuilder(Util.getBot().getAsFriend())
                                                          .add(Util.getBot(), new PlainText(new MessageBuilder()
                                                                                                .plus(info.getRepo().split("/")[1] + " 发布了新Release:")
                                                                                                .plus("版本: " + result.getStr("tag_name"))
                                                                                                .plus("")
                                                                                                .plus("更新内容: ")
                                                                                                .plus(result.getStr("body").substring(0, Math.min(2000, result.getStr("body").length())))
                                                                                                .plus("")
                                                                                                .plus("发布页面: " + result.getStr("html_url"))
                                                                                                .toString()))
                                                          .build());

                                    try {
                                        FileUtil.writeFile(urls, JSONUtil.toJsonPrettyStr(ReleasesUpdate.getInstance()));
                                    } catch (IOException e) {
                                        handleException(e);
                                    }
                                });
                        }
                    }
                }
            });
        }, 10 * 60L, 60L);
    }
}
