package me.xpyex.plugin.parrot.mirai.module.aichat.message;

import java.util.ArrayList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ChatMessages {
    private final ArrayList<SingleChatMessage> msg = new ArrayList<>();

    @NotNull
    @Contract(" -> new")
    public static ChatMessages of() {
        return new ChatMessages();
        //
    }

    @NotNull
    @Contract("_, _ -> new")
    public static ChatMessages of(Role role, String msg) {
        return of().plus(role, msg);
        //
    }

    @NotNull
    public ChatMessages plus(Role role, String msg) {
        getMessage().add(SingleChatMessage.of(role, msg));
        return this;
    }

    public ArrayList<SingleChatMessage> getMessage() {
        return msg;
        //
    }

    public enum Role {
        SYSTEM,
        USER,
        ASSISTANT
    }
}
