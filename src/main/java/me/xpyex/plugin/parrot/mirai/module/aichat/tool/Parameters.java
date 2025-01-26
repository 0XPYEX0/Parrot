package me.xpyex.plugin.parrot.mirai.module.aichat.tool;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class Parameters {
    private String type = "object";
    private Map<String, ParamProperties> properties;
}
