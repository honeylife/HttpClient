package gov.pianzong.httpclientproject.ssl;

import android.os.Build;
import android.util.Log;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;

import gov.pianzong.httpclientproject.base.DnApplication;

/**
 * @CreateDate: 2020/3/31
 * @Author: honeylife
 * @Description:
 * @Version:
 */
public class EasySSLSocketFactory implements SocketFactory, LayeredSocketFactory {
    //android应该选用 bks证书格式
    private static final String KEY_STORE_TYPE_BKS = "bks";//android
    private static final String KEY_STORE_PASSWORD = "donews123";

    //当前证书对应的密码，勿修改！
//    private static final String KEY_STORE_PASSWORD = "123456";
//    private static final String KEY_STORE_TRUST_PASSWORD = "123456";
    private static final String KEY_STORE_TRUST_PASSWORD = "donews123";
    private SSLContext sslcontext = null;


    public static final X509HostnameVerifier ALLOW_ALL_HOSTNAME_VERIFIER
            = new AllowAllHostnameVerifier();

    public static final X509HostnameVerifier BROWSER_COMPATIBLE_HOSTNAME_VERIFIER
            = new BrowserCompatHostnameVerifier();

    public static final X509HostnameVerifier STRICT_HOSTNAME_VERIFIER
            = new StrictHostnameVerifier();
    private X509HostnameVerifier hostnameVerifier = BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;


    private static SSLContext createEasySSLContext() throws IOException {
        try {
            SSLContext context = SSLContext.getInstance("TLSv1");

            //客户端证书
            KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE_BKS);
            keyStore.load(DnApplication.getInstance().getAssets().open("client.bks"), KEY_STORE_PASSWORD.toCharArray());

            //服务证书
            KeyStore trustStore = KeyStore.getInstance(KEY_STORE_TYPE_BKS);// 服务器证书
            trustStore.load(DnApplication.getInstance().getAssets().open("server.bks"), KEY_STORE_TRUST_PASSWORD.toCharArray());

            //x509
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, KEY_STORE_PASSWORD.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
//            context.init(keyManagerFactory.getKeyManagers(), new TrustManager[]{new EasyX509TrustManager(trustStore)}, new SecureRandom());
            context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
            return context;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private SSLContext getSSLContext() throws IOException {
        if (this.sslcontext == null) {
            this.sslcontext = createEasySSLContext();
        }
        return this.sslcontext;
    }

    /**
     * @see org.apache.http.conn.scheme.SocketFactory#connectSocket(java.net.Socket, java.lang.String, int,
     * java.net.InetAddress, int, org.apache.http.params.HttpParams)
     */
    public Socket connectSocket(Socket sock, String host, int port, InetAddress localAddress, int localPort,
                                HttpParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
        int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
        int soTimeout = HttpConnectionParams.getSoTimeout(params);
        InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
        SSLSocket sslsock = (SSLSocket) ((sock != null) ? sock : createSocket());
        if ((localAddress != null) || (localPort > 0)) {
            // we need to bind explicitly
            if (localPort < 0) {
                localPort = 0; // indicates "any"
            }
            InetSocketAddress isa = new InetSocketAddress(localAddress, localPort);
            sslsock.bind(isa);
        }

        sslsock.connect(remoteAddress, connTimeout);
        sslsock.setSoTimeout(soTimeout);


        try {
            // BEGIN android-added
            /*
             * Make sure we have started the handshake before verifying.
             * Otherwise when we go to the hostname verifier, it directly calls
             * SSLSocket#getSession() which swallows SSL handshake errors.
             */
            sslsock.startHandshake();
            // END android-added
//            hostnameVerifier.verify(host, sslsock);
            if (this.hostnameVerifier != null) {
                try {
                    HostNameSetter.setServerNameIndication(host, sslsock);
                    this.hostnameVerifier.verify(host, sslsock);
                    // verifyHostName() didn't blowup - good!
                    Log.e("TAG", "==host==" + host);
                } catch (IOException iox) {
                    iox.printStackTrace();
                }
            }
            // verifyHostName() didn't blowup - good!
        } catch (IOException iox) {
            // close the socket before re-throwing the exception
            Log.e("TAG", "==host=iox=" + host);
            try {
                sslsock.close();
            } catch (Exception x) { /*ignore*/ }
            throw iox;
        }

        return sslsock;
    }

    /**
     * @see org.apache.http.conn.scheme.SocketFactory#createSocket()
     */
    public Socket createSocket() throws IOException {
        return getSSLContext().getSocketFactory().createSocket();
    }

    /**
     * @see org.apache.http.conn.scheme.SocketFactory#isSecure(java.net.Socket)
     */
    public boolean isSecure(Socket socket) throws IllegalArgumentException {
        return true;
    }

    /**
     * @see org.apache.http.conn.scheme.LayeredSocketFactory#createSocket(java.net.Socket, java.lang.String, int,
     * boolean)
     */
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
            UnknownHostException {
//        SSLSocket sslSocket = (SSLSocket) this.socketfactory.createSocket(
//                socket,
//                host,
//                port,
//                autoClose
//        );
        SSLSocket sslSocket = (SSLSocket) getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
//        hostnameVerifier.verify(host, sslSocket);
        Log.e("TAG", "===sslSocket=" + sslSocket);


        // Android specific code to enable SNI
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (this.hostnameVerifier != null) {
                try {
                    HostNameSetter.setServerNameIndication(host, sslSocket);
                    this.hostnameVerifier.verify(host, sslSocket);
                    Log.e("TAG", "==host==" + host);
                    // verifyHostName() didn't blowup - good!
                } catch (IOException iox) {
                    iox.printStackTrace();
                    Log.e("TAG", "==host==" + iox);
                }
            }
        }
        // End of Android specific code
//        return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
        return sslSocket;
    }

