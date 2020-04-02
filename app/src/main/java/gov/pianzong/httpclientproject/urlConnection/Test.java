package gov.pianzong.httpclientproject.urlConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import gov.pianzong.httpclientproject.base.DnApplication;

/**
 * @CreateDate: 2020/3/30
 * @Author: honeylife
 * @Description:
 * @Version:
 */
public class Test {
    private String serverUrl = "https://adopt.xg.tagtic.cn/init?sdkver=5";
    private SSLSocketFactory sslFactory = null;

    public void run() {
        try {
            String requestBody ="";
            HttpURLConnection connection = doHttpRequest(serverUrl, "GET",requestBody, null);
            int responseCode = getResponseCode(connection);
            String responseBody = getResponseBodyAsString(connection);
            connection.disconnect();
            System.out.println("response code=" + responseCode + ", body=[" + responseBody + "]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Test test = new Test();
        test.run();
    }

    private synchronized SSLSocketFactory getSSLFactory() throws Exception {
        if (sslFactory == null) {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] tm = { new MyX509TrustManager() };

            KeyStore truststore = KeyStore.getInstance("BKS");
            truststore.load(DnApplication.getInstance().getAssets().open("server.bks"), "donews123".toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(truststore, "donews123".toCharArray());

            sslContext.init(kmf.getKeyManagers(), tm, new java.security.SecureRandom());
            sslFactory = sslContext.getSocketFactory();
        }
        return sslFactory;
    }

    private HttpURLConnection doHttpRequest(String requestUrl, String method, String body, Map<String, String> header) throws Exception {
        HttpURLConnection conn;

        if (method == null || method.length() == 0 ) {
            method = "GET";
        }
        if ("GET".equals(method) && body != null && ! body.isEmpty()) {
            requestUrl = requestUrl + "?" + body;
        }

        URL url = new URL(requestUrl);
        conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestMethod(method);

        if (requestUrl.matches("^(https?)://.*$")){
            ((HttpsURLConnection) conn).setSSLSocketFactory(this.getSSLFactory());
        }

        if (header != null) {
            for (String key : header.keySet()) {
                conn.setRequestProperty(key, header.get(key));
            }
        }

        if (body != null && ! body.isEmpty()) {
            if (!method.equals("GET") ) {
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(body);
                wr.close();
            }
        }
        conn.connect();
        return conn;
    }

    public int getResponseCode(HttpURLConnection connection) throws IOException {
        return connection.getResponseCode();
    }

    public String getResponseBodyAsString(HttpURLConnection connection) throws Exception {
        BufferedReader reader = null;
        if (connection.getResponseCode() == 200) {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        }

        StringBuffer buffer = new StringBuffer();
        String line=null;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }

    class MyX509TrustManager implements X509TrustManager {
        private X509TrustManager sunJSSEX509TrustManager;

        MyX509TrustManager() throws Exception {
            // create a "default" JSSE X509TrustManager.
            KeyStore ks = KeyStore.getInstance("bks");
            ks.load(DnApplication.getInstance().getAssets().open("client.bks"), "donews123".toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            TrustManager tms[] = tmf.getTrustManagers();

            /*
             * Iterate over the returned trustmanagers, look for an instance of
             * X509TrustManager. If found, use that as our "default" trust manager.
             */
            for (int i = 0; i < tms.length; i++) {
                if (tms[i] instanceof X509TrustManager) {
                    sunJSSEX509TrustManager = (X509TrustManager) tms[i];
                    return;
                }
            }
            throw new Exception("Couldn't initialize");
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                sunJSSEX509TrustManager.checkClientTrusted(chain, authType);
            } catch (CertificateException excep) {
            }
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                sunJSSEX509TrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException excep) {
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return sunJSSEX509TrustManager.getAcceptedIssuers();
        }
    }
}
