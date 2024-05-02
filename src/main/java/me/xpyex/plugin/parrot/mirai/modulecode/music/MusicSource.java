package me.xpyex.plugin.parrot.mirai.modulecode.music;

@FunctionalInterface
public interface MusicSource {
    MusicInfo get(final String p0) throws Exception;
}
