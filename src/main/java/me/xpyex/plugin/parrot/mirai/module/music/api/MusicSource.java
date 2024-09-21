package me.xpyex.plugin.parrot.mirai.module.music.api;

@FunctionalInterface
public interface MusicSource {
    MusicInfo get(final String p0) throws Exception;
}
