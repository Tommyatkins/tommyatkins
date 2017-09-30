package com.tommyatkins.security.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResourceLoader;

public class RSAUtil {

    private static final Logger logger = Logger.getLogger(RSAUtil.class);
    /** 指定加密算法为RSA */
    private final static String ALGORITHM = "RSA";
    /** 指定签名算法 */
    private final static String SIGN_ALGORITHMS = "SHA1WithRSA";
    /** 指定key的大小 */
    private final static int KEYSIZE = 1024; // bite
    /** 指定key的大小 */
    private final static String ENCODE = "UTF-8";

    /**
     * 初始化对称密钥
     * 
     * @return
     * @throws Exception
     */
    public static Map<String, Object> initKey(String system) throws Exception {

        /** RSA算法要求有一个可信任的随机数源 */
        SecureRandom sr = new SecureRandom();
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(ALGORITHM);
        keyPairGen.initialize(KEYSIZE, sr);
        KeyPair keyPair = keyPairGen.generateKeyPair();

        Key publicKey = keyPair.getPublic();
        Key privateKey = keyPair.getPrivate();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("PUBLIC_KEY", publicKey);
        map.put("PRIVATE_KEY", privateKey);

        return map;
    }

    /** 加密算法 :明文+秘钥=密文(base64) */
    public static String encrypt(String data, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        /** 执行加密操作 */
        byte[] b = cipher.doFinal(data.getBytes());

        return Base64.getEncoder().encodeToString(b);
    }

    /** 解密算法 :密文(base64)+秘钥 */
    public static String decrypt(String encryptData, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        /** 执行解密操作 */
        byte[] b = cipher.doFinal(Base64.getDecoder().decode(encryptData));
        return new String(b, ENCODE);
    }

    /**
     * RSA私钥签名方法
     * 
     * @param data
     *        明文
     * @param privateKey
     *        私钥
     * @return
     * @throws Exception
     */
    public static String sign(String data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
        signature.initSign(privateKey);
        signature.update(data.getBytes(ENCODE));
        byte[] signed = signature.sign();
        return Base64.getEncoder().encodeToString(signed);
    }

    /**
     * RSA公钥校验签名方法
     * 
     * @param data
     *        明文
     * @param signData
     *        签名数据
     * @param publicKey
     *        公钥
     * @return
     */
    public static boolean verify(String data, String signData, PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
            signature.initVerify(publicKey);
            signature.update(data.getBytes(ENCODE));
            boolean bverify = signature.verify(Base64.getDecoder().decode(signData));
            return bverify;

        } catch (Exception e) {
            logger.error(e);
        }
        return false;
    }

    /**
     * 从文件中读取秘钥串，去除首行和尾行，支持classpath
     * 
     * @param path
     * @return
     * @throws Exception
     */
    public static String loadKeyStrByFile(String path) throws Exception {
        FileSystemResourceLoader loader = new FileSystemResourceLoader();
        try (FileReader fr = new FileReader(loader.getResource(path).getFile());
                BufferedReader br = new BufferedReader(fr);) {
            String readLine = null;
            StringBuilder sb = new StringBuilder();
            boolean firstLine = true;
            String lastLine = null;
            while ((readLine = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                } else {
                    sb.append(readLine);
                    lastLine = readLine;
                }
            }

            return sb.substring(0, sb.lastIndexOf(lastLine));
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 秘钥串转私钥
     * 
     * @param privateKeyStr
     * @return
     * @throws Exception
     */
    public static PrivateKey loadPrivateKeyByStr(String privateKeyStr) throws Exception {
        try {
            byte[] buffer = Base64.getDecoder().decode(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 秘钥传转公钥
     * 
     * @param publicKeyStr
     * @return
     * @throws Exception
     */
    public static PublicKey loadPublicKeyByStr(String publicKeyStr) throws Exception {
        try {
            byte[] buffer = Base64.getDecoder().decode(publicKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw e;
        }
    }

}
