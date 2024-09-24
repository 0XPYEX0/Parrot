package me.xpyex.plugin.parrot.mirai.module;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.experimental.ExtensionMethod;
import me.xpyex.plugin.parrot.mirai.api.CommandMenu;
import me.xpyex.plugin.parrot.mirai.api.MessageBuilder;
import me.xpyex.plugin.parrot.mirai.core.command.argument.ArgParser;
import me.xpyex.plugin.parrot.mirai.core.command.argument.StrParser;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.utils.StringUtil;
import me.xpyex.plugin.parrot.mirai.utils.ValueUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;

@ExtensionMethod(ArgParser.class)
public class SearchSkriptHub extends Module {
    private static JSONArray syntaxList;

    private static void downloadDocAndSave() throws IOException {
        getModule(SearchSkriptHub.class).info("正在下载Skript文档...");
        URL url = new URL("https://skripthub.net/api/v1/addonsyntaxlist/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        syntaxList = new JSONArray(new String(connection.getInputStream().readAllBytes()));
        File syntaxListFile = new File(getModule(SearchSkriptHub.class).getDataFolder(), "SkriptExpressions.json");
        Files.write(syntaxListFile.toPath(), syntaxList.toStringPretty().getBytes());
        getModule(SearchSkriptHub.class).info("下载完成，文件已保存至" + syntaxListFile.getAbsolutePath());
    }

    @Override
    public void register() throws Throwable {
        runTaskLater(SearchSkriptHub::downloadDocAndSave, 5);  //每次启动Bot时，下载最新版本覆盖
        registerCommand(Contact.class, (source, sender, label, args) -> {
            if (!sender.hasPerm(getName() + ".use")) {
                source.sendMessage("缺少权限节点: " + getName() + ".use");
                return;
            }
            StrParser.class.of().parse(() -> args[0]).ifPresentOrElse(a -> {
                if ("search".equalsIgnoreCase(a)) {
                    ArrayList<String> addon = new ArrayList<>();
                    ArrayList<String> type = new ArrayList<>();
                    String[] key = {""};
                    for (String s : Arrays.copyOfRange(args, 1, args.length)) {
                        if (StringUtil.startsWithIgnoreCaseOr(s, "addon:")) {
                            addon.addAll(List.of(s.substring(6).split(",")));
                        } else if (StringUtil.startsWithIgnoreCaseOr(s, "type:")) {
                            type.addAll(List.of(s.substring(5).split(",")));
                        } else {
                            key[0] = key[0] + " " + s;
                        }
                    }
                    key[0] = key[0].trim();
                    if (key[0].isEmpty()) {
                        source.sendMessage("关键词为空");
                        return;
                    }
                    ForwardMessageBuilder forwardMessage = new ForwardMessageBuilder(source.getContact());
                    syntaxList.stream().filter(json -> {
                        if (json instanceof JSONObject obj) {
                            if (obj.getStr("title").contains(key[0]) || obj.getStr("description").contains(key[0])) {
                                return true;
                            }
                            if (obj.getStr("syntax_pattern").replaceAll("\\(\\)\\[]", "").contains(key[0])) {
                                return true;
                            }
                        }
                        return false;
                    }).filter(json -> {
                        if (!type.isEmpty()) {
                            JSONObject obj = (JSONObject) json;
                            for (String s : type) {
                                if (s.equalsIgnoreCase(obj.getStr("syntax_type")))
                                    return true;
                            }
                            return false;
                        }
                        return true;
                    }).filter(json -> {
                        if (!addon.isEmpty()) {
                            JSONObject obj = (JSONObject) json;
                            for (String s : addon) {
                                if (s.equalsIgnoreCase(obj.getJSONObject("addon").getStr("name")))
                                    return true;
                            }
                            return false;
                        }
                        return true;
                    }).forEach(json -> {
                        JSONObject obj = (JSONObject) json;
                        forwardMessage.add(getBot(),
                            new MessageBuilder()
                                .plus("查找结果: " + obj.getStr("title"))
                                .plus("该条描述: " + obj.getStr("description"))
                                .plus("该条类型: " + obj.getStr("syntax_type"))
                                .plus("语法: " + obj.getStr("syntax_pattern"))
                                .plus(" ")
                                .plus("需要本体|附属: " + obj.getJSONObject("addon").getStr("name") + "-v" + obj.getStr("compatible_addon_version"))
                                .plus("附属下载链接: " + obj.getJSONObject("addon").getStr("link_to_addon"))
                                .plus(!obj.getJSONArray("required_plugins").isEmpty(), "需要插件: " + obj.getJSONArray("required_plugins").toString())
                                .plus(!ValueUtil.isEmpty(obj.getStr("compatible_minecraft_version")), "限制MC版本: " + obj.getStr("compatible_minecraft_version"))
                                .plus(" ")
                                .plus("返回类型: " + obj.getStr("return_type"))
                                .plus("event".equalsIgnoreCase(obj.getStr("syntax_type")), "Event-Values: " + obj.getStr("event_values"))
                                .plus("event".equalsIgnoreCase(obj.getStr("syntax_type")), "可否取消: " + (obj.getBool("event_cancellable") ? "可取消" : "不可取消"))
                                .toMessage()
                        );
                    });
                    if (forwardMessage.isEmpty()) {
                        source.sendMessage("未找到任何结果");
                        return;
                    }
                    source.sendMessage(forwardMessage.build());
                }
            }, () -> {
                new CommandMenu(label)
                    .add("search <Key> [addon:xx,xx2,xx3], [type:effect|expression|...]", "在Skript中搜索")
                    .send(source);
            });
        }, "sk", "skript", "skriptHub");
    }
}
