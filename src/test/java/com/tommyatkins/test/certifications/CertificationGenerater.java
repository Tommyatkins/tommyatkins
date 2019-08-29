package com.tommyatkins.test.certifications;

import com.tommyatkins.security.utils.RSAUtil;
import sun.security.pkcs10.PKCS10;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.*;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * @author zlkong
 * @version V1.0
 * @date 2017年9月26日 上午11:13:58
 * @description TODO
 * <p>
 * **********************************************
 * @modifyRecord **********************************************
 * @editor zlkong
 * @date 2017年9月26日 上午11:13:58
 * @description 创建
 */
public class CertificationGenerater {

    public static void main(String[] args) throws Exception {
//        tryGen("ignore/cert/ca.crt","ignore/cert/cakey.pem","ignore/cert/cakey_pkcs8.pem");

        tryGen("gemini",
                365,
                "E:\\Repository\\certifications\\gemini\\ca.crt",
                "E:\\Repository\\certifications\\gemini\\ca.key",
                "E:\\Repository\\certifications\\gemini\\ca_pkcs8.key",
                "gemini-debug",
                "avBPw9LDGLuHmnrE",
                new String[]{},
                new String[]{});
    }

    final static String CERTIFICATE_TYPE = "X.509";

    final static String ALGORITHM_RSA = "RSA";

    private CertificationGenerater() {
    }

    /**
     * @param is
     * @return
     * @throws CertificateException
     * @author zlkong
     * @description 输入流转换成x509证书
     * @date 2017年9月30日 下午3:30:30
     */
    public static X509Certificate parseX509Certificate(InputStream is) throws CertificateException {
        return (X509Certificate) parseCertificate(is, CERTIFICATE_TYPE);
    }

