package me.xpyex.plugin.parrot.mirai.module;

import cn.hutool.core.lang.Pair;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;
import me.xpyex.plugin.parrot.mirai.api.CommandMenu;
import me.xpyex.plugin.parrot.mirai.api.MessageBuilder;
import me.xpyex.plugin.parrot.mirai.core.command.argument.ArgParser;
import me.xpyex.plugin.parrot.mirai.core.command.argument.GroupParser;
import me.xpyex.plugin.parrot.mirai.core.command.argument.StrParser;
import me.xpyex.plugin.parrot.mirai.core.command.argument.UserParser;
import me.xpyex.plugin.parrot.mirai.core.mirai.ParrotContact;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.modulecode.git.GitInfo;
import me.xpyex.plugin.parrot.mirai.modulecode.git.ReleasesUpdate;
import me.xpyex.plugin.parrot.mirai.utils.FileUtil;
import me.xpyex.plugin.parrot.mirai.utils.MsgUtil;
import me.xpyex.plugin.parrot.mirai.utils.Util;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.PlainText;

public class GitUpdates extends Module {
    private File urls;

    @SneakyThrows
    private void reload() {
        ReleasesUpdate.setInstance(JSONUtil.toBean(FileUtil.readFile(urls), ReleasesUpdate.class));
    }

    @Override
    public void register() throws Throwable {
        urls = new File(getDataFolder(), "urls.json");
        if (!urls.exists()) {
            urls.createNewFile();
            new ReleasesUpdate().save(urls);
        }

        reload();

        registerCommand(Contact.class, (source, sender, label, args) -> {
            if (!sender.hasPerm(getName() + ".use", MemberPermission.ADMINISTRATOR)) {
                source.sendMessage("你没有权限");
                return;
            }
            if (args.length == 0) {
                new CommandMenu(label)
                    .add("add <GitHub|Gitee> <Owner/RepoName> <是否需要上传文件(true|false)>", "订阅指定Git平台的仓库，发布releases时推送")
                    .add("remove <Owner/RepoName>", "解除订阅")
                    .send(source);
                return;
            }
            if ("checkNow".equalsIgnoreCase(args[0])) {
                checkUpdate();
            }
            if ("add".equalsIgnoreCase(args[0])) {
                if (args.length < 4) {  //updates add GitHub Owner/RepoName
                    source.sendMessage("参数不足");
                    return;
                }
                if (!args[2].contains("/")) {
                    source.sendMessage("需要填入 <用户名/仓库名> 的格式");
                    return;
                }
                Map<Long, Set<GitInfo>> map;
                if (source.getContact() instanceof Group) {
                    map = ReleasesUpdate.getInstance().getGroups();
                } else {
                    map = ReleasesUpdate.getInstance().getUsers();
                }
                if (!map.containsKey(source.getId())) {
                    map.put(source.getId(), new HashSet<>());
                }
                for (GitInfo.SupportedGits value : GitInfo.SupportedGits.values()) {
                    if (value.toString().equalsIgnoreCase(args[1])) {
                        map.get(source.getId()).add(new GitInfo()
                                                        .setType(value)
                                                        .setRepo(args[2])
                                                        .setUploadFile("true".equalsIgnoreCase(args[3]))
                        );
                        ReleasesUpdate.getInstance().save(urls);
                        source.sendMessage("已订阅该Repo");
                        return;
                    }
                }
                source.sendMessage("无效的仓库类型");
            }
            if ("remove".equalsIgnoreCase(args[0])) {
                ArgParser.of(StrParser.class).parse(() -> args[1])
                    .ifPresentOrElse(path -> {
                        boolean[] result = {false};
                        ReleasesUpdate.getInstance().getUsers().forEach((ID, info) -> {
                            if (result[0]) return;
                            for (GitInfo gitInfo : info) {
                                if (gitInfo.getRepo().equals(path)) {
                                    runTaskLater(() -> {
                                        ReleasesUpdate.getInstance().getUsers().get(ID).remove(gitInfo);
                                        ReleasesUpdate.getInstance().save(urls);
                                    }, 1);
                                    result[0] = true;
                                    return;
                                }
                            }
                        });
                        ReleasesUpdate.getInstance().getGroups().forEach((ID, info) -> {
                            if (result[0]) return;
                            for (GitInfo gitInfo : info) {
                                if (gitInfo.getRepo().equals(path)) {
                                    runTaskLater(() -> {
                                        ReleasesUpdate.getInstance().getGroups().get(ID).remove(gitInfo);
                                        ReleasesUpdate.getInstance().save(urls);
                                    }, 1);
                                    result[0] = true;
                                    return;
                                }
                            }
                        });
                        source.sendMessage(result[0] ? "已解除订阅" : "未订阅该Repo");
                    }, () -> source.sendMessage("参数不足"));
            }
            if ("reload".equalsIgnoreCase(args[0])) {
                reload();
                source.sendMessage("重新载入文件");
            }
        }, "updates", "gitUpdates", "git", "repo");
        runTaskTimer(this::checkUpdate, 25 * 60L, 60L);
    }

