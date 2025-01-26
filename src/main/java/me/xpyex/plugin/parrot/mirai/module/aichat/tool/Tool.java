package me.xpyex.plugin.parrot.mirai.module.aichat.tool;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class Tool {
    private String type = "function";
    private FunctionCalling function;
}
