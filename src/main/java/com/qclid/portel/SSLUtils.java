package com.qclid.portel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.bukkit.configuration.file.FileConfiguration;

public class SSLUtils {

    public static SSLContext createSSLContext(File dataFolder, FileConfiguration config) throws Exception {
        String keystorePath = config.getString("ssl.keystore-path");
        String passwordStr = config.getString("ssl.keystore-password");
        
        if (keystorePath == null || passwordStr == null) {
            throw new IOException("SSL configuration is missing keystore path or password.");
        }

        char[] password = passwordStr.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        File keystoreFile = new File(dataFolder, keystorePath);
        
        if (!keystoreFile.exists()) {
            throw new IOException("Keystore file not found at: " + keystoreFile.getAbsolutePath());
        }

        try (FileInputStream fis = new FileInputStream(keystoreFile)) {
            ks.load(fis, password);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, password);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        
        return sslContext;
    }
}
