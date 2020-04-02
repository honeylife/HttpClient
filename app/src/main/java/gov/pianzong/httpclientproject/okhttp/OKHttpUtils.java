package gov.pianzong.httpclientproject.okhttp;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import gov.pianzong.httpclientproject.base.DnApplication;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @CreateDate: 2020/3/27
 * @Author: honeylife
 * @Description:
 * @Version:
 */
public class OKHttpUtils {

    private static OkHttpClient okHttpClient;
    //定义一个信任所有证书的TrustManager
    final static X509TrustManager trustAllCert = new X509TrustManager() {
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[0];
        }
    };

    public  void get(String url){

//        OkHttpClient okHttpClient = getClient();
        //设置OkHttpClient
        OkHttpClient okHttpClient = new OkHttpClient.Builder().
                sslSocketFactory(new SSLSocketFactoryCompat(trustAllCert), trustAllCert)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();
        //2.创建Request对象，设置一个url地址（百度地址）,设置请求方式。

        Request request = new Request.Builder().url(url).get().build();
        //3.创建一个call对象,参数就是Request请求对象
        Call call = okHttpClient.newCall(request);
        //4.请求加入调度，重写回调方法
        call.enqueue(new Callback() {
            //请求失败执行的方法
            @Override
            public void onFailure( Call call, IOException e) {
                Log.d("TAG","==cccc");
                Log.e("TAG",e+"");
            }
            //请求成功执行的方法
            @Override
            public void onResponse( Call call,  Response response) throws IOException {
//                 response.body().string();
                Log.d("TAG","=="+response.body().string());

            }
        });
    }
    public  void gets(String url){

        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        assert sslContext != null;
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustManager)
                .build();
//        OkHttpClient okHttpClient = getClient();
        //设置OkHttpClient
//        OkHttpClient okHttpClient = new OkHttpClient.Builder().
//                sslSocketFactory(new SSLSocketFactoryCompat(trustAllCert), trustAllCert).build();
        //2.创建Request对象，设置一个url地址（百度地址）,设置请求方式。
        Request request = new Request.Builder().url(url).get().build();
        //3.创建一个call对象,参数就是Request请求对象
        Call call = okHttpClient.newCall(request);
        //4.请求加入调度，重写回调方法
        call.enqueue(new Callback() {
            //请求失败执行的方法
            @Override
            public void onFailure( Call call, IOException e) {
                Log.d("TAG","==cccc");
                Log.e("TAG",e+"");
            }
            //请求成功执行的方法
            @Override
            public void onResponse( Call call,  Response response) throws IOException {
//                 response.body().string();
                Log.d("TAG","=="+response.body().string());

            }
        });
    }
    public  void getss(String url){

        // 获取自签名证书集合，由证书工厂管理
        TrustManager[] trustManagers = new TrustManager[0];
        SSLSocketFactory sslSocketFactory=null;
        InputStream inputStream = null;
        try {
            inputStream = DnApplication.getInstance().getAssets().open("server.bks");
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(inputStream);
            if (certificates.isEmpty()) {
                throw new IllegalArgumentException("expected non-empty set of trusted certificates");
            }
// 将证书保存到 KeyStore 中
            char[] password = "donews123".toCharArray();
            KeyStore keyStore = KeyStore.getInstance("bks");
            keyStore.load(null, password);
            int index = 0;
            for (Certificate certificate : certificates) {
                String certificateAlias = String.valueOf(index++);
                keyStore.setCertificateEntry(certificateAlias, certificate);
            }
// 使用包含自签名证书的 KeyStore 构建一个 X509TrustManager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, password);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
// 使用 X509TrustManager 初始化 SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, new TrustManager[]{trustManagers[0]}, null);
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (IOException | KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }


        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustManagers[0])
                .build();
//        OkHttpClient okHttpClient = getClient();
        //设置OkHttpClient
//        OkHttpClient okHttpClient = new OkHttpClient.Builder().
//                sslSocketFactory(new SSLSocketFactoryCompat(trustAllCert), trustAllCert).build();
        //2.创建Request对象，设置一个url地址（百度地址）,设置请求方式。
        Request request = new Request.Builder().url(url).get().build();
        //3.创建一个call对象,参数就是Request请求对象
        Call call = okHttpClient.newCall(request);
        //4.请求加入调度，重写回调方法
        call.enqueue(new Callback() {
            //请求失败执行的方法
            @Override
            public void onFailure( Call call, IOException e) {
                Log.d("TAG","==cccc");
                Log.e("TAG",e+"");
            }
            //请求成功执行的方法
            @Override
            public void onResponse( Call call,  Response response) throws IOException {
//                 response.body().string();
                Log.d("TAG","=="+response.body().string());

            }
        });
    }



    //android应该选用 bks证书格式
    private static final String KEY_STORE_TYPE_BKS = "bks";//android

    //当前证书对应的密码，勿修改！
    private static final String KEY_STORE_PASSWORD = "donews123";
    private static final String KEY_STORE_TRUST_PASSWORD = "donews123";


    /**
     * 关联Https请求验证证书
     *
     * @param okHttpClient
     */
    public OkHttpClient SSLContext(OkHttpClient.Builder okHttpClient){
        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .allEnabledTlsVersions()
                .allEnabledCipherSuites()
                .build();
        try {
            //设置证书类型
            CertificateFactory factory = CertificateFactory.getInstance("X.509", "BC");
            //打开放在main文件下的 assets 下的Http证书
            InputStream stream = DnApplication.getInstance().getAssets().open("client.bks");
            Certificate certificate = factory.generateCertificate(stream);
            //证书类型
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            //授信证书 , 授信证书密码（应该是服务端证书密码）
            keyStore.load(DnApplication.getInstance().getAssets().open("server.bks"), KEY_STORE_TRUST_PASSWORD.toCharArray());

//            keyStore.load(null, null);
            keyStore.setCertificateEntry("certificate",certificate);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            //证书密码（应该是客户端证书密码）
            keyManagerFactory.init(keyStore, KEY_STORE_PASSWORD.toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(),trustManagerFactory.getTrustManagers(),new SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            okHttpClient.connectionSpecs(Collections.singletonList(spec))
                    .sslSocketFactory(sslSocketFactory, trustAllCert)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return true;
                        }
                    });

            return okHttpClient.build();

        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public synchronized static OkHttpClient getClient(){
        if (okHttpClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            try {
                // 自定义一个信任所有证书的TrustManager，添加SSLSocketFactory的时候要用到
                final X509TrustManager trustAllCert =
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                        };
                final SSLSocketFactory sslSocketFactory = new SSLSocketFactoryCompat(trustAllCert);
                builder.sslSocketFactory(sslSocketFactory, trustAllCert);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            okHttpClient = builder.build();
        }
        return okHttpClient;
    }
}
