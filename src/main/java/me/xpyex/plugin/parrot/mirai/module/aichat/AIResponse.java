package me.xpyex.plugin.parrot.mirai.module.aichat;

import cn.hutool.json.JSONUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import me.xpyex.plugin.parrot.mirai.module.aichat.message.ResponseChoice;

@Getter
@Setter
@Accessors(chain = true)
public class AIResponse {
    private String id = null;  //对话UUID
    private List<ResponseChoice> choices = new ArrayList<>();  //答复
    private long created = -1;  //对话创建时间
    private String model = null;  //模型
    private AIError error = null;

    public static AIResponse of() {
        return new AIResponse();
    }

    @SneakyThrows
    public static AIResponse parseToResponse(String json) {
        return JSONUtil.toBean(json, AIResponse.class);
    }
}
