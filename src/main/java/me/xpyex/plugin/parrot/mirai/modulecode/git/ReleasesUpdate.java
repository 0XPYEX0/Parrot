package me.xpyex.plugin.parrot.mirai.modulecode.git;

import cn.hutool.json.JSONUtil;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.xpyex.plugin.parrot.mirai.utils.FileUtil;

@Data
public class ReleasesUpdate {
    @Setter
    @Getter
    private static ReleasesUpdate instance;
    private Map<Long, Set<GitInfo>> Groups = new HashMap<>();  //群号, <GitHub, Owner/RepoName>
    private Map<Long, Set<GitInfo>> Users = new HashMap<>();
    private Map<String, String> Cache = new HashMap<>();  //Repo, Version(R-ver|P-ID)

    public ReleasesUpdate() {
        instance = this;
        //
    }

    @SneakyThrows
    public void save(File file) {
        FileUtil.writeFile(file, JSONUtil.toJsonPrettyStr(this));
        //
    }
}
