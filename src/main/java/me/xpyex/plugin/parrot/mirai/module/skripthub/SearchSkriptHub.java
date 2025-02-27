package me.xpyex.plugin.parrot.mirai.module.skripthub;

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
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import lombok.experimental.ExtensionMethod;
import me.xpyex.plugin.parrot.api.CommandMenu;
import me.xpyex.plugin.parrot.mirai.core.command.CommandNode;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.ArgParser;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.aichat.AIRequest;
import me.xpyex.plugin.parrot.utils.StringUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;

@ExtensionMethod(ArgParser.class)
public class SearchSkriptHub extends Module {
    private static JSONArray syntaxList;

    static {
        AIRequest.getFunctions().put("searchSkriptHub", json -> {
            List<String> keyWord = json.getJSONArray("keyWords").toList(String.class);
            List<String> addon = json.getJSONArray("addon").toList(String.class);
            List<String> type = json.getJSONArray("type").toList(String.class);
            return searchDoc(keyWord, type, addon);
        });
    }

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
        registerCommand(Contact.class,
            CommandNode.of((source, sender, arguments) -> {
                    new CommandMenu(arguments)
                        .add("search <Key> [addon:xx,xx2,xx3], [type:effect|expression|...]", "在SkriptHub中搜索")
                        .send(source);
                })
                .executableCheck((source, sender) -> {
                    if (!sender.hasPerm(getName() + ".use")) {
                        source.sendMessage("缺少权限节点: " + getName() + ".use");
                        return false;
                    }
                    return true;
                })
                .child(CommandNode.of((source, sender, arguments) -> {
                    List<String> keyWords = Arrays.stream(arguments.getArguments())
                                               .filter(s -> !StringUtil.startsWithIgnoreCaseOr(s, "addon:"))
                                               .filter(s -> !StringUtil.startsWithIgnoreCaseOr(s, "type:"))
                                               .toList();
                    ArrayList<String> addon = new ArrayList<>();
                    ArrayList<String> type = new ArrayList<>();
                    for (String s : arguments.getArguments()) {
                        if (StringUtil.startsWithIgnoreCaseOr(s, "addon:")) {
                            addon.addAll(List.of(s.substring(6).split(",")));
                        } else if (StringUtil.startsWithIgnoreCaseOr(s, "type:")) {
                            type.addAll(List.of(s.substring(5).split(",")));
                        }
                    }
                    if (keyWords.isEmpty()) {
                        source.sendMessage("关键词为空");
                        return;
                    }
                    ForwardMessageBuilder forwardMessage = new ForwardMessageBuilder(source.getContact());
                    searchDoc(keyWords, type, addon).forEach(json -> {
                        try {
                            File tmpFile = File.createTempFile("SkriptHub/" + json.getStr("title"), ".png");
                            ImageIO.write(new AwtSkriptDocBuilder().syntax(json).build(), "png", tmpFile);
                            ExternalResource resource = ExternalResource.create(tmpFile);
                            forwardMessage.add(getBot(), source.getContact().uploadImage(resource));
                            resource.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    if (forwardMessage.isEmpty()) {
                        source.sendMessage("未找到任何结果");
                        return;
                    }
                    source.sendMessage(forwardMessage.build());
                }), "search")
                .child(CommandNode.of((source, sender, arguments) -> {
                    source.sendMessage("重新同步SkriptHub Doc中");
                    downloadDocAndSave();
                    source.sendMessage("已完成");
                }).permission(getName() + ".update", MemberPermission.ADMINISTRATOR), "update")
            , "sk", "skript", "skriptHub");
    }

    @NotNull
    private static List<JSONObject> searchDoc(List<String> keyWords, List<String> type, List<String> addon) {
        return syntaxList.stream()
                   .filter(json -> {  //筛选关键词
                       if (keyWords == null) return false;  //未设置关键词时直接返回未找到，不进行查找
                       if (json instanceof JSONObject obj) {
                           if (StringUtil.containsIgnoreCaseOr(obj.getStr("title"), keyWords) || StringUtil.containsIgnoreCaseOr(obj.getStr("description"), keyWords)) {
                               return true;
                           }
                           if (StringUtil.containsIgnoreCaseOr(obj.getStr("syntax_pattern").replaceAll("\\(\\)\\[]", ""), keyWords)) {
                               return true;
                           }
                       }
                       return false;
                   })
                   .filter(json -> {  //筛选语句类型
                       if (type != null && !type.isEmpty()) {
                           JSONObject obj = (JSONObject) json;
                           for (String s : type) {
                               if (s.equalsIgnoreCase(obj.getStr("syntax_type")))
                                   return true;
                           }
                           return false;
                       }
                       return true;
                   })
                   .filter(json -> {  //筛选附属
                       if (addon != null && !addon.isEmpty()) {
                           JSONObject obj = (JSONObject) json;
                           for (String s : addon) {
                               if (s.equalsIgnoreCase(obj.getJSONObject("addon").getStr("name")))
                                   return true;
                           }
                           return false;
                       }
                       return true;
                   })
                   .map(obj -> (JSONObject) obj)
                   .collect(Collectors.toList());
    }
}
