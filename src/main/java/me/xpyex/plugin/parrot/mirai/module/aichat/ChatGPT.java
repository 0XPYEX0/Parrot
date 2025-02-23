package me.xpyex.plugin.parrot.mirai.module.aichat;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.WeakHashMap;
import lombok.experimental.ExtensionMethod;
import me.xpyex.plugin.parrot.api.CommandMenu;
import me.xpyex.plugin.parrot.api.MessageBuilder;
import me.xpyex.plugin.parrot.mirai.core.command.CommandArguments;
import me.xpyex.plugin.parrot.mirai.core.command.CommandExecutor;
import me.xpyex.plugin.parrot.mirai.core.command.CommandNode;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.ArgParser;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.GroupParser;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.StrParser;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.UserParser;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.core.reachable.MiraiContact;
import me.xpyex.plugin.parrot.mirai.module.aichat.message.ChatMessages;
import me.xpyex.plugin.parrot.mirai.module.aichat.message.SingleChatMessage;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.PlainText;

@ExtensionMethod(ArgParser.class)
public final class ChatGPT extends Module {
    private static final WeakHashMap<Long, ChatMessages> CHAT_CACHE = new WeakHashMap<>();
    private static final String DEFAULT_MSG = "";
    private static final String API_VER3 = "";
    private static final String API_KEY3 = "";
    private static final String API_VER4 = "";
    private static final String API_KEY4 = "";
    private static final String DENIED_MSG_3 = "你没有使用 ChatGPT 3.5 模型的权限";
    private static final String DENIED_MSG_4 = "你没有使用 ChatGPT 4 模型的权限";
    private static final HashMap<Long, String> GROUP_RULES = new HashMap<>();
    private static final int MSG_SIZE_LIMIT = 20;

