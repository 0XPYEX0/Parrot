package me.xpyex.plugin.parrot.mirai.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileUtil {
    public static void createNewFile(File target, boolean replaced) throws IOException {
        if (replaced) {
            target.delete();
        }
        if (!target.exists()) {
            File parent = target.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            } else if (parent.isFile()) {
                throw new IllegalStateException("文件目录非法: parent为文件");
            }
            target.createNewFile();
        }
    }

    /**
     * 读取目标文本文件
     *
     * @param target 目标文本文件
     * @return 目标文件的文本
     * @throws IOException 文件异常
     */
    public static String readFile(File target) throws IOException {
        return Files.readString(target.toPath());
    }

    /**
     * 向目标文件写出文本
     *
     * @param target  目标文本
     * @param content 要写出的内容
     * @param attend  是否在原文本的内容基础续写新文本，否则覆写整个文件
     * @throws IOException 文件异常
     */
    public static void writeFile(File target, String content, boolean attend) throws IOException {
        if (!target.exists()) {
            createNewFile(target, false);
        }
        PrintWriter out = new PrintWriter(target, StandardCharsets.UTF_8);
        if (attend) {
            out.println(content);
        } else {
            out.write(content);
        }
        out.flush();
        out.close();
    }

    /**
     * 覆写目标文件
     *
     * @param target  目标文件
     * @param content 覆写的内容
     * @throws IOException 文件异常
     */
    public static void writeFile(File target, String content) throws IOException {
        writeFile(target, content, false);
    }
}
