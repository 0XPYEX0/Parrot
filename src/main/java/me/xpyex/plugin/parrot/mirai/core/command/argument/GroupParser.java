package me.xpyex.plugin.parrot.mirai.core.command.argument;

import java.util.Optional;
import me.xpyex.plugin.parrot.mirai.api.TryCallable;
import me.xpyex.plugin.parrot.mirai.utils.Util;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import org.jetbrains.annotations.NotNull;

public class GroupParser extends ContactParser {
    @NotNull
    @Override
    public Optional<Group> parse(String arg) {
        if ("this".equalsIgnoreCase(arg)) {
            if (getParseObj() instanceof Member member) {
                return Optional.of(member.getGroup());
            }
        }
        String strID = arg.replaceAll("[^0-9]", "");
        if (strID.trim().isEmpty()) return Optional.empty();
        return parse(Long.parseLong(strID));
    }

    @NotNull
    @Override
    public Optional<Group> parse(TryCallable<String> callable) {
        return parse(callable, Group.class);
        //
    }

    public Optional<Group> parse(long id) {
        return Optional.ofNullable(Util.getBot().getGroup(id));
        //
    }
}
