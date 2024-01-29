package me.xpyex.plugin.allinone.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class FileUtil {
    public static void createNewFile(File target, boolean replaced) throws IOException {
        if (replaced) {
            target.delete();
        }
        while (!target.exists()) {
            Throwable ex = null;
            try {
                target.createNewFile();
            } catch (Throwable e) {
                ex = e;  //当没权限，或者文件所在的目录还未创建
            }
            File parent = target;  //先定义文件自身
            while (ex != null) {  //此时文件尚未被创建，故尝试创建父文件夹
                ValueUtil.mustTrue("无法创建文件 " + target.getPath() + " ,原因可能是Java没有访问权限", parent != null);
                parent = parent.getParentFile();  //寻找所在目录
                try {
                    parent.mkdirs();  //尝试创建所在目录及其所有父目录
                    ex = null;  //打破第二个while
                } catch (Throwable e) {
                    ex = e;  //重启第二个while
                }
            }
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
        Scanner in = new Scanner(target, StandardCharsets.UTF_8);
        StringBuilder builder = new StringBuilder();
        while (in.hasNextLine()) {
            builder.append(in.nextLine()).append("\n");
        }
        return builder.toString();
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
