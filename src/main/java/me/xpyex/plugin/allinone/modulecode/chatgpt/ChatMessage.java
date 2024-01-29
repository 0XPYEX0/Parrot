package me.xpyex.plugin.allinone.modulecode.chatgpt;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ChatMessage {
    private final JSONArray msg = new JSONArray();

    @NotNull
    @Contract(" -> new")
    public static ChatMessage of() {
        return new ChatMessage();
        //
    }

    @NotNull
    @Contract("_, _ -> new")
    public static ChatMessage of(Role role, String msg) {
        return of().plus(role, msg);
        //
    }

    @NotNull
    public ChatMessage plus(Role role, String msg) {
        getMessage().add(new JSONObject()
                             .set("role", role.name().toLowerCase())
                             .set("content", msg));
        return this;
    }

    public JSONArray getMessage() {
        return msg;
        //
    }

    public enum Role {
        SYSTEM,
        USER,
        ASSISTANT
    }
}
