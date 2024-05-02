package me.xpyex.plugin.parrot.mirai.core.service;

import kotlin.coroutines.Continuation;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.overflow.spi.FileService;

public class ParrotFileService implements FileService {
    private final int priority = 900;
    @Nullable
    @Override
    public Object upload(@NotNull ExternalResource externalResource, @NotNull Continuation<? super String> continuation) {
        return null;
    }
}
