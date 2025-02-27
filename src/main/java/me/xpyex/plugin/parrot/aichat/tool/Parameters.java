package me.xpyex.plugin.parrot.aichat.tool;

import java.util.HashMap;
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

    public static Parameters of() {
        return new Parameters();
    }

    public Parameters addProperty(String key, ParamProperties value) {
        if (properties == null) properties = new HashMap<>();

        properties.put(key, value);
        return this;
    }
}
