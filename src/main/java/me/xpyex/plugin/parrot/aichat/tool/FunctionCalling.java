package me.xpyex.plugin.parrot.aichat.tool;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class FunctionCalling {
    private String name;
    private String description;
    private Parameters parameters;

    public static FunctionCalling of() {
        return new FunctionCalling();
    }
}
