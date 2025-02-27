package me.xpyex.plugin.parrot.aichat.message;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.xpyex.plugin.parrot.aichat.tool.ResponseTool;

@Getter
@Setter
@Accessors(chain = true)
public class SingleChatMessage {
    private Role role;
    private String content;
    private String reasoning_content;

    private List<ResponseTool> tool_calls;
    private String tool_call_id;

    public static SingleChatMessage of(Role role, String content) {
        return new SingleChatMessage().setRole(role).setContent(content);
    }

    public enum Role {
        tool,
        system,
        user,
        assistant
    }
}
