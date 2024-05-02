package me.xpyex.plugin.parrot.mirai.modulecode.git;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

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
}
