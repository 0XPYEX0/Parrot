package me.xpyex.plugin.allinone.modelcode.music;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import me.xpyex.plugin.allinone.utils.Util;

public final class MusicUtils {
    
    /**
     * Read all content from input stream.<br>
     * 从数据流读取全部数据
     *
     * @param i the input stream<br>
     *          数据流
     * @return return all read data <br>
     *         返回读入的所有数据
     * @throws IOException Signals that an I/O exception has occurred.<br>
     *                     发生IO错误
     */
    public static byte[] readAll(InputStream i) throws IOException {
        ByteArrayOutputStream ba = new ByteArrayOutputStream(16384);
        int nRead;
        byte[] data = new byte[4096];
        
        try {
            while ((nRead = i.read(data, 0, data.length)) != -1) {
                ba.write(data, 0, nRead);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw e;
        }
        
        return ba.toByteArray();
    }

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
