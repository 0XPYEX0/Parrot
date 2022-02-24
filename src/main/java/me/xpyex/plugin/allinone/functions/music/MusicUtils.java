package me.xpyex.plugin.allinone.functions.music;

import java.io.*;
import java.util.*;

public final class MusicUtils {
    public static byte[] readAll(final InputStream i) throws IOException {
        final ByteArrayOutputStream ba = new ByteArrayOutputStream(16384);
        final byte[] data = new byte[4096];
        try {
            int nRead;
            while ((nRead = i.read(data, 0, data.length)) != -1) {
                ba.write(data, 0, nRead);
            }
        }
        catch (IOException e) {
            throw e;
        }
        return ba.toByteArray();
    }
    
    public static byte[] readAll(final File i) {
        try (final FileInputStream fis = new FileInputStream(i)) {
            return readAll(fis);
        }
        catch (IOException ex) {
            return new byte[0];
        }
    }
    
    public static long getTime() {
        return new Date().getTime();
    }

    public static String bytesToHex(final byte[] hash) {
        final StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; ++i) {
            final String hex = Integer.toHexString(0xFF & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    public static String removeLeadings(final String leading, final String orig) {
        return orig.replace(leading, "").trim();
    }
}
