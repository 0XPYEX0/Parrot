package me.xpyex.plugin.parrot.mirai.module.aichat.tool;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class RequestTool {
    private final String type = "function";
    private FunctionCalling function;

    public static RequestTool of() {
        return new RequestTool();
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, function);
    }
}
