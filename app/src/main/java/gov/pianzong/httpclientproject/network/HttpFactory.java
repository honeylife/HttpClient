package gov.pianzong.httpclientproject.network;


import android.os.Build;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import gov.pianzong.httpclientproject.base.DnApplication;
import gov.pianzong.httpclientproject.ssl.EasySSLSocketFactory;

import static org.apache.http.conn.ssl.SSLSocketFactory.getSocketFactory;


/**
 * 操作联网核心对象
 */
public class HttpFactory {

    private static final String ENCODING_GZIP = "gzip";
    /**
     * 连接池默认最大数
     */
    private static final int maxConnections = 15;
    /**
     * 连接超时，包含readTimeout和socketTimeout
     */
    private static final int timeout = 30 * 1000;

    //android应该选用 bks证书格式
    private static final String KEY_STORE_TYPE_BKS = "bks";//android

    //当前证书对应的密码，勿修改！
//    private static final String KEY_STORE_PASSWORD = "donews123";
//    private static final String KEY_STORE_TRUST_PASSWORD = "donews123";
    private static final String KEY_STORE_PASSWORD = "123456";
    private static final String KEY_STORE_TRUST_PASSWORD = "123456";

    static String protocols[] = null, cipherSuites[] = null;
    static {
        try {
            SSLSocket socket = (SSLSocket) javax.net.ssl.SSLSocketFactory.getDefault().createSocket();
            if (socket != null) {
                /* set reasonable protocol versions */
                // - enable all supported protocols (enables TLSv1.1 and TLSv1.2 on Android <5.0)
                // - remove all SSL versions (especially SSLv3) because they're insecure now
                List<String> protocols = new LinkedList<>();
                for (String protocol : socket.getSupportedProtocols())
                    if (!protocol.toUpperCase().contains("SSL"))
                        protocols.add(protocol);
                HttpFactory.protocols = protocols.toArray(new String[protocols.size()]);
                /* set up reasonable cipher suites */
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    // choose known secure cipher suites
                    List<String> allowedCiphers = Arrays.asList(
                            // TLS 1.2
                            "TLS_RSA_WITH_AES_256_GCM_SHA384",
                            "TLS_RSA_WITH_AES_128_GCM_SHA256",
                            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
                            "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                            "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
                            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
                            "TLS_ECHDE_RSA_WITH_AES_128_GCM_SHA256",
                            // maximum interoperability
                            "TLS_RSA_WITH_3DES_EDE_CBC_SHA",
                            "TLS_RSA_WITH_AES_128_CBC_SHA",
                            // additionally
                            "TLS_RSA_WITH_AES_256_CBC_SHA",
                            "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
                            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
                            "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
                            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA");
                    List<String> availableCiphers = Arrays.asList(socket.getSupportedCipherSuites());
                    // take all allowed ciphers that are available and put them into preferredCiphers
                    HashSet<String> preferredCiphers = new HashSet<>(allowedCiphers);
                    preferredCiphers.retainAll(availableCiphers);
                    /* For maximum security, preferredCiphers should *replace* enabled ciphers (thus disabling
                     * ciphers which are enabled by default, but have become unsecure), but I guess for
                     * the security level of DAVdroid and maximum compatibility, disabling of insecure
                     * ciphers should be a server-side task */
                    // add preferred ciphers to enabled ciphers
                    HashSet<String> enabledCiphers = preferredCiphers;
                    enabledCiphers.addAll(new HashSet<>(Arrays.asList(socket.getEnabledCipherSuites())));
                    HttpFactory.cipherSuites = enabledCiphers.toArray(new String[enabledCiphers.size()]);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 代理对象
     */
    public static class Proxy {
        String proxy;
        int port;
    }

    private static SSLSocketFactory sslSocketFactory = null;


    private static SSLSocketFactory getSslSocketFactory() {
        if (sslSocketFactory == null) {
            try {
//
                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//                    KeyStore trustStore = KeyStore.getInstance(KEY_STORE_TYPE_BKS);
//                    trustStore.load(null, null);

//                    客户端证书
                    KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE_BKS);
                    keyStore.load(DnApplication.getInstance().getAssets().open("client.bks"), KEY_STORE_PASSWORD.toCharArray());

                    //服务证书
                    KeyStore trustStore = KeyStore.getInstance(KEY_STORE_TYPE_BKS);// 服务器证书
                    trustStore.load(DnApplication.getInstance().getAssets().open("server.bks"), KEY_STORE_TRUST_PASSWORD.toCharArray());

                    sslSocketFactory = new MySSLSocketFactory( keyStore,trustStore);
                    Log.e("TAG","===走的是这里==");

                }else{
                    //客户端证书
                    KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE_BKS);
                    keyStore.load(DnApplication.getInstance().getAssets().open("client.bks"), KEY_STORE_PASSWORD.toCharArray());


//                    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
//                    KeyStore trustStore = KeyStore.getInstance(KEY_STORE_TYPE_BKS);
//                    keyStore.load(null);
//                    InputStream is = DnApplication.getInstance().getAssets().open("server.bks");
//                    keyStore.setCertificateEntry("0", certificateFactory.generateCertificate(is));
//                    if(is != null) {
//                        is.close();
//                    }
                    //服务证书
                    KeyStore trustStore = KeyStore.getInstance(KEY_STORE_TYPE_BKS);// 服务器证书
                    trustStore.load(DnApplication.getInstance().getAssets().open("server.bks"), KEY_STORE_TRUST_PASSWORD.toCharArray());

                    sslSocketFactory = new MySSLSocketFactory( keyStore,trustStore);
                }

            } catch (Exception e) {
                e.printStackTrace();
                sslSocketFactory = getSocketFactory();
            }
        }
        return sslSocketFactory;
    }

    public  static class MyHom implements X509HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }

