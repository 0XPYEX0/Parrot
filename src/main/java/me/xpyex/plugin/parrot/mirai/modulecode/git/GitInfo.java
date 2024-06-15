package me.xpyex.plugin.parrot.mirai.modulecode.git;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GitInfo {
    private SupportedGits type = SupportedGits.UNKNOWN;
    private String repo = "";
    private boolean uploadFile = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitInfo gitInfo = (GitInfo) o;
        return type == gitInfo.type &&
                   repo.equals(gitInfo.repo) &&
                   uploadFile == gitInfo.uploadFile
            ;
    }

    public enum SupportedGits {
        GitHub,
        Gitee,
        UNKNOWN
    }
}
