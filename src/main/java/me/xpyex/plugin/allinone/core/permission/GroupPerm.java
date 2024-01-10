package me.xpyex.plugin.allinone.core.permission;

import cn.hutool.json.JSONUtil;
import java.io.File;
import java.util.ArrayList;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import me.xpyex.plugin.allinone.core.Model;
import me.xpyex.plugin.allinone.model.core.PermManager;
import me.xpyex.plugin.allinone.utils.FileUtil;

@Accessors(chain = true)
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

    @Override
    @SneakyThrows
    public void save() {
        File f = new File(Model.getModel(PermManager.class).getDataFolder(), "Groups/" + name + ".json");
        FileUtil.writeFile(f, JSONUtil.toJsonPrettyStr(this));
    }
}
