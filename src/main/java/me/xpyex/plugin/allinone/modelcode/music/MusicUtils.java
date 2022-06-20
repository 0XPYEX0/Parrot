package me.xpyex.plugin.allinone.modelcode.music;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import me.xpyex.plugin.allinone.utils.Util;

public final class MusicUtils {

    public static byte[] readAll(final File i) {
        try (final FileInputStream fis = new FileInputStream(i)) {
            return Util.readAll(fis);
        } catch (IOException ex) {
            return new byte[0];
        }
    }

    public static long getTime() {
        return new Date().getTime();
        //
    }

    public static String bytesToHex(final byte[] hash) {
        final StringBuffer hexString = new StringBuffer();
        for (byte b : hash) {
            final String hex = Integer.toHexString(0xFF & b);
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

    public static boolean isExistent(String urlstr) throws IOException {
        try {
            URL url = new URL(urlstr);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36");
            huc.setRequestMethod("HEAD");
            huc.connect();
            return huc.getResponseCode() == 200;
        } catch (Exception ex) {

            return false;
        }
    }
}
