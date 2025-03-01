package me.xpyex.plugin.parrot.aichat;

import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.xpyex.plugin.parrot.aichat.message.ChatMessages;
import me.xpyex.plugin.parrot.aichat.message.SingleChatMessage;
import me.xpyex.plugin.parrot.aichat.tool.RequestTool;
import me.xpyex.plugin.parrot.aichat.tool.ResponseTool;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.module.aichat.DeepSeek;

@Getter
@Setter
@Accessors(chain = true)
public class AIRequest implements Cloneable {
    @Getter
    private static final HashMap<String, Function<JSONObject, Object>> functions = new HashMap<>();
    private List<SingleChatMessage> messages;
    private String model;
    private float frequency_penalty = 0;
    private int max_tokens = 4096;
    private float presence_penalty = 0;
    private Map<String, String> response_format;
    private List<String> stop;
    private float temperature = 1;
    private float top_p = 1;
    private Set<RequestTool> tools;
    private ToolChoice tool_choice = null;

    public static AIRequest of() {
        return new AIRequest();
    }

    public AIResponse getResponse(String url, String apiKey) {
        String result = HttpUtil.createPost(url)
                            .contentType(ContentType.JSON.getValue())
                            .header(url.toLowerCase().contains("deepSeek".toLowerCase())
                                        ? Header.AUTHORIZATION.getValue()
                                        : "api-key"
                                , apiKey
                            )
                            .body(Module.getModule(DeepSeek.class).info(toString()))
                            .execute()
                            .body();
        Module.getModule(DeepSeek.class).info("已获取返回结果: " + result);
        AIResponse response = AIResponse.parseToResponse(result);
        List<ResponseTool> toolCalls = response.getChoices().get(0).getMessage().getTool_calls();
        if (toolCalls == null || toolCalls.isEmpty()) return response;

        AIRequest newRequest = clone();
        newRequest.getMessages().add(SingleChatMessage.of(SingleChatMessage.Role.assistant, "").setTool_calls(toolCalls));
        toolCalls.stream()
            .filter(tool -> functions.containsKey(tool.getFunction().getName()))
            .forEach(tool -> {
                String toolId = tool.getId();
                Object funcOut = functions.get(tool.getFunction().getName()).apply(tool.getFunction().parseArguments());
                newRequest.getMessages().add(SingleChatMessage.of(SingleChatMessage.Role.tool, funcOut + "").setTool_call_id(toolId));
            });
        newRequest.setTool_choice(ToolChoice.none);
        return newRequest.getResponse(url, apiKey);
    }

    public AIRequest setMessages(ChatMessages messages) {
        return setMessages(messages.getMessage());
    }

    public AIRequest setMessages(List<SingleChatMessage> messages) {
        this.messages = messages;
        return this;
    }

    @Override
    public String toString() {
        JSONObject out = JSONUtil.parseObj(this);
        if ("DeepSeek-Reasoner".equalsIgnoreCase(getModel())) out.remove("tools");
        return out.toString();
    }

    public String toStringPretty() {
        return JSONUtil.toJsonPrettyStr(this);
    }

    @Override
    public AIRequest clone() {
        try {
            AIRequest clone = (AIRequest) super.clone();
            // TODO: 复制此处的可变状态，这样此克隆就不能更改初始克隆的内部项
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public enum ToolChoice {
        none,
        auto,
        required
    }

    public AIRequest addTool(RequestTool tool, Function<JSONObject, Object> function) {
        if (getTools() == null) setTools(new HashSet<>());
        if (model != null && "DeepSeek-Reasoner".equalsIgnoreCase(getModel())) return this;  //reasoner不允许调用方法

        getTools().add(tool);
        functions.put(tool.getFunction().getName(), function);
        return this;
    }
}
