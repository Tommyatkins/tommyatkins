package com.tommyatkins.test.certifications;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.RSAPrivateKeySpec;
import java.util.Base64;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;

import com.tommyatkins.security.utils.RSAUtil;

import sun.security.pkcs10.PKCS10;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.AlgorithmId;
import sun.security.x509.BasicConstraintsExtension;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateExtensions;
import sun.security.x509.CertificateVersion;
import sun.security.x509.DNSName;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNames;
import sun.security.x509.IPAddressName;
import sun.security.x509.SubjectAlternativeNameExtension;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

/**
 * @author zlkong
 * @date 2017年9月26日 上午11:13:58
 * @description TODO
 *
 ***********************************************
 * @modifyRecord
 ***********************************************
 * @editor zlkong
 * @version V1.0
 * @date 2017年9月26日 上午11:13:58
 * @description 创建
 *
 */
public class CertificationGenerater {

    public static void main(String[] args) throws Exception {
        tryGen();

    }

    public static void tryGen() throws Exception {

        CertificateFactory x509Factory = CertificateFactory.getInstance("x.509");

        try (FileInputStream fis = new FileInputStream("cert/cacert.crt");) {
            String privateKeyStr = RSAUtil.loadKeyStrByFile("cert/cakey.pem");
            byte[] privateKeyByte = Base64.getDecoder().decode(privateKeyStr);
            ByteArrayInputStream bais = new ByteArrayInputStream(privateKeyByte);
            ASN1InputStream in = new ASN1InputStream(bais);
            ASN1Primitive obj = in.readObject();
            RSAPrivateKey pStruct = RSAPrivateKey.getInstance(obj);
            RSAPrivateKeySpec spec = new RSAPrivateKeySpec(pStruct.getModulus(), pStruct.getPublicExponent());
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey rootKey = kf.generatePrivate(spec);
            bais.close();
            in.close();

            // openssl pkcs8 -topk8 -inform PEM -in cakey.pem -outfo rm pem -nocrypt -out cakey_pkcs8.pem
            rootKey = RSAUtil.loadPrivateKeyByStr(RSAUtil.loadKeyStrByFile("/cert/cakey_pkcs8.pem"));

            storeBytes(rootKey.getEncoded(), "RSA PRIVATE KEY", "cert/out/ca.pem");

            X509Certificate root = (X509Certificate) x509Factory.generateCertificate(fis);

            CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA256WithRSA", null);
            keyGen.setRandom(SecureRandom.getInstance("SHA1PRNG", "SUN"));
            keyGen.generate(2048);

            X500Name x500Name = new X500Name("CN=zlkong");

            PKCS10 certRequest = keyGen.getCertRequest(x500Name);

            byte[] csr = certRequest.getEncoded();
            storeBytes(csr, "CERTIFICATE REQUEST", "cert/out/top.csr");

            PrivateKey privateKey = keyGen.getPrivateKey();
            storeBytes(privateKey.getEncoded(), "RSA PRIVATE KEY", "cert/out/top.pem");

            X509Certificate top = keyGen.getSelfCertificate(x500Name, 3 * 365 * 24 * 60 * 60);

            storeBytes(top.getEncoded(), "CERTIFICATE", "cert/out/before_top.crt");

            top = tryCreateSignedCertificate(top, root, rootKey);

            storeCertificate(top, "cert/out/top.crt");

            storeKeyAndCertificateChain("top", "changeit".toCharArray(), "cert/out/top.jks", privateKey,
                    new X509Certificate[] {top, root});

        }

    }

    static X509Certificate tryCreateSignedCertificate(X509Certificate cetrificate, X509Certificate issuerCertificate,
            PrivateKey issuerPrivateKey) {
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

            GeneralNames gn = new GeneralNames();
            gn.add(new GeneralName(new DNSName("zlkong")));
            gn.add(new GeneralName(new IPAddressName("127.0.0.1")));
            SubjectAlternativeNameExtension subjectAltName = new SubjectAlternativeNameExtension(gn);
            exts.set(SubjectAlternativeNameExtension.NAME, subjectAltName);

            BasicConstraintsExtension bce = new BasicConstraintsExtension(false, -1);
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

    public static void test() {
        try {
            // Generate ROOT certificate
            CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA256WithRSA", null);
            keyGen.generate(1024);
            PrivateKey rootPrivateKey = keyGen.getPrivateKey();

            X509Certificate rootCertificate =
                    keyGen.getSelfCertificate(new X500Name("CN=ROOT"), (long) 365 * 24 * 60 * 60);

            // Generate intermediate certificate
            CertAndKeyGen keyGen1 = new CertAndKeyGen("RSA", "SHA256WithRSA", null);
            keyGen1.generate(1024);
            PrivateKey middlePrivateKey = keyGen1.getPrivateKey();

            X509Certificate middleCertificate =
                    keyGen1.getSelfCertificate(new X500Name("CN=MIDDLE"), (long) 365 * 24 * 60 * 60);

            // Generate leaf certificate
            CertAndKeyGen keyGen2 = new CertAndKeyGen("RSA", "SHA256WithRSA", null);
            keyGen2.generate(1024);
            PrivateKey topPrivateKey = keyGen2.getPrivateKey();

            X509Certificate topCertificate =
                    keyGen2.getSelfCertificate(new X500Name("CN=TOP"), (long) 365 * 24 * 60 * 60);

            rootCertificate = createSignedCertificate(rootCertificate, rootCertificate, rootPrivateKey);
            storeCertificate(rootCertificate, "cert/out/root.crt");

            middleCertificate = createSignedCertificate(middleCertificate, rootCertificate, rootPrivateKey);
            storeCertificate(middleCertificate, "cert/out/middle.crt");

            topCertificate = createSignedCertificate(topCertificate, middleCertificate, middlePrivateKey);
            storeCertificate(topCertificate, "cert/out/top.crt");

            X509Certificate[] chain = new X509Certificate[3];
            chain[0] = topCertificate;
            chain[1] = middleCertificate;
            chain[2] = rootCertificate;

            String alias = "mykey";
            char[] password = "password".toCharArray();
            String keystore = "cert/out/testkeys.jks";

            // Store the certificate chain
            storeKeyAndCertificateChain(alias, password, keystore, topPrivateKey, chain);
            // Reload the keystore and display key and certificate chain info
            loadAndDisplayChain(alias, password, keystore);
            // Clear the keystore
            // clearKeyStore(alias, password, keystore);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static void storeKeyAndCertificateChain(String alias, char[] password, String keystore, Key key,
            X509Certificate[] chain) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("pkcs12");
        keyStore.load(null, null);

        keyStore.setKeyEntry(alias, key, password, chain);
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
            fos.write(Base64.getEncoder().encode(data));
            fos.write(String.format("\n-----END %s-----\n", type).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
