package gov.pianzong.httpclientproject.network;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * @CreateDate: 2020/3/27
 * @Author: honeylife
 * @Description:
 * @Version:
 */
public class SSLClient extends DefaultHttpClient {
    public SSLClient() throws Exception{
        super();
        final SSLContext ctx = SSLContext.getInstance("TLS");
        X509TrustManager tm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        ctx.init(null, new TrustManager[]{tm}, null);
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        SSLSocketFactory ssf = new SSLSocketFactory(trustStore){
            @Override
            public Socket createSocket() throws IOException {
                return ctx.getSocketFactory().createSocket();
            }
            @Override
            public Socket createSocket(Socket socket, String host, int port,
                                       boolean autoClose) throws IOException, UnknownHostException {
                return ctx.getSocketFactory().createSocket(socket, host, port,
                        autoClose);
            }
            @Override
            public boolean isSecure(Socket sock) throws IllegalArgumentException {
                return super.isSecure(sock);
            }
        };
        ClientConnectionManager ccm = this.getConnectionManager();
        SchemeRegistry sr = ccm.getSchemeRegistry();
        sr.register(new Scheme("https", ssf, 443));
    }


}
