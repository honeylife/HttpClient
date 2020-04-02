package gov.pianzong.httpclientproject.network;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import gov.pianzong.httpclientproject.base.DnApplication;


/**
 * @CreateDate: 2020/3/26
 * @Author: honeylife
 * @Description: 网络请求总的工具类
 * @Version:
 */
public class NetworkTool {
    private final static String TAG = "NetworkTool";

//        HttpURLConnection
        //android应该选用 bks证书格式
        private static final String KEY_STORE_TYPE_BKS = "bks";//android
     public NetworkTool() {
    }
        //当前证书对应的密码，勿修改！
        private static final String KEY_STORE_PASSWORD = "donews123";/**
        private static final String KEY_STORE_TRUST_PASSWORD = "donews123"; * @param url 网络球球的地址
     * @return 返回网络请求的数据
     */
    public String getContentFromUrl(String url) {

        HttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);

        HttpResponse response=null;
        try {
            response = httpClient.execute(get);
            HttpEntity entity = response.getEntity();
            Log.e(TAG, "状态码：" + response.getStatusLine());
            if (entity != null) {
                Log.e(TAG, "内容的长度：" + entity.getContentLength());
                String message = EntityUtils.toString(entity, "utf-8");
                Log.e(TAG, message);
                return message;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            get.abort();
            httpClient.getConnectionManager().shutdown();
        }
        Log.d(TAG, url);
        return "这是数据哦，测一测，哈哈哈。";
    }

    public String getData(String url){
        try {
            return HttpUtil.get(url);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (HttpException e) {
            e.printStackTrace();
        }
        return "没有数据可返回，生气啦！";
    }

   public String requestHTTPSPage(String mUrl) {
        InputStream ins = null;
        String result = "";
        try {
           ins = DnApplication.getInstance().getAssets().open("app_pay.cer"); //下载的证书放到项目中的assets目录中
           			CertificateFactory cerFactory = CertificateFactory
           					.getInstance("X.509");
           			Certificate cer = cerFactory.generateCertificate(ins);
           			KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");


                            keyStore.load(DnApplication.getInstance().getAssets().open("client.bks"), KEY_STORE_PASSWORD.toCharArray());
                            keyStore.setCertificateEntry("trust", cer);

            SSLSocketFactory socketFactory = new SSLSocketFactory(keyStore);
            Scheme sch = new Scheme("https", socketFactory, 443);
            HttpClient mHttpClient = new DefaultHttpClient();
            mHttpClient.getConnectionManager().getSchemeRegistry()
                    .register(sch);

            BufferedReader reader = null;
            try {
                Log.d(TAG, "executeGet is in,murl:" + mUrl);
                HttpGet request = new HttpGet();
                request.setURI(new URI(mUrl));
                HttpResponse response = mHttpClient.execute(request);
                if (response.getStatusLine().getStatusCode() != 200) {
                    request.abort();
                    return result;
                }

                reader = new BufferedReader(new InputStreamReader(response
                        .getEntity().getContent()));
                StringBuffer buffer = new StringBuffer();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                result = buffer.toString();
                Log.d(TAG, "mUrl=" + mUrl + "\nresult = " + result);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            try {
                if (ins != null)
                    ins.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

//    public void getPost(String path){
//        // Trust own CA and all self-signed certs
//        SSLContext sslcontext = SSLContexts.custom()
//                .loadTrustMaterial(new File("my.keystore"), "nopassword".toCharArray(),
//                        new TrustSelfSignedStrategy())
//                .build();
//        // Allow TLSv1 protocol only
//        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
//                sslcontext,
//                new String[] { "TLSv1" },
//                null,
//                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
//        CloseableHttpClient httpclient = HttpClients.custom()
//                .setSSLSocketFactory(sslsf)
//                .build();
//        try {
//
//            HttpGet httpget = new HttpGet("https://httpbin.org/");
//
//            System.out.println("Executing request " + httpget.getRequestLine());
//
//            CloseableHttpResponse response = httpclient.execute(httpget);
//            try {
//                HttpEntity entity = response.getEntity();
//
//                System.out.println("----------------------------------------");
//                System.out.println(response.getStatusLine());
//                EntityUtils.consume(entity);
//            } finally {
//                response.close();
//            }
//        } finally {
//            httpclient.close();
//        }
//    }
}