    /**
     * @param is
     * @param type
     * @return
     * @throws CertificateException
     * @author zlkong
     * @description 数据转换成相应类型的证书
     * @date 2017年9月30日 下午3:30:54
     */
    public static Certificate parseCertificate(InputStream is, String type) throws CertificateException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance(type);
        Certificate certificate = certificateFactory.generateCertificate(is);
        return certificate;
    }

    /**
     * @param is
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     * @author zlkong
     * @description 读取PKCS8格式的私钥（PKCS8私钥是从openssl生成的RSA私钥中转换过来的）
     * @date 2017年10月9日 上午9:54:11
     */
    public static PrivateKey parsePKCS8PrivateKey(InputStream is) throws IOException, GeneralSecurityException {
        byte[] keyBytes = Base64.getDecoder().decode(read(is));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    /**
     * @param is
     * @return
     * @throws IOException
     * @author zlkong
     * @description 从字节流上读取字节数组
     * @date 2017年10月9日 上午10:11:52
     */
    private static byte[] read(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] temp = new byte[256];
        int length = 0;
        while ((length = is.read(temp)) != -1) {
            baos.write(temp, 0, length);
        }
        byte[] data = baos.toByteArray();
        baos.close();
        return data;
    }

    public static void tryGen(String ca, int days, String crt, String key, String pkcs8, String commonName, String pw, String[] ips, String[] dns) throws Exception {

        CertificateFactory x509Factory = CertificateFactory.getInstance("x.509");

        // openssl req -new -x509 -days 365 -keyout ca.key -out ca.crt
        try (FileInputStream fis = new FileInputStream(crt);) {
//            String privateKeyStr = RSAUtil.loadKeyStrByFile(key);
//            byte[] privateKeyByte = Base64.getDecoder().decode(privateKeyStr);
//            ByteArrayInputStream bais = new ByteArrayInputStream(privateKeyByte);
//            ASN1InputStream in = new ASN1InputStream(bais);
//            ASN1Primitive obj = in.readObject();
//            RSAPrivateKey pStruct = RSAPrivateKey.getInstance(obj);
//            RSAPrivateKeySpec spec = new RSAPrivateKeySpec(pStruct.getModulus(), pStruct.getPublicExponent());
//            KeyFactory kf = KeyFactory.getInstance("RSA");
//            PrivateKey rootKey = kf.generatePrivate(spec);
//            bais.close();
//            in.close();

            // openssl pkcs8 -topk8 -inform PEM -in cakey.pem -outform pem -nocrypt -out cakey_pkcs8.pem
            PrivateKey rootKey = RSAUtil.loadPrivateKeyByStr(RSAUtil.loadKeyStrByFile(pkcs8));

            storeBytes(rootKey.getEncoded(), "RSA PRIVATE KEY", "ignore/cert/out/ca.pem");

            X509Certificate root = (X509Certificate) x509Factory.generateCertificate(fis);

            CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA256WithRSA", null);
            keyGen.setRandom(SecureRandom.getInstance("SHA1PRNG", "SUN"));
            keyGen.generate(2048);

            // TODO
            X500Name x500Name = new X500Name("CN=" + commonName);

            PKCS10 certRequest = keyGen.getCertRequest(x500Name);

            byte[] csr = certRequest.getEncoded();
            storeBytes(csr, "CERTIFICATE REQUEST", "ignore/cert/out/top.csr");

            PrivateKey privateKey = keyGen.getPrivateKey();
            storeBytes(privateKey.getEncoded(), "RSA PRIVATE KEY", "ignore/cert/out/server.key");

            X509Certificate top = keyGen.getSelfCertificate(x500Name, TimeUnit.SECONDS.convert(days, TimeUnit.DAYS));

            storeBytes(top.getEncoded(), "CERTIFICATE", "ignore/cert/out/before_top.crt");

            top = tryCreateSignedCertificate(top, root, rootKey, ips, dns);

            storeCertificate(top, "ignore/cert/out/server.crt");

            // TODO
            char[] password = pw.toCharArray();
//            char[] password = "b70EJdwiqBassPx8".toCharArray();

            storeKeyAndCertificateChainJKS(ca, root, commonName, password, "ignore/cert/out/server.keystore",
                    privateKey, new X509Certificate[]{top});

            storeKeyAndCertificateChainPFX(ca, root, commonName, password, "ignore/cert/out/server.pfx",
                    privateKey, new X509Certificate[]{top});

        }

    }

    static X509Certificate tryCreateSignedCertificate(X509Certificate cetrificate, X509Certificate issuerCertificate,
                                                      PrivateKey issuerPrivateKey, String[] ips, String[] dns) {
        try {
            Principal issuer = issuerCertificate.getSubjectDN();
            String issuerSigAlg = issuerCertificate.getSigAlgName();

            byte[] inCertBytes = cetrificate.getTBSCertificate();
            X509CertInfo info = new X509CertInfo(inCertBytes);
            // CertificateIssuerName issuerName = new CertificateIssuerName((X500Name) issuer);

            info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
            AlgorithmId algo = new AlgorithmId(AlgorithmId.sha256WithRSAEncryption_oid);
            info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));

            // info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(new BigInteger(64, new
            // SecureRandom())));
            // info.set(X509CertInfo.VALIDITY, new CertificateValidity(cetrificate.getNotBefore(),
            // cetrificate.getNotAfter()));
            // info.set(X509CertInfo.SUBJECT, cetrificate.getSubjectDN());
            // info.set(X509CertInfo.KEY, new CertificateX509Key(cetrificate.getPublicKey()));

            info.set(X509CertInfo.ISSUER, issuer);
            CertificateExtensions exts = new CertificateExtensions();
            boolean hasIp = ips != null && ips.length > 0;
            boolean hasDns = dns != null && dns.length > 0;
            if (hasIp || hasDns) {
                GeneralNames gn = new GeneralNames();
                if (hasIp) {
                    for (String ip : ips) {
                        gn.add(new sun.security.x509.GeneralName(new sun.security.x509.IPAddressName(ip)));
                    }
                }
                if (hasDns) {
                    for (String dn : dns) {
                        gn.add(new sun.security.x509.GeneralName(new sun.security.x509.DNSName(dn)));
                    }
                }

                SubjectAlternativeNameExtension subjectAltName = new SubjectAlternativeNameExtension(gn);

                // TODO
                exts.set(SubjectAlternativeNameExtension.NAME, subjectAltName);
            }


            BasicConstraintsExtension bce = new BasicConstraintsExtension(false, -1);
