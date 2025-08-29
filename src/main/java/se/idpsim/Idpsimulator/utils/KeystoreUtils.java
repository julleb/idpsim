package se.idpsim.Idpsimulator.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import lombok.experimental.UtilityClass;

@UtilityClass
public class KeystoreUtils {

    private static final String CLASSLOADER_PREFIX = "classpath:";

    public KeyStore getKeyStore(String keystorePath, String keystorePassword) throws Exception {
        InputStream inputStream = null;
        if(keystorePath.startsWith(CLASSLOADER_PREFIX)) {
            inputStream = KeystoreUtils.class.getClassLoader()
                .getResourceAsStream(keystorePath.replaceFirst(CLASSLOADER_PREFIX, "").trim());
        } else {
            inputStream = new FileInputStream(keystorePath);
        }
        if(inputStream == null) {
            throw new IllegalArgumentException("Keystore not found at path: " + keystorePath);
        }
        return loadKeyStore(inputStream, keystorePassword);
    }

    public KeyStore loadKeyStore(InputStream is, String keystorePassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(is, keystorePassword.toCharArray());
        is.close();
        return keyStore;
    }

    public X509Certificate getAsX509Certificate(KeyStore keyStore, String alias) throws Exception {
        return (X509Certificate) keyStore.getCertificate(alias);
    }

    public PrivateKey getPrivateKey(KeyStore keyStore, String alias, String keyPassword) throws Exception {
        return (PrivateKey) keyStore.getKey(alias, keyPassword.toCharArray());
    }

}
