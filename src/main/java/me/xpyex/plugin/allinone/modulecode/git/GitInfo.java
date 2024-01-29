package me.xpyex.plugin.allinone.modulecode.git;

import lombok.Data;

@Data
public class GitInfo {
    private SupportedGits type;
    private String repo;

    public GitInfo() {

    }

    public GitInfo(SupportedGits type, String repo) {
        this.type = type;
        this.repo = repo;
    }

    public enum SupportedGits {
        GitHub,
        Gitee
    }
}