//            BasicConstraintsExtension bce = new BasicConstraintsExtension(true, 3);
            exts.set(BasicConstraintsExtension.NAME, bce);

            info.set(X509CertInfo.EXTENSIONS, exts);

            X509CertImpl outCert = new X509CertImpl(info);
            outCert.sign(issuerPrivateKey, issuerSigAlg);

            return outCert;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    static byte[] generateCSR(String sigAlg, KeyPair keyPair) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outStream);

        try {
            X500Name x500Name = new X500Name("CN=EXAMPLE.COM");

            Signature sig = Signature.getInstance(sigAlg);

            sig.initSign(keyPair.getPrivate());

            PKCS10 pkcs10 = new PKCS10(keyPair.getPublic());
            // pkcs10.encodeAndSign(new X500Signer(sig, x500Name)); // For Java 6
            pkcs10.encodeAndSign(x500Name, sig); // For Java 7 and Java 8
            pkcs10.print(printStream);

            byte[] csrBytes = outStream.toByteArray();

            return csrBytes;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (null != outStream) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (null != printStream) {
                printStream.close();
            }
        }

        return new byte[0];
    }

    static byte[] generateCSR(String sigAlg, PrivateKey privateKey, PKCS10 pkcs10) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outStream);

        try {
            X500Name x500Name = new X500Name("CN=EXAMPLE.COM");

            Signature sig = Signature.getInstance(sigAlg);

            sig.initSign(privateKey);

            // pkcs10.encodeAndSign(new X500Signer(sig, x500Name)); // For Java 6
            pkcs10.encodeAndSign(x500Name, sig); // For Java 7 and Java 8
            pkcs10.print(printStream);

            byte[] csrBytes = outStream.toByteArray();

            return csrBytes;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (null != outStream) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (null != printStream) {
                printStream.close();
            }
        }

        return new byte[0];
    }

    static void storeKeyAndCertificateChainJKS(String ca, X509Certificate root, String alias, char[] password, String keystore, Key key,
                                               X509Certificate[] chain) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);

        keyStore.setKeyEntry(alias, key, password, chain);
        keyStore.setCertificateEntry(ca, root);
        keyStore.store(new FileOutputStream(keystore), password);
    }

    static void storeKeyAndCertificateChainPFX(String ca, X509Certificate root, String alias, char[] password, String keystore, Key key,
                                               X509Certificate[] chain) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);

        keyStore.setKeyEntry(alias, key, password, chain);
        keyStore.setCertificateEntry(ca, root);
        keyStore.store(new FileOutputStream(keystore), password);
    }

    static void loadAndDisplayChain(String alias, char[] password, String keystore) throws Exception {
        // Reload the keystore
        KeyStore keyStore = KeyStore.getInstance("jks");
        keyStore.load(new FileInputStream(keystore), password);

        Key key = keyStore.getKey(alias, password);

        if (key instanceof PrivateKey) {
            System.out.println("Get private key : ");
            System.out.println(key.toString());

            Certificate[] certs = keyStore.getCertificateChain(alias);
            System.out.println("Certificate chain length : " + certs.length);
            for (Certificate cert : certs) {
                System.out.println(cert.toString());
            }

            System.out.println("################################################################");
        } else {
            System.out.println("Key is not private key");
        }
    }

    static void clearKeyStore(String alias, char[] password, String keystore) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("jks");
        keyStore.load(new FileInputStream(keystore), password);
        keyStore.deleteEntry(alias);
        keyStore.store(new FileOutputStream(keystore), password);
    }

    static X509Certificate createSignedCertificate(X509Certificate cetrificate, X509Certificate issuerCertificate,
                                                   PrivateKey issuerPrivateKey) {
        try {
            Principal issuer = issuerCertificate.getSubjectDN();
            String issuerSigAlg = issuerCertificate.getSigAlgName();

            byte[] inCertBytes = cetrificate.getTBSCertificate();
            X509CertInfo info = new X509CertInfo(inCertBytes);
            // CertificateIssuerName issuerName = new CertificateIssuerName((X500Name) issuer);

            // BigInteger sn = new BigInteger(64, new SecureRandom());
            // CertificateValidity interval = new CertificateValidity(new Date(), new Date());
            // info.set(X509CertInfo.VALIDITY, interval);
            // info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
            // info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
            // info.set(X509CertInfo.SUBJECT, issuer);
            // info.set(X509CertInfo.KEY, new CertificateX509Key(issuerCertificate.getPublicKey()));
            // info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
            // AlgorithmId algo = new AlgorithmId(AlgorithmId.sha256WithRSAEncryption_oid);
            // info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));

            info.set(X509CertInfo.ISSUER, issuer);

            // No need to add the BasicContraint for leaf cert
            if (!cetrificate.getSubjectDN().getName().equals("CN=TOP")) {
                CertificateExtensions exts = new CertificateExtensions();
                BasicConstraintsExtension bce = new BasicConstraintsExtension(true, -1);
                exts.set(BasicConstraintsExtension.NAME, new BasicConstraintsExtension(false, bce.getExtensionValue()));
                info.set(X509CertInfo.EXTENSIONS, exts);
            }

            X509CertImpl outCert = new X509CertImpl(info);
            outCert.sign(issuerPrivateKey, issuerSigAlg);

            return outCert;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static void storeCertificate(X509Certificate certificate, String path) throws Exception {
        storeBytes(certificate.getEncoded(), "CERTIFICATE", path);
    }

    private static void storeBytes(byte[] data, String type, String path) {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(String.format("-----BEGIN %s-----\n", type).getBytes());
            byte[] result = Base64.getEncoder().encode(data);
            int length = result.length;
            int bufferLength = 64;
            int offset = 0;
            while (length - bufferLength > 0) {
                fos.write(result, offset, bufferLength);
                fos.write("\n".getBytes());
                length -= bufferLength;
                offset += bufferLength;
            }
            fos.write(result, offset, length);
            fos.write(String.format("\n-----END %s-----\n", type).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
