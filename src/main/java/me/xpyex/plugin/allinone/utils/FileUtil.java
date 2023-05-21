package me.xpyex.plugin.allinone.utils;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

public class FileUtil {
    public static String readFile(File f) {
        try {
            StringBuilder builder = new StringBuilder();
            Scanner scanner = new Scanner(f, "UTF-8");
            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
            }
            scanner.close();
            return builder.toString();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeFile(File f, String content, boolean replaced) {
        try {
            if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
            if (!f.exists()) f.createNewFile();

            PrintWriter writer = new PrintWriter(f, "UTF-8");
            if (replaced) {
                writer.write(content);
            } else {
                writer.println(content);
            }
            writer.flush();
            writer.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
