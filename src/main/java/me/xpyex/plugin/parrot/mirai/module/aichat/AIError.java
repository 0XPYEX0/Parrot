package me.xpyex.plugin.parrot.mirai.module.aichat;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(chain = true)
public class AIError {
    private String message;
    private String code;
    private String type;
}
