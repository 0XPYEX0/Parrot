package me.xpyex.plugin.parrot.mirai.modulecode.git;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GitInfo {
    private SupportedGits type;
    private String repo;
    private boolean uploadFile = true;

    public enum SupportedGits {
        GitHub,
        Gitee
    }
}
