package me.xpyex.plugin.parrot.mirai.core.reachable;

import me.xpyex.plugin.parrot.reachable.ParrotUser;
import net.mamoe.mirai.contact.User;

public class MiraiUser extends ParrotUser<User> {
    public MiraiUser(User handle, long id) {
        super(handle, id);
    }

    @Override
    public void sendMessage(String message) {

    }

    @Override
    public boolean hasPerm(String s) {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }
}
