package me.xpyex.plugin.allinone.functions.music;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import me.xpyex.plugin.allinone.utils.Util;

public class NetEaseCrypto {
    static final String[] userAgentList;
    private static String presetKey;
    private static String publicKey;
    private static String modulus;
    private static String iv;

    static {
        userAgentList = new String[]{"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36", "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1", "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Mobile Safari/537.36", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Mobile Safari/537.36", "Mozilla/5.0 (Linux; Android 5.1.1; Nexus 6 Build/LYZ28E) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Mobile Safari/537.36", "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_2 like Mac OS X) AppleWebKit/603.2.4 (KHTML, like Gecko) Mobile/14F89;GameHelper", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/603.2.4 (KHTML, like Gecko) Version/10.1.1 Safari/603.2.4", "Mozilla/5.0 (iPhone; CPU iPhone OS 10_0 like Mac OS X) AppleWebKit/602.1.38 (KHTML, like Gecko) Version/10.0 Mobile/14A300 Safari/602.1", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:46.0) Gecko/20100101 Firefox/46.0", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:46.0) Gecko/20100101 Firefox/46.0", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0)", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; Win64; x64; Trident/6.0)", "Mozilla/5.0 (Windows NT 6.3; Win64, x64; Trident/7.0; rv:11.0) like Gecko", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/13.10586", "Mozilla/5.0 (iPad; CPU OS 10_0 like Mac OS X) AppleWebKit/602.1.38 (KHTML, like Gecko) Version/10.0 Mobile/14A300 Safari/602.1"};
        NetEaseCrypto.presetKey = "0CoJUm6Qyw8W8jud";
        NetEaseCrypto.publicKey = "010001";
        NetEaseCrypto.modulus = "00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b3ece0462db0a22b8e7";
        NetEaseCrypto.iv = "0102030405060708";
    }

    public static String getUserAgent() {
        final Double index = Math.floor(Math.random() * NetEaseCrypto.userAgentList.length);
        return NetEaseCrypto.userAgentList[index.intValue()];
    }

    private static String createSecretKey(final int size) {
        final String keys = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String key = "";
        for (int i = 0; i < size; ++i) {
            final Double index = Math.floor(Math.random() * keys.length());
            key += keys.charAt(index.intValue());
        }
        return key;
    }

    private static String aesEncrypt(final String content, final String key, final String iv) {
        String result = null;
        if (content == null || key == null) {
            return result;
        }
        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] bytes = new byte[0];
            cipher.init(1, new SecretKeySpec(key.getBytes("utf-8"), "AES"), new IvParameterSpec(iv.getBytes("utf-8")));
            bytes = cipher.doFinal(content.getBytes("utf-8"));
            result = Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            Util.handleException(e);
        }
        return result;
    }

    private static String fillString(final String str, final int size) {
        final StringBuilder sb = new StringBuilder(str);
        while (sb.length() < size) {
            sb.insert(0, "0");
        }
        return sb.toString();
    }

    private static String rsaEncrypt(String text, final String pubKey, final String modulus) {
        text = new StringBuffer(text).reverse().toString();
        final BigInteger biText = new BigInteger(MusicUtils.bytesToHex(text.getBytes()), 16);
        final BigInteger biEx = new BigInteger(pubKey, 16);
        final BigInteger biMod = new BigInteger(modulus, 16);
        final BigInteger biRet = biText.modPow(biEx, biMod);
        return fillString(biRet.toString(16), 256);
    }

    public static String[] weapiEncrypt(final String content) {
        final String[] result = new String[2];
        final String key = createSecretKey(16);
        final String encText = aesEncrypt(aesEncrypt(content, NetEaseCrypto.presetKey, NetEaseCrypto.iv), key, NetEaseCrypto.iv);
        final String encSecKey = rsaEncrypt(key, NetEaseCrypto.publicKey, NetEaseCrypto.modulus);
        try {
            result[0] = URLEncoder.encode(encText, "UTF-8");
            result[1] = URLEncoder.encode(encSecKey, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
        }
        return result;
    }
}