    @Override
    public void register() throws Throwable {
        registerCommand(Contact.class,
            CommandNode.of((source, sender, arguments) -> {
                    new CommandMenu(arguments)
                        .add("talk <Messages>...", "与ChatGPT对话，每次对话保留 " + MSG_SIZE_LIMIT / 2 + " 回合")
                        .add("reset", "开启新话题")
                        .add("reGo", "按照先前的话题重新生成")
                        .add("groupRule", "设定在某个群的System语句")
                        .send(source);
                })
                .permission("ChatGPT.use", MemberPermission.ADMINISTRATOR)
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
                    }).permission("ChatGPT.setGroupRule", MemberPermission.ADMINISTRATOR, "不理你不理你！"),
                    "groupRule")
                .child(CommandNode.of((source, sender, arguments) -> {
                        source.sendMessage("你想聊点什么？😊");
                    }).executableCheckWithArg((source, sender, args) -> {
                        boolean is3 = "talk".equalsIgnoreCase(args.getLabelReverse(0));
                        if (is3 && !sender.hasPerm("ChatGPT.use.3", MemberPermission.ADMINISTRATOR)) {
                            source.sendMessage(DENIED_MSG_3);
                            return false;
                        }
                        if (!is3 && !sender.hasPerm("ChatGPT.use.4")) {  //调用GPT4且无使用权限，则拦截
                            source.sendMessage(DENIED_MSG_4);
                            return false;
                        }
                        return true;
                    }).notMatchedArg(new CommandExecutor<>() {
                        @Override
                        public void execute(MiraiContact<Contact> source, MiraiContact<User> sender, CommandArguments arguments) throws Throwable {
                            boolean is3 = "talk".equalsIgnoreCase(arguments.getLabelReverse(0));
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
                                builder.add(SingleChatMessage.Role.user == obj.getRole() ? sender.getContact() : getBot(), new PlainText(obj.getContent()));
                            }
                            builder.add(getBot(), new PlainText(talkToGPT(sender.getId(), is3 ? API_VER3 : API_VER4, is3 ? API_KEY3 : API_KEY4)));
                            source.sendMessage(builder.build());
                        }
                    })
                    , "talk", "talk4")
                .child(CommandNode.of((source, sender, arguments) -> {
                    boolean is3 = "reGo".equalsIgnoreCase(arguments.getLabelReverse(0));
                    ChatMessages chatMessages = CHAT_CACHE.get(sender.getId());  //获取其缓存
                    chatMessages.getMessage().remove(chatMessages.getMessage().size() - 1);  //清除最终的缓存

                    ForwardMessageBuilder builder = new ForwardMessageBuilder(source.getContact());
                    for (int i = 1; i < chatMessages.getMessage().size(); i++) {
                        SingleChatMessage message = chatMessages.getMessage().get(i);
                        builder.add(SingleChatMessage.Role.user == message.getRole() ? sender.getContact() : getBot(), new PlainText(message.getContent()));
                    }
                    builder.add(getBot(), new PlainText(talkToGPT(sender.getId(), is3 ? API_VER3 : API_VER4, is3 ? API_KEY3 : API_KEY4)));
                    source.sendMessage(builder.build());
                }).executableCheckWithArg((source, sender, arguments) -> {
                    boolean is3 = "reGo".equalsIgnoreCase(arguments.getLabelReverse(0));
                    if (is3 && !sender.hasPerm("ChatGPT.use.3", MemberPermission.ADMINISTRATOR)) {
                        source.sendMessage(DENIED_MSG_3);
                        return false;
                    }
                    if (!is3 && !sender.hasPerm("ChatGPT.use.4")) {  //调用GPT4且无使用权限，则拦截
                        source.sendMessage(DENIED_MSG_4);
                        return false;
                    }
                    if (!CHAT_CACHE.containsKey(sender.getId())) {
                        source.sendMessage("抱歉，我已经遗忘了与您的对话...");
                        return false;
                    }
                    return true;
                }), "reGo", "reGo4")
            , "ChatGPT", "GPT", "Chat", "ChatBot");

        for (File file : getDataFolder().listFiles()) {
            GROUP_RULES.put(Long.parseLong(file.getName().split("\\.")[0]), Files.readString(file.toPath(), StandardCharsets.UTF_8));
        }
    }

    private String talkToGPT(long id, String apiHost, String apiKey) {
        //若还没有聊过天，则新建缓存
        CHAT_CACHE.putIfAbsent(id, ChatMessages.of(SingleChatMessage.Role.system, GROUP_RULES.getOrDefault(id, DEFAULT_MSG).replace("<USER_NAME>", UserParser.class.of().parse(id).map(User::getNick).orElse("null"))));
        try {
            ChatMessages chatMessages = CHAT_CACHE.get(id);  //获取其缓存

            while (chatMessages.getMessage().size() >= MSG_SIZE_LIMIT + 1) {  //只保留指定回合的对话，第一条为System
                chatMessages.getMessage().remove(1);  //0是System语句，无需移除。从1开始是对话语句
            }

            String result = HttpUtil.createPost(apiHost)
                                .header("Content-Type", "application/json")
                                .header("api-key", apiKey)
                                .body(info(JSONUtil.toJsonPrettyStr(
                                    AIRequest.of().setTemperature(1.65f)
                                        .setTop_p(0.95f)
                                        .setMessages(chatMessages))))
                                .execute()
                                .body();
            @SuppressWarnings("all")
            JSONObject jsonResult = new JSONObject(info(result));
            if (jsonResult.containsKey("error")) {
                chatMessages.getMessage().remove(chatMessages.getMessage().size() - 1);
                return new MessageBuilder().plus("GPT拒绝回答: " + jsonResult.getJSONObject("error").getStr("message"))
                           .plus("")
                           .plus("你触犯的规则类别: " + jsonResult.getJSONObject("error").getJSONObject("innererror").getStr("code"))
                           .plus("")
                           .plus("你的上一个提问已被清除")
                           .toString();
            }
            AIResponse response = JSONUtil.toBean(result, AIResponse.class);
            String gptSaid = response.getChoices()
                                 .get(0)
                                 .getMessage()
                                 .getContent();
            if (gptSaid.trim().endsWith("<STOP_HERE>")) {
                CHAT_CACHE.remove(id);
                return gptSaid.replace("<STOP_HERE>", "\n\n我想我们需要换个新话题了\n先前的对话记录已清除");
            }
            chatMessages.plus(SingleChatMessage.Role.assistant, gptSaid);
            return gptSaid;
        } catch (IORuntimeException e) {
            handleException(e, true, null);
            return "网络异常，访问失败: " + e;
        }
    }
}
