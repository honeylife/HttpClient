package gov.pianzong.httpclientproject.urlConnection;

import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import gov.pianzong.httpclientproject.base.DnApplication;
import gov.pianzong.httpclientproject.ssl.EasyX509TrustManager;

/**
 * @CreateDate: 2020/3/30
 * @Author: honeylife
 * @Description:
 * @Version:
 */
public class HttpUrlConnectionUtil {


    private static final String KEY_STORE_TYPE_BKS = "bks";//android
    private static final String KEY_STORE_PASSWORD = "donews123";

    //当前证书对应的密码，勿修改！
//    private static final String KEY_STORE_PASSWORD = "123456";
//    private static final String KEY_STORE_TRUST_PASSWORD = "123456";
    private static final String KEY_STORE_TRUST_PASSWORD = "donews123";
    public String get(String path) {

        URL url = null;
        StringBuilder sb = new StringBuilder();
        try {
            url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //设置请求方式
            connection.setRequestMethod("GET");
            //连接
            connection.connect();
            //得到响应码
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection
                        .getInputStream()));
                String temp;
                while ((temp = reader.readLine()) != null) {
                    sb.append(temp);
                }
                reader.close();
            }
            connection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //得到connection对象。

        return sb.toString();

    }


    public static String getRequest(String urlString) throws Exception {
        HttpsURLConnection httpsURLConnection;

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

        context.init(keyManagerFactory.getKeyManagers(), new TrustManager[]{new EasyX509TrustManager(trustStore)}, new SecureRandom());
//        context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());

        URL url = new URL(urlString);

        httpsURLConnection = (HttpsURLConnection) url.openConnection();
        httpsURLConnection.setSSLSocketFactory(context.getSocketFactory());
        httpsURLConnection.setRequestMethod("GET");
        httpsURLConnection.setReadTimeout(5000);

        if (httpsURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = httpsURLConnection.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = inputStream.read(bytes)) != -1) {
                byteArrayOutputStream.write(bytes, 0, len);
            }
            String values = new String(byteArrayOutputStream.toByteArray());
            inputStream.close();
            byteArrayOutputStream.close();

            Log.e("TAG","===="+values);
            return values;
        }
        return null;
    }
}

