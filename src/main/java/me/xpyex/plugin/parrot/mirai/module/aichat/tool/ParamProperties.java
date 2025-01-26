package me.xpyex.plugin.parrot.mirai.module.aichat.tool;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ParamProperties {
    private String type = "string";
    private String description = "参数描述";
}
