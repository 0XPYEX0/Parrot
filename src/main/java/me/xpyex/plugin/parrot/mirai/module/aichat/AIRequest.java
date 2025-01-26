package me.xpyex.plugin.parrot.mirai.module.aichat;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.xpyex.plugin.parrot.mirai.module.aichat.message.ChatMessages;
import me.xpyex.plugin.parrot.mirai.module.aichat.tool.Tool;

@Getter
@Setter
@Accessors(chain = true)
public class AIRequest {
    private ChatMessages messages;
    private String model;
    private float frequency_penalty = 0;
    private int max_tokens = 4096;
    private float presence_penalty = 0;
    private Map<String, String> response_format;
    private List<String> stop;
    private float temperature = 1;
    private float top_p = 1;
    private List<Tool> tools;
    private ToolChoice tool_choice;

    public static AIRequest of() {
        return new AIRequest();
    }

    public enum ToolChoice {
        none,
        auto,
        required
    }
}
