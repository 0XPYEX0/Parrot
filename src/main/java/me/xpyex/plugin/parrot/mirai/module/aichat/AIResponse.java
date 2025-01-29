package me.xpyex.plugin.parrot.mirai.module.aichat;

import java.util.List;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.xpyex.plugin.parrot.mirai.module.aichat.message.ResponseChoice;

@Getter
@Accessors(chain = true)
public class AIResponse {
    private String id;  //对话UUID
    private List<ResponseChoice> choices;  //答复
    private long created;  //对话创建时间
    private String model;  //模型
    private AIError error;

    public static AIResponse of() {
        return new AIResponse();
    }
}
