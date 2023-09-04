package me.xpyex.plugin.allinone.core.permission;

import cn.hutool.json.JSONUtil;
import java.io.File;
import java.util.ArrayList;
import lombok.Data;
import lombok.SneakyThrows;
import me.xpyex.plugin.allinone.core.Model;
import me.xpyex.plugin.allinone.utils.FileUtil;

@Data
public class GroupPerm implements Perms {
    private String name;
    private ArrayList<String> permissions = new ArrayList<>();
    private ArrayList<String> denyPerms = new ArrayList<>();
    private boolean isDefaultGroup = false;

    public GroupPerm(String name) {
        this.name = name;
        //
    }

    public boolean isDefaultGroup() {
        return isDefaultGroup;
        //
    }

    public GroupPerm setDefaultGroup(boolean isDefaultGroup) {
        this.isDefaultGroup = isDefaultGroup;
        return this;
    }

    @Override
    @SneakyThrows
    public void save() {
        File f = new File(Model.getModel("PermManager").getDataFolder(), "Groups/" + name + ".json");
        FileUtil.writeFile(f, JSONUtil.toJsonPrettyStr(this));
    }
}
