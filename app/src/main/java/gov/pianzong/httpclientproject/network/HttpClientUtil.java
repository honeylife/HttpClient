package gov.pianzong.httpclientproject.network;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import gov.pianzong.httpclientproject.base.DnApplication;

/**
 * @CreateDate: 2020/3/27
 * @Author: honeylife
 * @Description:
 * @Version:
 */
public class HttpClientUtil {

    public String doPost(String url, Map<String, String> map, String charset) {
        HttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        try {
            httpClient = new SSLClient();
            httpPost = new HttpPost(url);
            //设置参数
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> elem = (Map.Entry<String, String>) iterator.next();
                list.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
            }
            if (list.size() > 0) {
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, charset);
                httpPost.setEntity(entity);
            }
            HttpResponse response = httpClient.execute(httpPost);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, charset);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
    public static String doGet(String url) {
        HttpClient httpClient = null;
        HttpGet httpGet = null;
        String result = null;
        try {
            httpClient = new SSLClient();
            httpGet = new HttpGet(url);

            HttpResponse response = httpClient.execute(httpGet);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static String get(String url){
        DefaultHttpClient httpclient = new DefaultHttpClient();

//        KeyStore trustStore  = null;
        try {
//            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
////            FileInputStream instream = new FileInputStream(new File("/Users/honeylife/Documents/duoniuspace/HttpclientProject/app/src/main/assets/server.bks"));
//            try {
//                trustStore.load(DnApplication.getInstance().getAssets().open("server.bks"), "donews123".toCharArray());
//            } finally {
////                instream.close();
//            }
            SSLContext sslContext = SSLContext.getInstance("SSL");


            //客户端证书
            KeyStore keyStore = KeyStore.getInstance("bks");
            keyStore.load(DnApplication.getInstance().getAssets().open("client.bks"), "donews123".toCharArray());

            //服务证书
            KeyStore trustStore = KeyStore.getInstance("bks");// 服务器证书
            trustStore.load(DnApplication.getInstance().getAssets().open("server.bks"), "donews123".toCharArray());

//            sslContext.init(null, (tm != null) ? new X509TrustManager[] { tm } : null, null);

            //x509
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "donews123".toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);



//            javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
//
//            SSLSocketFactory sslSocketFactory1=sslContext.getSocketFactory();
//
            SSLSocketFactory socketFactory= new MySSLSocketFactory(keyStore,trustStore);
            socketFactory.setHostnameVerifier(new X509HostnameVerifier(){
                @Override
                public boolean verify(String host, SSLSession session) {
                    return true;
                }

                @Override
                public void verify(String host, SSLSocket ssl) throws IOException {

                }

                @Override
                public void verify(String host, X509Certificate cert) throws SSLException {

                }

                @Override
                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {

                }
            });


            Scheme sch = new Scheme("https",socketFactory, 443);
//            Scheme sch = new Scheme("https", new org.apache.http.conn.ssl.SSLSocketFactory(sslSocketFactory), 443);
            httpclient.getConnectionManager().getSchemeRegistry().register(sch);

            HttpGet httpget = new HttpGet(url);

            System.out.println("executing request" + httpget.getRequestLine());

            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            if (entity != null) {
                System.out.println("Response content length: " + entity.getContentLength());
                System.out.println("Response content length: " + EntityUtils.toString(entity));
            }
            if (entity != null) {
                entity.consumeContent();
            }
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | KeyManagementException | UnrecoverableKeyException e) {
            e.printStackTrace();
        }

        // When HttpClient instance is no longer needed,
        // shut down the connection manager to ensure
        // immediate deallocation of all system resources
        httpclient.getConnectionManager().shutdown();


//        System.Net.ServicePointManager.ServerCertificateValidationCallback = (sender, certificate, chain, sslPolicyErrors) =>
//        {
//            Console.WriteLine($"****************************************************************************************************");
//
//            return true;
//        };

        return "";
    }

    public static class MySSLSocketFactory extends org.apache.http.conn.ssl.SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

        //        private  SSLSocketFactory delegate=sslContext.getSocketFactory();
        private org.apache.http.conn.ssl.SSLSocketFactory delegate;

//        SSLContext sslcontext = SSLContext.getInstance("TLSv1");
//sslcontext.init(null, null, null);
//        SSLSocketFactory NoSSLv3Factory = new NoSSLv3SocketFactory(sslcontext.getSocketFactory());
//
//HttpsURLConnection.setDefaultSSLSocketFactory(NoSSLv3Factory);
//        l_connection = (HttpsURLConnection) l_url.openConnection();
//l_connection.connect();

        public MySSLSocketFactory(KeyStore keyStore, KeyStore truststore) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, KeyManagementException {
            super(keyStore, "donews123", truststore);
            //x509
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "donews123".toCharArray());

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
//            if (ssl instanceof SSLSocket)
//                upgradeTLS((SSLSocket)ssl);
            return ssl;

        }
        @Override
        public Socket createSocket() throws IOException {
            Socket ssl=sslContext.getSocketFactory().createSocket();
//            if (ssl instanceof SSLSocket)
//                upgradeTLS((SSLSocket)ssl);
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



    }
}
