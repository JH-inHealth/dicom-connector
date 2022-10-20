/**
 * Copyright (c) 2022 The Johns Hopkins University
 * All rights reserved
 *
 * @author David J. Talley, Technology Innovation Center, Precision Medicine Analytics Platform, Johns Hopkins Medicine
 *
 */
package org.mule.module.dicom.internal.util;

import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.SSLManagerFactory;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.api.tls.TlsContextKeyStoreConfiguration;
import org.mule.runtime.api.tls.TlsContextTrustStoreConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class SecurityUtils {
    private SecurityUtils() { }
    private static final String[] DEFAULT_PROTOCOLS = new String[]{"TLSv1.2"};
    private static final String[] DEFAULT_CIPHERS = new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384", "TLS_RSA_WITH_AES_128_GCM_SHA256", "TLS_RSA_WITH_AES_128_CBC_SHA",
            "TLS_RSA_WITH_AES_128_CBC_SHA256", "TLS_RSA_WITH_AES_256_GCM_SHA384", "TLS_RSA_WITH_AES_256_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA256",
            "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256", "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_DHE_RSA_WITH_AES_256_CBC_SHA", "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256"};

    public static void setTls(Device device, Connection connection, TlsContextFactory tlsContextFactory) throws CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        if (tlsContextFactory != null) {
            String[] protocols = tlsContextFactory.getEnabledProtocols();
            if (protocols != null && protocols.length > 0) {
                connection.setTlsProtocols(protocols);
            } else {
                connection.setTlsProtocols(DEFAULT_PROTOCOLS);
            }
            String[] ciphers = tlsContextFactory.getEnabledCipherSuites();
            if (ciphers != null && ciphers.length > 0) {
                connection.setTlsCipherSuites(ciphers);
            } else {
                connection.setTlsCipherSuites(DEFAULT_CIPHERS);
            }
            if (tlsContextFactory.isTrustStoreConfigured()) {
                TlsContextTrustStoreConfiguration trustStoreConfig = tlsContextFactory.getTrustStoreConfiguration();
                if (device != null) {
                    String trustStoreUrl = fileToUrl(trustStoreConfig.getPath());
                    String trustStoreType = trustStoreConfig.getType();
                    String trustStorePwd = trustStoreConfig.getPassword();
                    device.setTrustManager(SSLManagerFactory.createTrustManager(trustStoreType, trustStoreUrl, trustStorePwd));
                }
                connection.setTlsNeedClientAuth(!trustStoreConfig.isInsecure());
            }
            if (device != null && tlsContextFactory.isKeyStoreConfigured()) {
                TlsContextKeyStoreConfiguration keyStoreConfig = tlsContextFactory.getKeyStoreConfiguration();
                String keyStoreUrl = fileToUrl(keyStoreConfig.getPath());
                String keyStoreType = keyStoreConfig.getType();
                String keyStorePwd = keyStoreConfig.getPassword();
                String keyPwd = keyStoreConfig.getKeyPassword();
                device.setKeyManager(SSLManagerFactory.createKeyManager(keyStoreType, keyStoreUrl, keyStorePwd, keyPwd));
            }
        }
    }

    private static String fileToUrl(String filePath) throws MalformedURLException {
        File file = new File(filePath);
        URL url = file.toURI().toURL();
        return url.toString();
    }
}
