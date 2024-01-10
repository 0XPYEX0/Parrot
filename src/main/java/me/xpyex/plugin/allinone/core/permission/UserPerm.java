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

@Data
@Accessors(chain = true)
public class UserPerm implements Perms {
    private long id;
    private ArrayList<String> extendsGroups = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private ArrayList<String> denyPerms = new ArrayList<>();
    private boolean hasAllPerms = false;

    public UserPerm(long id) {
        this.id = id;
        for (GroupPerm groupPerm : PermManager.GROUPS.values()) {
            if (groupPerm.isDefaultGroup()) {
                extendsGroups.add(groupPerm.getName());
            }
        }
    }

    public boolean hasAllPerms() {
        return hasAllPerms;
        //
    }

    private boolean getHasAllPerms() {
        return hasAllPerms();
        // For JavaBean
        // private for hasAllPerms()
    }

    @Override
    @SneakyThrows
    public void save() {
        File f = new File(Model.getModel(PermManager.class).getDataFolder(), "Users/" + getId() + ".json");
        FileUtil.writeFile(f, JSONUtil.toJsonPrettyStr(this));
    }
}