        @Override
        public void verify(String host, SSLSocket ssl) throws IOException {
            Log.e("TAG","=host="+host+"ssl="+ssl);

        }

        @Override
        public void verify(String host, X509Certificate cert) throws SSLException {
            Log.e("TAG","=host="+host+"cert="+cert);

        }

        @Override
        public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {

            Log.e("TAG","=host="+host+"cns="+cns+"subjectAlts=="+subjectAlts);
        }
    }
    /**
     * 创建联网核心对象
     *
     * @param header     请求头
     * @param retryCount 重试次数
     * @param timeout    超时时间
     * @param proxy      代理对象
     * @return 配置过后的httpClient 核心对象
     */
    public static DefaultHttpClient createHttpClient(
            Map<String, String> header, int retryCount, int timeout, Proxy proxy) {
        HttpParams params = new BasicHttpParams();
        if (proxy != null) {
            HttpHost host = new HttpHost(proxy.proxy, proxy.port);
            params.setParameter(ConnRouteParams.DEFAULT_PROXY, host);
        }

        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        HttpConnectionParams.setStaleCheckingEnabled(params, false);

        HttpConnectionParams.setConnectionTimeout(params, timeout);
        HttpConnectionParams.setSoTimeout(params, timeout);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpConnectionParams.setSocketBufferSize(params, 8192);

        HttpClientParams.setRedirecting(params, true);

        ConnManagerParams.setTimeout(params, timeout);
        ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(maxConnections));
        ConnManagerParams.setMaxTotalConnections(params, 20);

        final SchemeRegistry supportedSchemes = new SchemeRegistry();

        final SocketFactory sf = PlainSocketFactory.getSocketFactory();

//        SSLSocketFactory sslSocketFactory = getSslSocketFactory();

//        sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        EasySSLSocketFactory easySSLSocketFactory = new EasySSLSocketFactory();
        easySSLSocketFactory.setHostnameVerifier(new MyHom());


//        SSLContext sslcontext = null;
//        javax.net.ssl.SSLSocketFactory NoSSLv3Factory = null;
//        try {
//            sslcontext = SSLContext.getInstance("TLSv1");
//            sslcontext.init(null, null, null);
//            NoSSLv3Factory = new NoSSLv3SocketFactory(sslcontext.getSocketFactory());
//            HttpsURLConnection.setDefaultSSLSocketFactory(NoSSLv3Factory);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (KeyManagementException e) {
//            e.printStackTrace();
//        }
//        l_connection = (HttpsURLConnection) l_url.openConnection();
//        l_connection.connect();

