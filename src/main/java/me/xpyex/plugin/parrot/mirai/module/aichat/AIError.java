package me.xpyex.plugin.parrot.mirai.module.aichat;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class AIError {
    private String message;
    private String code;
    private String type;
    private Object param;
}
