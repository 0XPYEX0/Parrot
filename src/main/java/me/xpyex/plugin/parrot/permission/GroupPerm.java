package me.xpyex.plugin.parrot.permission;

import cn.hutool.json.JSONUtil;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.TreeSet;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.module.core.PermManager;

@Accessors(chain = true)
@Data
public class GroupPerm implements Perms {
    private String name;
    private TreeSet<String> permissions = new TreeSet<>();
    private TreeSet<String> denyPerms = new TreeSet<>();
    private boolean isDefaultGroup = false;

    public GroupPerm(String name) {
        this.name = name;
        //
    }

    @Override
    @SneakyThrows
    public void save() {
        File f = new File(Module.getModule(PermManager.class).getDataFolder(), "Groups/" + name + ".json");
        String content = JSONUtil.toJsonPrettyStr(this);
        Files.writeString(f.toPath(), content, StandardCharsets.UTF_8);
    }
}
