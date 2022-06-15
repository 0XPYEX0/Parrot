package me.xpyex.plugin.allinone.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import me.xpyex.plugin.allinone.Main;

public class ReflectUtil {
    private static final File PLUGIN_JAR_FILE = new File(Main.INSTANCE
            .getLoader().getClassLoaders().get(0)
            .getResource("me/xpyex/plugin/allinone/Main.class")
            .getPath()
            .replace("file:/", "")
            .split("!")[0]
    );

    public static List<Class<?>> getClasses(String packagePath) {
        List<Class<?>> classList = new ArrayList<>();
        try {
            JarFile jar = new JarFile(PLUGIN_JAR_FILE);
            Enumeration<JarEntry> enumFiles = jar.entries();
            while (enumFiles.hasMoreElements()) {
                JarEntry entry = enumFiles.nextElement();
                if (!entry.getName().contains("META-INF")) {
                    String classPath = entry.getName().replace("/", ".");
                    if (classPath.endsWith(".class")) {
                        String className = classPath.substring(0, classPath.length() - 6);
                        if (className.contains(packagePath)) {
                            classList.add(
                                    Class.forName(className)
                            );
                        }
                    }
                }
            }
        } catch (Exception e) {
            Util.handleException(e);
        }
        return classList;
    }
}