    private void checkUpdate() {
        HashMap<String, String> repoURLs = new HashMap<>();  //Repo, URL
        ReleasesUpdate.getInstance().getGroups().values().forEach(URLs -> {
            URLs.forEach(info -> {
                switch (info.getType()) {
                    case Gitee:
                        repoURLs.put(info.getRepo(), "https://gitee.com/api/v5/repos/" + info.getRepo() + "/releases/latest");
                        break;
                    case GitHub:
                        repoURLs.put(info.getRepo(), "https://api.github.com/repos/" + info.getRepo() + "/releases/latest");
                        break;
                }
            });
        });
        ReleasesUpdate.getInstance().getUsers().values().forEach(URLs -> {
            URLs.forEach(info -> {
                switch (info.getType()) {
                    case Gitee:
                        repoURLs.put(info.getRepo(), "https://gitee.com/api/v5/repos/" + info.getRepo() + "/releases/latest");
                        break;
                    case GitHub:
                        repoURLs.put(info.getRepo(), "https://api.github.com/repos/" + info.getRepo() + "/releases/latest");
                        break;
                }
            });
        });

        HashMap<String, JSONObject> results = new HashMap<>();  //Repo, Results
        repoURLs.forEach((repo, api) -> {
            JSONObject result = new JSONObject(HttpRequest.get(api).execute().body());
            if (!ReleasesUpdate.getInstance().getCache().containsKey(repo) || !ReleasesUpdate.getInstance().getCache().get(repo).equalsIgnoreCase(result.getStr("tag_name"))) {
                //此时就是检查到相对自身而言的“新版本”
                results.put(repo, result);
                //是新版本再存入
            }

            try {
                Thread.sleep(5000L);
            } catch (InterruptedException ignored) {
            }

        });  //去获取更新

        HashMap<Contact, ArrayList<Pair<String, Boolean>>> contacts = new HashMap<>();  //Contact, <Repo, NeedUpload>
        ReleasesUpdate.getInstance().getGroups().forEach((ID, URLs) -> {
            ArgParser.of(GroupParser.class).parse(ID).ifPresent(group -> {
                for (GitInfo info : URLs) {
                    if (!contacts.containsKey(group)) {
                        contacts.put(group, new ArrayList<>());
                    }
                    contacts.get(group).add(Pair.of(info.getRepo(), info.isUploadFile()));
                }
            });
        });
        ReleasesUpdate.getInstance().getUsers().forEach((ID, URLs) -> {
            ArgParser.of(UserParser.class).parse(ID).ifPresent(friend -> {
                for (GitInfo info : URLs) {
                    if (!contacts.containsKey(friend)) {
                        contacts.put(friend, new ArrayList<>());
                    }
                    contacts.get(friend).add(Pair.of(info.getRepo(), info.isUploadFile()));
                }
            });
        });

        HashMap<String, String> newVer = new HashMap<>();  //Repo, Version
        contacts.forEach((contact, list) -> {
            for (Pair<String, Boolean> pair : list) {
                Optional.ofNullable(results.get(pair.getKey())).ifPresent(got -> {
                    System.out.println(got.toStringPretty());
                    if (!ReleasesUpdate.getInstance().getCache().containsKey(pair.getKey()) || !ReleasesUpdate.getInstance().getCache().get(pair.getKey()).equalsIgnoreCase(got.getStr("tag_name"))) {
                        //此时就是检查到相对自身而言的“新版本”
                        String verName = got.getStr("tag_name");
                        newVer.put(pair.getKey(), verName);
                        ForwardMessageBuilder builder = MsgUtil.getForwardMsgBuilder(contact);
                        String releasePage = got.containsKey("html_url") ? got.getStr("html_url") : "https://gitee.com/" + pair.getKey() + "/releases";
                        builder.add(Util.getBot(), new PlainText(new MessageBuilder()
                                                                     .plus(pair.getKey().split("/")[1] + " 发布了新Release:")
                                                                     .plus("版本名: " + got.getStr("name"))
                                                                     .plus("版本号: " + verName)
                                                                     .plus("发布时间: " + got.getStr("published_at", got.getStr("created_at")).replace("T", " ").replace("Z", "").replace("+08:00", ""))
                                                                     .plus("")
                                                                     .plus("更新内容: ")
                                                                     .plus(got.getStr("body").substring(0, Math.min(2200, got.getStr("body").length())))
                                                                     .toString()))
                            .add(Util.getBot(), new PlainText(
                                new MessageBuilder()
                                    .plus("详细内容请至 <发布页面> 查看")
                                    .plus("发布页面: " + releasePage)
                                    .toString()
                            ));
                        try {
                            contact.sendMessage(builder.build());
                        } catch (Throwable ignored) {
                        }

                        try {
                            if (pair.getValue()) {  //需要上传文件
                                String fileName = got.getJSONArray("assets").getJSONObject(0).getStr("name");
                                if (!fileName.contains(verName.replace("v", ""))) {
                                    String[] splitName = fileName.split("\\.");
                                    String name = String.join(".", Arrays.copyOfRange(splitName, 0, splitName.length - 1));
                                    String type = splitName[splitName.length - 1];
                                    fileName = name + "-" + (verName.contains("v") ? "" : "v") + verName + "." + type;
                                }
                                ParrotContact.of(contact).uploadFile(got.getJSONArray("assets").getJSONObject(0).getStr("browser_download_url"), fileName, "RepoUpdates");
                            }
                        } catch (Exception e) {
                            contact.sendMessage("更新文件上传失败: " + e);
                        }
                    }
                });
            }
        });
        ReleasesUpdate.getInstance().getCache().putAll(newVer);
        ReleasesUpdate.getInstance().save(urls);
    }
}
