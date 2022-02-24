package me.xpyex.plugin.allinone.functions.music;

@FunctionalInterface
public interface MusicSource {
    MusicInfo get(final String p0) throws Exception;
}