//        RegistryBuilder

        supportedSchemes.register(new Scheme("https", easySSLSocketFactory, 443));
        supportedSchemes.register(new Scheme("http", sf, 80));
        final ThreadSafeClientConnManager ccm = new ThreadSafeClientConnManager(
                params, supportedSchemes);
        DefaultHttpClient httpClient = new DefaultHttpClient(ccm, params);
        httpClient.setHttpRequestRetryHandler(new RequestRetryHandler(retryCount, true));
        // Add gzip header to requests using an interceptor
        httpClient.addRequestInterceptor(new RequestInterceptor(header));
        // Add gzip compression to responses using an interceptor
        httpClient.addResponseInterceptor(new GzipHttpResponseInterceptor());
        CookieSpecFactory csf = new CookieSpecFactory() {
            public CookieSpec newInstance(HttpParams params) {
                return new BrowserCompatSpec() {
                    @Override
                    public void validate(Cookie cookie, CookieOrigin origin)
                            throws MalformedCookieException {

                    }
                };
            }
        };
        httpClient.getCookieSpecs().register("easy", csf);
        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, "easy");
        //新增authScope

        setAuthScope(httpClient);

        return httpClient;
    }

    private static void setAuthScope(DefaultHttpClient httpClient) {
//		BasicScheme basicScheme = new BasicScheme();
//		AuthScope mAuthScope = new AuthScope(SERVER_HOST, AuthScope.ANY_PORT);
        httpClient.setCredentialsProvider(new BasicCredentialsProvider());
//		BasicHttpContext localcontext = new BasicHttpContext();
//		localcontext.setAttribute("preemptive-auth", basicScheme);
    }

    /**
     * 只包含包含gzip拦截器请求头
     */
    public static final DefaultHttpClient createHttpClient(int timeout, int retryCount, Proxy proxy) {
        return createHttpClient(null, retryCount, timeout, proxy);
    }

    /**
     * 支持代理,默认超时时间为timeout
     */
    public static final DefaultHttpClient createHttpClient(int retryCount, Proxy proxy) {
        return createHttpClient(null, retryCount, timeout, proxy);
    }

    /**
     * 不需要代理，超时时间为timeout
     */
    public static final DefaultHttpClient createHttpClient(int retryCount) {
        return createHttpClient(null, retryCount, timeout, null);
    }

    /**
     * 不需要代理，超时时间为timeout，无特殊head头,重试三次
     *
     * @return
     */
    public static final DefaultHttpClient createHttpClient() {
        return createHttpClient(3);
    }

    private final static class RequestInterceptor extends GzipHttpRequestInterceptor {

        private Map<String, String> header;

        public RequestInterceptor(Map<String, String> header) {
            super();
            this.header = header;
        }

        @Override
        public void process(HttpRequest request, HttpContext context)
                throws HttpException, IOException {
            super.process(request, context);
            if (header != null) {
                for (Entry<String, String> entry : header.entrySet()) {
                    request.addHeader(entry.getKey(), entry.getValue());
                }
            }
        }
    }


    public static class MySSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

//        private  SSLSocketFactory delegate=sslContext.getSocketFactory();
        private  SSLSocketFactory delegate;

//        SSLContext sslcontext = SSLContext.getInstance("TLSv1");
//sslcontext.init(null, null, null);
//        SSLSocketFactory NoSSLv3Factory = new NoSSLv3SocketFactory(sslcontext.getSocketFactory());
//
//HttpsURLConnection.setDefaultSSLSocketFactory(NoSSLv3Factory);
//        l_connection = (HttpsURLConnection) l_url.openConnection();
//l_connection.connect();

        public MySSLSocketFactory(KeyStore keyStore, KeyStore truststore) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, KeyManagementException {
            super(keyStore, KEY_STORE_PASSWORD, truststore);
            //x509
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, KEY_STORE_PASSWORD.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(truststore);

            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
//            sslContext.createSSLEngine();
        }


        public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException,
                KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

//            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//            trustManagerFactory.init(truststore);
//            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            sslContext.init(null, new TrustManager[]{tm}, null);

        }


        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
                throws IOException, UnknownHostException {
            Socket ssl= sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
            if (ssl instanceof SSLSocket)
                upgradeTLS((SSLSocket)ssl);
            return ssl;

        }
        @Override
        public Socket createSocket() throws IOException {
           Socket ssl=sslContext.getSocketFactory().createSocket();
            if (ssl instanceof SSLSocket)
                upgradeTLS((SSLSocket)ssl);
            return ssl;
        }



//        @Override
//        public String[] getDefaultCipherSuites() {
//            return cipherSuites;
//        }
//        @Override
//        public String[] getSupportedCipherSuites() {
//            return cipherSuites;
//        }

