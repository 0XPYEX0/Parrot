package me.xpyex.plugin.parrot.mirai.modulecode.rcon;

import lombok.Data;

/**
 * Rcon数据包
 */
@Data(staticConstructor = "of")
public class RconPacket {
    private final int requestId;
    private final int type;
    private final String message;

    public static class Login extends RconPacket {
        public Login(int requestId, String password) {
            super(requestId, 3, password);
        }
    }

    public static class Command extends RconPacket {
        public Command(int requestId, String command) {
            super(requestId, 2, command);
        }
    }
}
