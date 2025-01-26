package me.xpyex.plugin.parrot.mirai.module.aichat.message;

import cn.hutool.json.JSONObject;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class SingleChatMessage {
    private Role role;
    private String content;
    private String reasoning_content;
    private List<JSONObject> tool_calls;

    public static SingleChatMessage of(Role role, String content) {
        return new SingleChatMessage().setRole(role).setContent(content);
    }

    public enum Role {
        system,
        user,
        assistant
    }
}