//        @Override
//        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
//            return makeSocketSafe(delegate.createSocket(s, host, port, autoClose));
//        }


        private void upgradeTLS(SSLSocket ssl) {
            // Android 5.0+ (API level21) provides reasonable default settings
            // but it still allows SSLv3
            // https://developer.android.com/about/versions/android-5.0-changes.html#ssl
            if (protocols != null) {
                ssl.setEnabledProtocols(protocols);
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && cipherSuites != null) {
                ssl.setEnabledCipherSuites(cipherSuites);
            }
        }

        private Socket makeSocketSafe(Socket socket) {
            if (socket instanceof SSLSocket) {
                socket = new NoSSLv3SSLSocket((SSLSocket) socket);
            }
            return socket;
        }
    }


    private static class NoSSLv3SSLSocket extends DelegateSSLSocket {

        private NoSSLv3SSLSocket(SSLSocket delegate) {
            super(delegate);

        }

        @Override
        public void setEnabledProtocols(String[] protocols) {
            if (protocols != null && protocols.length == 1 && "SSLv3".equals(protocols[0])) {

                List<String> enabledProtocols = new ArrayList<String>(Arrays.asList(delegate.getEnabledProtocols()));
                if (enabledProtocols.size() > 1) {
                    enabledProtocols.remove("SSLv3");
                    System.out.println("Removed SSLv3 from enabled protocols");
                } else {
                    System.out.println("SSL stuck with protocol available for " + String.valueOf(enabledProtocols));
                }
                protocols = enabledProtocols.toArray(new String[enabledProtocols.size()]);
            }

            super.setEnabledProtocols(protocols);
        }
    }

    public static class DelegateSSLSocket extends SSLSocket {

        protected final SSLSocket delegate;

        DelegateSSLSocket(SSLSocket delegate) {
            this.delegate = delegate;
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public String[] getEnabledCipherSuites() {
            return delegate.getEnabledCipherSuites();
        }

        @Override
        public void setEnabledCipherSuites(String[] suites) {
            delegate.setEnabledCipherSuites(suites);
        }

        @Override
        public String[] getSupportedProtocols() {
            return delegate.getSupportedProtocols();
        }

        @Override
        public String[] getEnabledProtocols() {
            return delegate.getEnabledProtocols();
        }

        @Override
        public void setEnabledProtocols(String[] protocols) {
            delegate.setEnabledProtocols(protocols);
        }

        @Override
        public SSLSession getSession() {
            return delegate.getSession();
        }

        @Override
        public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
            delegate.addHandshakeCompletedListener(listener);
        }

        @Override
        public void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
            delegate.removeHandshakeCompletedListener(listener);
        }

        @Override
        public void startHandshake() throws IOException {
            delegate.startHandshake();
        }

        @Override
        public void setUseClientMode(boolean mode) {
            delegate.setUseClientMode(mode);
        }

        @Override
        public boolean getUseClientMode() {
            return delegate.getUseClientMode();
        }

        @Override
        public void setNeedClientAuth(boolean need) {
            delegate.setNeedClientAuth(need);
        }

        @Override
        public void setWantClientAuth(boolean want) {
            delegate.setWantClientAuth(want);
        }

        @Override
        public boolean getNeedClientAuth() {
            return delegate.getNeedClientAuth();
        }

        @Override
        public boolean getWantClientAuth() {
            return delegate.getWantClientAuth();
        }

        @Override
        public void setEnableSessionCreation(boolean flag) {
            delegate.setEnableSessionCreation(flag);
        }

        @Override
        public boolean getEnableSessionCreation() {
            return delegate.getEnableSessionCreation();
        }

        @Override
        public void bind(SocketAddress localAddr) throws IOException {
            delegate.bind(localAddr);
        }

        @Override
        public synchronized void close() throws IOException {
            delegate.close();
        }

        @Override
        public void connect(SocketAddress remoteAddr) throws IOException {
            delegate.connect(remoteAddr);
        }

        @Override
        public void connect(SocketAddress remoteAddr, int timeout) throws IOException {
            delegate.connect(remoteAddr, timeout);
        }

        @Override
        public SocketChannel getChannel() {
            return delegate.getChannel();
        }

        @Override
        public InetAddress getInetAddress() {
            return delegate.getInetAddress();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return delegate.getInputStream();
        }

        @Override
        public boolean getKeepAlive() throws SocketException {
            return delegate.getKeepAlive();
        }

        @Override
        public InetAddress getLocalAddress() {
            return delegate.getLocalAddress();
        }

        @Override
        public int getLocalPort() {
            return delegate.getLocalPort();
        }

        @Override
        public SocketAddress getLocalSocketAddress() {
            return delegate.getLocalSocketAddress();
        }

        @Override
        public boolean getOOBInline() throws SocketException {
            return delegate.getOOBInline();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return delegate.getOutputStream();
        }

        @Override
        public int getPort() {
            return delegate.getPort();
        }

        @Override
        public synchronized int getReceiveBufferSize() throws SocketException {
            return delegate.getReceiveBufferSize();
        }

        @Override
        public SocketAddress getRemoteSocketAddress() {
            return delegate.getRemoteSocketAddress();
        }

        @Override
        public boolean getReuseAddress() throws SocketException {
            return delegate.getReuseAddress();
        }

        @Override
        public synchronized int getSendBufferSize() throws SocketException {
            return delegate.getSendBufferSize();
        }

        @Override
        public int getSoLinger() throws SocketException {
            return delegate.getSoLinger();
        }

        @Override
        public synchronized int getSoTimeout() throws SocketException {
            return delegate.getSoTimeout();
        }

        @Override
        public boolean getTcpNoDelay() throws SocketException {
            return delegate.getTcpNoDelay();
        }

        @Override
        public int getTrafficClass() throws SocketException {
            return delegate.getTrafficClass();
        }

        @Override
        public boolean isBound() {
            return delegate.isBound();
        }

        @Override
        public boolean isClosed() {
            return delegate.isClosed();
        }

        @Override
        public boolean isConnected() {
            return delegate.isConnected();
        }

        @Override
        public boolean isInputShutdown() {
            return delegate.isInputShutdown();
        }

        @Override
        public boolean isOutputShutdown() {
            return delegate.isOutputShutdown();
        }

        @Override
        public void sendUrgentData(int value) throws IOException {
            delegate.sendUrgentData(value);
        }

        @Override
        public void setKeepAlive(boolean keepAlive) throws SocketException {
            delegate.setKeepAlive(keepAlive);
        }

        @Override
        public void setOOBInline(boolean oobinline) throws SocketException {
            delegate.setOOBInline(oobinline);
        }

        @Override
        public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
            delegate.setPerformancePreferences(connectionTime, latency, bandwidth);
        }

        @Override
        public synchronized void setReceiveBufferSize(int size) throws SocketException {
            delegate.setReceiveBufferSize(size);
        }

        @Override
        public void setReuseAddress(boolean reuse) throws SocketException {
            delegate.setReuseAddress(reuse);
        }

        @Override
        public synchronized void setSendBufferSize(int size) throws SocketException {
            delegate.setSendBufferSize(size);
        }

        @Override
        public void setSoLinger(boolean on, int timeout) throws SocketException {
            delegate.setSoLinger(on, timeout);
        }

        @Override
        public synchronized void setSoTimeout(int timeout) throws SocketException {
            delegate.setSoTimeout(timeout);
        }

        @Override
        public void setTcpNoDelay(boolean on) throws SocketException {
            delegate.setTcpNoDelay(on);
        }

        @Override
        public void setTrafficClass(int value) throws SocketException {
            delegate.setTrafficClass(value);
        }

        @Override
        public void shutdownInput() throws IOException {
            delegate.shutdownInput();
        }

        @Override
        public void shutdownOutput() throws IOException {
            delegate.shutdownOutput();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }
    }

    private static class GzipHttpRequestInterceptor implements HttpRequestInterceptor {
        public void process(final HttpRequest request, final HttpContext context)
                throws HttpException, IOException {
            request.setHeader("Accept-Encoding", ENCODING_GZIP);
        }
    }

    private final static class GzipHttpResponseInterceptor implements
            HttpResponseInterceptor {

        public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return;
            }
            final Header encoding = entity.getContentEncoding();
            if (encoding != null) {
                for (HeaderElement element : encoding.getElements()) {
                    if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
                        response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                        break;
                    }
                }
            }
        }
    }

    private final static class GzipDecompressingEntity extends HttpEntityWrapper {

        public GzipDecompressingEntity(final HttpEntity entity) {
            super(entity);
        }

        @Override
        public InputStream getContent() throws IOException, IllegalStateException {
            InputStream wrappedin = wrappedEntity.getContent();
            return new GZIPInputStream(wrappedin);
        }

        @Override
        public long getContentLength() {
            return -1;
        }
    }

    /**
     * HttpRequestInterceptor for DefaultHttpClient
     */
    public static HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {
        @Override
        public void process(final HttpRequest request, final HttpContext context) {
            AuthState authState = (AuthState) context
                    .getAttribute(ClientContext.TARGET_AUTH_STATE);
            CredentialsProvider credsProvider = (CredentialsProvider) context
                    .getAttribute(ClientContext.CREDS_PROVIDER);
            HttpHost targetHost = (HttpHost) context
                    .getAttribute(ExecutionContext.HTTP_TARGET_HOST);

            if (authState.getAuthScheme() == null) {
                AuthScope authScope = new AuthScope(targetHost.getHostName(),
                        targetHost.getPort());
                Credentials creds = credsProvider.getCredentials(authScope);
                if (creds != null) {
                    authState.setAuthScheme(new BasicScheme());
                    authState.setCredentials(creds);
                }
            }
            request.setHeader("Accept-Encoding", ENCODING_GZIP);
        }
    };

}
