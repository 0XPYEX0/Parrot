/**
 * Mirai Song Plugin
 * Copyright (C) 2021  khjxiaogu
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.xpyex.plugin.parrot.mirai.module.music.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public final class MusicUtils {

    /**
     * Read all content from input stream.<br>
     * 从数据流读取全部数据
     *
     * @param i the input stream<br>
     *          数据流
     * @return return all read data <br>
     * 返回读入的所有数据
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

    /**
     * Read all content from File.<br>
     * 从文件读取全部数据
     *
     * @param i the file<br>
     *          文件
     * @return return all read data <br>
     * 返回读入的所有数据
     */
    public static byte[] readAll(File i) {
        try (FileInputStream fis = new FileInputStream(i)) {
            return readAll(fis);
        } catch (IOException ignored) {
            return new byte[0];
        }
    }

    /**
     * Gets current time.<br>
     * 获取当前时间.
     *
     * @return time<br>
     */
    public static long getTime() {
        return new Date().getTime();
        //
    }

    /**
     * byte array to hex string.<br>
     * 字节串转换为十六进制字符串。
     *
     * @param hash the byte array<br>
     *             字节串。
     * @return return hex string<br>
     * 返回十六进制字符串。
     */
    public static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
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