    // -------------------------------------------------------------------
    // javadoc in org.apache.http.conn.scheme.SocketFactory says :
    // Both Object.equals() and Object.hashCode() must be overridden
    // for the correct operation of some connection managers
    // -------------------------------------------------------------------
    public boolean equals(Object obj) {
        return ((obj != null) && obj.getClass().equals(EasySSLSocketFactory.class));
    }

    public int hashCode() {
        return EasySSLSocketFactory.class.hashCode();
    }

    public void setHostnameVerifier(X509HostnameVerifier hostnameVerifier) {
        if (hostnameVerifier == null) {
            throw new IllegalArgumentException("Hostname verifier may not be null");
        }
        this.hostnameVerifier = hostnameVerifier;
    }

    public X509HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }


    /**
     * +     * Uses the underlying implementation to support Server Name Indication (SNI).
     * +     * @author Michael Locher <cmbntr@gmail.com>
     * +
     */
    private static class HostNameSetter {

        private static final AtomicReference<HostNameSetter> CURRENT = new AtomicReference<HostNameSetter>();

        private final WeakReference<Class<?>> cls;
        private final WeakReference<Method> setter;

        private HostNameSetter(Class<?> clazz, Method setter) {
            this.cls = new WeakReference<Class<?>>(clazz);
            this.setter = setter == null ? null : new WeakReference<Method>(setter);
        }

        private static Method init(Class<?> cls) {
            Method s = null;
            try {
                s = cls.getMethod("setHostname", String.class);
            } catch (SecurityException e) {
                initFail(e);
            } catch (NoSuchMethodException e) {
                initFail(e);
            }
            CURRENT.set(new HostNameSetter(cls, s));
            return s;
        }

        private static void initFail(Exception e) {
            // ignore

            Log.e("TAG", "=====" + e);

            // Android specific code to enable SNI
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                if (Log.isLoggable("TAG", Log.DEBUG)) {
//                    Log.d("TAG", "Enabling SNI for " + target);
//                }
//                try {
//                    Method method = sslsock.getClass().getMethod("setHostname", String.class);
//                    method.invoke(sslsock, target);
//                } catch (Exception ex) {
//                    if (Log.isLoggable(TAG, Log.DEBUG)) {
//                        Log.d(TAG, "SNI configuration failed", ex);
//                    }
//                }
//            }
            // End of Android specific code
        }

        /**
         * +         * Invokes the {@code #setName(String)} method if one is present.
         * +         *
         * +         * @param hostname the name to set
         * +         * @param sslsock the socket
         * +
         */
        public static void setServerNameIndication(String hostname, SSLSocket sslsock) {
            final Class<?> cls = sslsock.getClass();
            final HostNameSetter current = CURRENT.get();
            final Method setter = (current == null) ? init(cls) : current.reuse(cls);
            if (setter != null) {
                try {
                    setter.invoke(sslsock, hostname);
                } catch (IllegalArgumentException e) {
                    setServerNameIndicationFail(e);
                } catch (IllegalAccessException e) {
                    setServerNameIndicationFail(e);
                } catch (InvocationTargetException e) {
                    setServerNameIndicationFail(e);
                }
            }
        }

        private static void setServerNameIndicationFail(Exception e) {
            // ignore
        }

        private Method reuse(Class<?> cls) {
            final boolean wrongClass = this.cls.get() != cls;
            if (wrongClass) {
                return init(cls);
            }

            final boolean setterNotSupported = this.setter == null;
            if (setterNotSupported) {
                return null;
            }

            final Method s = setter.get();
            final boolean setterLost = s == null;
            return setterLost ? init(cls) : s;
        }
    }

    class MyKeyManager implements X509KeyManager {

        private final X509KeyManager keyManager;

        MyKeyManager(X509KeyManager keyManager) {
            this.keyManager = keyManager;
        }


        @Override
        public String chooseClientAlias(String[] strings, Principal[] principals, Socket socket) {
            return this.keyManager.chooseClientAlias(strings, principals, socket);
        }


        @Override
        public String chooseServerAlias(String s, Principal[] principals, Socket socket) {
            return keyManager.chooseServerAlias(s, principals, socket);
        }


        @Override
        public X509Certificate[] getCertificateChain(String s) {
            return keyManager.getCertificateChain(s);
        }


        @Override
        public String[] getClientAliases(String s, Principal[] principals) {
            return keyManager.getClientAliases(s, principals);
        }


        @Override
        public String[] getServerAliases(String s, Principal[] principals) {
            return keyManager.getServerAliases(s, principals);
        }


        @Override
        public PrivateKey getPrivateKey(String s) {
            return keyManager.getPrivateKey(s);
        }
    }
}