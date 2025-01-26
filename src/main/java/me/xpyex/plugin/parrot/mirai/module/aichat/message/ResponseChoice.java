package me.xpyex.plugin.parrot.mirai.module.aichat.message;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ResponseChoice {
    private FinishReason finish_reason;
    private int index;
    private SingleChatMessage message;


    public enum FinishReason {
        stop,
        length,
        content_filter,
        insufficient_system_resource
    }
}
