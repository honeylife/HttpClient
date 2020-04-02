package gov.pianzong.httpclientproject.network;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.logging.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import gov.pianzong.httpclientproject.base.DnApplication;


/**
 * @CreateDate: 2020/3/30
 * @Author: honeylife
 * @Description:
 * @Version:
 */
public class SendHttps {
    private static Logger log = Logger.getLogger(String.valueOf(SendHttps.class));


    public static String sendToHttps(String reqMsg, String url, Map<String, String> headMap) {
        log.info("keyFactory");
        //初始化KeyManager
        KeyManagerFactory keyFactory = null;
        try {
            keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            KeyStore keystore = KeyStore.getInstance("bks");
            keystore.load(DnApplication.getInstance().getAssets().open("client.bks"), "donews123".toCharArray());
            keyFactory.init(keystore, "donews123".toCharArray());

        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CertificateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        KeyManager[] keyManagers = keyFactory.getKeyManagers();
        //初始化Trust Manager
        log.info("keyFactory ="+keyFactory);
        TrustManagerFactory trustFactory = null;

        try {
            trustFactory = TrustManagerFactory.getInstance("SunX509");
        } catch (NoSuchAlgorithmException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        KeyStore tsstore;
        try {
            tsstore = KeyStore.getInstance("bks");
            tsstore.load(DnApplication.getInstance().getAssets().open("server.bks"), "donews123".toCharArray());
            trustFactory.init(tsstore);
            log.info("tsstore ="+tsstore+"   ||  trustFactory = "+trustFactory);
        } catch (KeyStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CertificateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        TrustManager[] trustManagers = trustFactory.getTrustManagers();
        log.info("trustManagers ="+trustManagers);
        //注册HtpClient
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, null);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyManagementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        log.info("sslContext ="+sslContext);

//        //设置规则限制
//        SSLConnectionSocketFactory ssf = new SSLConnectionSocketFactory(sslContext,
//                new String[]{"TLSv1","TLSv1.1","TLSv1.2"},null,
//                new HttpsHostnameVerifier());
//        //注册
//        Registry<ConnectionSocketFactory> socketFactoryRegistry = null;
//        socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
//                .register("http", PlainConnectionSocketFactory.INSTANCE)
//                .register("https", ssf).build();
//        //池化管理
//        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
//        //创建httpClient
//        CloseableHttpClient httpClient;
//        httpClient = HttpClients.custom().setConnectionManager(connManager).build();
//
//        //设置httpPost
//        HttpPost httpPost = new HttpPost(url);
//        if ((!headMap.isEmpty()) && (headMap.size() > 0)) {
//            Set<String> keys = headMap.keySet();
//            for (Iterator<String> i = keys.iterator(); i.hasNext(); ) {
//                String key = ObjectUtils.toString(i.next());
//                if("host".equals(key)){
//                    continue;
//                }else{
//                    log.info("key="+key+",value="+(String)headMap.get(key));
//                    httpPost.addHeader(key, (String)headMap.get(key));
//                }
//            }
//        }
//        StringEntity reqEntity = new StringEntity(reqMsg, "UTF-8");
//
//        Header[] types = httpPost.getHeaders("Content-Type");
//        if ((types == null) || (types.length < 1)) {
//            httpPost.addHeader("Content-Type", "application/json;charset=utf-8");
//        }
//
//        httpPost.setEntity(reqEntity);
//        CloseableHttpResponse response;
//        try {
//            response = httpClient.execute(httpPost);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        int statusCode = response.getStatusLine().getStatusCode();
//        if (statusCode != HttpStatus.SC_OK) {
//            httpPost.abort();
//            return JsonUtils.setError("Fail to connect . response code = " + statusCode + ". error.");
//        }
//
//        HttpEntity entity = response.getEntity();
//        String result = null;
//        try {
//            if (entity != null) {
//                result = EntityUtils.toString(entity, "utf-8");
//            }
//            EntityUtils.consume(entity);
//            response.close();
//        } catch (Exception e) {
//            log.error("Change charset to utf-8 error.");
//            return JsonUtils.setError("Change charset to utf-8 error.");
//        }
        return "result";
    }
}
