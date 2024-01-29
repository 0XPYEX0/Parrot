package me.xpyex.plugin.allinone.modulecode.git;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class ReleasesUpdate {
    private Map<Long, List<GitInfo>> Groups = new HashMap<>();  //群号, <GitHub, Owner/RepoName>
    private Map<Long, List<GitInfo>> Users = new HashMap<>();
    private Map<String, String> Cache = new HashMap<>();  //Repo, Version(R-ver|P-ID)
    @Setter
    @Getter
    private static ReleasesUpdate instance;

    public ReleasesUpdate() {
        instance = this;
    }
}
