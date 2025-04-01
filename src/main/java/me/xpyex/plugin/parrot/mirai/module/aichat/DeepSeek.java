package me.xpyex.plugin.parrot.mirai.module.aichat;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.lang.Pair;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;
import lombok.experimental.ExtensionMethod;
import me.xpyex.plugin.parrot.aichat.AIRequest;
import me.xpyex.plugin.parrot.aichat.AIResponse;
import me.xpyex.plugin.parrot.api.CommandMenu;
import me.xpyex.plugin.parrot.mirai.core.command.CommandArguments;
import me.xpyex.plugin.parrot.mirai.core.command.CommandExecutor;
import me.xpyex.plugin.parrot.mirai.core.command.CommandNode;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.ArgParser;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.GroupParser;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.StrParser;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.UserParser;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.core.reachable.MiraiContact;
import me.xpyex.plugin.parrot.aichat.message.ChatMessages;
import me.xpyex.plugin.parrot.aichat.message.SingleChatMessage;
import me.xpyex.plugin.parrot.aichat.tool.FunctionCalling;
import me.xpyex.plugin.parrot.aichat.tool.ParamProperties;
import me.xpyex.plugin.parrot.aichat.tool.Parameters;
import me.xpyex.plugin.parrot.aichat.tool.RequestTool;
import me.xpyex.plugin.parrot.mirai.module.skripthub.SearchSkriptHub;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.PlainText;

@ExtensionMethod(ArgParser.class)
public class DeepSeek extends Module {
    private static final String DEFAULT_MSG = "";
    private static final String API_KEY = "";
    private static final SimpleDateFormat SECOND_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final HashMap<Long, String> GROUP_RULES = new HashMap<>();
    private static final WeakHashMap<Long, ChatMessages> CHAT_CACHE = new WeakHashMap<>();
    private static final int MAX_TALK_COUNT = 60;

