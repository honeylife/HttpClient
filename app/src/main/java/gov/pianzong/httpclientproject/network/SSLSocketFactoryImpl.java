package gov.pianzong.httpclientproject.network;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

;


/**
 *
 * @CreateDate: 2020/3/26
 * @Author: honeylife
 * @Description:
 * @Version:
 *
 */
public class SSLSocketFactoryImpl extends SSLSocketFactory {


    /**
     * 使用协议
     */
    private static final String CLIENT_AGREEMENT = "TLS";
    SSLContext sslContext = SSLContext.getInstance(CLIENT_AGREEMENT);

    public SSLSocketFactoryImpl(KeyStore keystore)
            throws NoSuchAlgorithmException, KeyManagementException,
            KeyStoreException, UnrecoverableKeyException, IOException, CertificateException {
        super(keystore);
        TrustManager tm = new X509TrustManager() {

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType)
                    throws java.security.cert.CertificateException {

            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType)
                    throws java.security.cert.CertificateException {

            }
        };

        sslContext.init(null, new TrustManager[] { tm }, null);
    }



    @Override
    public Socket createSocket(Socket socket, String host, int port,
                               boolean autoClose) throws IOException, UnknownHostException {
        return sslContext.getSocketFactory().createSocket(socket, host, port,
                autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }



}