    @Override
    public void register() throws Throwable {
        registerCommand(Contact.class,
            CommandNode.of(arguments ->
                               new CommandMenu(arguments)
                                   .add("chat <Content...>", "和DeepSeek-Chat-v3模型对话，每次对话保留" + MAX_TALK_COUNT / 2 + " 回合")
                                   .add("reasoner <Content...>", "和DeepSeek-Reasoner模型对话，每次对话保留" + MAX_TALK_COUNT / 2 + " 回合")
                                   .add("reset", "开启新话题")
                                   .add("reChat", "按照先前的话题，指定DeepSeek-Chat-v3模型重新生成")
                                   .add("reReasoner", "按照先前的话题，指定DeepSeek-Reasoner模型重新生成")
                                   .add("groupRule", "设定在某个群的System语句")
                )
                .child(CommandNode.of((source, sender, arguments) -> source.sendMessage("你想聊些什么？😊"))
                           .notMatchedArg(new CommandExecutor<>() {
                               @Override
                               public void execute(MiraiContact<Contact> source, MiraiContact<User> sender, CommandArguments arguments) throws Throwable {
                                   if (source.isGroup() && source.getContactAsGroup().getBotPermission().getLevel() > sender.getContactAsMember().getPermission().getLevel()) {
                                       getEvent(source).ifPresent(msgEvent -> {
                                           recall(msgEvent.getSource());
                                       });
                                   }
                                   //若还没有聊过天，则新建缓存
                                   CHAT_CACHE.putIfAbsent(sender.getId(), ChatMessages.of(SingleChatMessage.Role.system, GROUP_RULES.getOrDefault(source.getId(), DEFAULT_MSG).replace("<USER_NAME>", sender.getName())));
                                   String userMsg = String.join(" ", arguments.getArguments());

                                   ChatMessages chatMessages = CHAT_CACHE.get(sender.getId());  //获取其缓存
                                   chatMessages.plus(SingleChatMessage.Role.user, userMsg);

                                   ForwardMessageBuilder builder = new ForwardMessageBuilder(source.getContact());
                                   for (int i = 1; i < chatMessages.getMessage().size(); i++) {
                                       SingleChatMessage obj = chatMessages.getMessage().get(i);
                                       if (obj.getRole() == SingleChatMessage.Role.tool) continue;
                                       builder.add(
                                           SingleChatMessage.Role.user == obj.getRole() ? sender.getContact() : getBot(),
                                           new PlainText(obj.getContent().isEmpty() ? " " : obj.getContent())
                                       );
                                   }

                                   Pair<String, String> response = talkToDS(sender.getId(), ("DeepSeek-" + arguments.getLabelReverse(0)).toLowerCase());
                                   builder.add(getBot(), new PlainText(response.getKey().isEmpty() ? " " : response.getKey()));
                                   if (response.getValue() != null)
                                       builder.add(getBot(), new PlainText("思绪:\n" + response.getValue()));
                                   source.sendMessage(builder.build());
                               }
                           })
                           .executableCheckWithArg((source, sender, args) -> {
                               if (!sender.hasPerm(getName() + ".use." + args.getLabelReverse(0), args.getLabelReverse(0).equalsIgnoreCase("reasoner") ? null : MemberPermission.ADMINISTRATOR)) {
                                   source.sendMessage("你没有使用DeepSeek-" + args.getLabelReverse(0) + "模型的权限");
                                   return false;
                               }
                               return true;
                           })
                    , "chat", "reasoner")
                .child(CommandNode.of((source, sender, arguments) -> {
                    CHAT_CACHE.remove(sender.getId());
                    source.sendMessage("已清除连续对话记忆");
                }), "reset")
                .child(CommandNode.of((source, sender, arguments) -> {
                    arguments.getArgument(0, GroupParser.class, Group.class).ifPresentOrElse(group -> {
                        File ruleFile = new File(getDataFolder(), group.getId() + ".txt");
                        StrParser.class.of().parse(() -> String.join(" ", Arrays.copyOfRange(arguments.getArguments(), 1, arguments.getArguments().length))).ifPresentOrElse(rule -> {
                            try {
                                Files.writeString(ruleFile.toPath(), rule, StandardCharsets.UTF_8);
                                GROUP_RULES.put(group.getId(), rule);
                                source.sendMessage("已保存规则");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }, () -> {
                            source.sendMessage("未输入具体规则，默认执行清空");
                            GROUP_RULES.remove(group.getId());
                            ruleFile.delete();
                        });
                    }, () -> source.sendMessage("未输入群号"));
                }).permission(getName() + ".setGroupRule", MemberPermission.ADMINISTRATOR, "不理你不理你！"), "groupRule")
                .child(CommandNode.of((source, sender, arguments) -> {
                    ChatMessages chatMessages = CHAT_CACHE.get(sender.getId());  //获取其缓存
                    chatMessages.getMessage().remove(chatMessages.getMessage().size() - 1);  //清除最终的缓存

                    ForwardMessageBuilder builder = new ForwardMessageBuilder(source.getContact());
                    for (int i = 1; i < chatMessages.getMessage().size(); i++) {
                        SingleChatMessage message = chatMessages.getMessage().get(i);
                        builder.add(SingleChatMessage.Role.user == message.getRole() ? sender.getContact() : getBot(), new PlainText(message.getContent()));
                    }
                    Pair<String, String> response = talkToDS(sender.getId(), ("DeepSeek-" + arguments.getLabelReverse(0).substring(2)).toLowerCase());
                    builder.add(getBot(), new PlainText(response.getKey()));
                    if (response.getValue() != null)
                        builder.add(getBot(), new PlainText("思绪:\n" + response.getValue()));
                    source.sendMessage(builder.build());
                }).executableCheckWithArg((source, sender, arguments) -> {
                    if (!sender.hasPerm(getName() + ".use." + arguments.getLabelReverse(0).substring(2), MemberPermission.ADMINISTRATOR)) {
                        source.sendMessage("你没有使用DeepSeek-" + arguments.getLabelReverse(0) + "模型的权限");
                        return false;
                    }
                    if (!CHAT_CACHE.containsKey(sender.getId())) {
                        source.sendMessage("抱歉，我已经遗忘了与您的对话...");
                        return false;
                    }
                    return true;
                }), "reChat", "reReasoner")
            , "deepSeek", "DS", "深度搜索");

        for (File file : getDataFolder().listFiles()) {
            GROUP_RULES.put(Long.parseLong(file.getName().split("\\.")[0]), Files.readString(file.toPath(), StandardCharsets.UTF_8));
        }
    }

    private Pair<String, String> talkToDS(long id, String model) {  //答复, 思考链
        //若还没有聊过天，则新建缓存
        CHAT_CACHE.putIfAbsent(id, ChatMessages.of(SingleChatMessage.Role.system,
            GROUP_RULES.getOrDefault(id, DEFAULT_MSG)
                .replace("<USER_NAME>", UserParser.class.of().parse(id).map(User::getNick).orElse("null"))
        ));
        try {
            ChatMessages chatMessages = CHAT_CACHE.get(id);  //获取其缓存

            while (chatMessages.getMessage().size() >= MAX_TALK_COUNT + 1) {  //只保留指定回合的对话，第一条为System
                chatMessages.getMessage().remove(1);  //0是System语句，无需移除。从1开始是对话语句
            }

            while (chatMessages.getMessage().size() >= 2 && chatMessages.getMessage().get(chatMessages.getMessage().size() - 1).getRole() == chatMessages.getMessage().get(chatMessages.getMessage().size() - 2).getRole()) {
                chatMessages.getMessage().remove(chatMessages.getMessage().size() - 1);  //清除连续的同一角色对话
            }

            AIResponse response =
                AIRequest.of()
                    .setMessages(chatMessages)
                    .setTemperature(1.65f)
                    .setModel(model)
                    .setTop_p(0.95f)
                    .addTool(
                        RequestTool.of()
                            .setFunction(
                                FunctionCalling.of()
                                    .setName("searchSkriptHub")
                                    .setDescription("在SkriptHub中搜索Skript语法。仅当用户让你编写Skript脚本时，你不清楚语法的情况下调用。该方法返回一个JSONArray")
                                    .setParameters(
                                        Parameters.of()
                                            .addProperty("keyWords", ParamProperties.of().setType("array").setDescription("搜索的关键词，应当为String[]数组。不可为null，不可为长度为0的数组"))
                                            .addProperty("type", ParamProperties.of().setType("array").setDescription("限制搜索结果的语句类型，应当为String[]数组，允许的类型有[expression, effect, type, condition, event, section, function, structure]。不允许为null，但可以是长度为0的数组"))
                                            .addProperty("addon", ParamProperties.of().setType("array").setDescription("限制搜索结果的Skript附属插件，应当为String[]数组。不可为null，但可以为长度为0的数组"))
                                    )
                            ),
                        json -> {
                            try {
                                List<String> keyWord = json.getJSONArray("keyWords").toList(String.class);
                                List<String> addon = json.getJSONArray("addon").toList(String.class);
                                List<String> type = json.getJSONArray("type").toList(String.class);
                                return SearchSkriptHub.searchDoc(keyWord, type, addon);
                            } catch (NullPointerException e) {
                                return "参数填写错误，无法搜索";
                            }
                        }
                    )
                    .addTool(
                        RequestTool.of()
                            .setFunction(
                                FunctionCalling.of()
                                    .setName("getCurrentTime")
                                    .setDescription("获取当前的时间，时区为中国(东八区，UTC+8)")
                            ), json -> SECOND_FORMAT.format(new Date())
                    )
                    .getResponse("https://api.deepseek.com/chat/completions", "Bearer " + API_KEY);
            if (response.getError() != null) {
                return Pair.of("""
                    DeepSeek回答时出错: %s
                    错误码: %s
                    错误类型: %s
                    """.formatted(
                    response.getError().getMessage(),
                    response.getError().getCode(),
                    response.getError().getType()
                ), null);
            }
            SingleChatMessage firstChoice = response.getChoices().get(0).getMessage();
            String gptSaid = firstChoice.getContent();
            if (gptSaid.trim().endsWith("<STOP_HERE>")) {
                CHAT_CACHE.remove(id);
                return Pair.of(gptSaid.replace("<STOP_HERE>", "\n\n我想我们需要换个新话题了\n先前的对话记录已清除"), null);
            }
            chatMessages.plus(SingleChatMessage.Role.assistant, gptSaid);
            return Pair.of(gptSaid, firstChoice.getReasoning_content());
        } catch (IORuntimeException e) {
            handleException(e, true, null);
            return Pair.of("网络异常，访问失败: " + e, null);
        }
    }
}
