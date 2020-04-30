# HttpClient
兼容HTTPClient  访问HTTPS协议，支持SNI

# 探究 https 自签名的双向校验
### 主要实现：
* 1、okhttp3 
* 2、HttpURLConnection
* 3、HTTPClient
 > 主要头疼的是HTTPClient 的实现，毕竟废弃了这么长时间,要兼容Android4.4以下的手机，还是比较费时的。
 ## 第一 okhttp3 
 ```
   OkHttpClient okHttpClient = new OkHttpClient.Builder().
                sslSocketFactory(new SSLSocketFactoryCompat(trustAllCert), trustAllCert)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();
 ```

> okhttp设置sslSocketFactory就可以，当然点进去看方法里面的参数，它要什么我们就给它传什么，如：

源码的解释如下：
```
/**
     * Sets the socket factory and trust manager used to secure HTTPS connections. If unset, the
     * system defaults will be used.
     *
     * <p>Most applications should not call this method, and instead use the system defaults. Those
     * classes include special optimizations that can be lost if the implementations are decorated.
     *
     * <p>If necessary, you can create and configure the defaults yourself with the following code:
     *
     * <pre>   {@code
     *
     *   TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
     *       TrustManagerFactory.getDefaultAlgorithm());
     *   trustManagerFactory.init((KeyStore) null);
     *   TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
     *   if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
     *     throw new IllegalStateException("Unexpected default trust managers:"
     *         + Arrays.toString(trustManagers));
     *   }
     *   X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
     *
     *   SSLContext sslContext = SSLContext.getInstance("TLS");
     *   sslContext.init(null, new TrustManager[] { trustManager }, null);
     *   SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
     *
     *   OkHttpClient client = new OkHttpClient.Builder()
     *       .sslSocketFactory(sslSocketFactory, trustManager)
     *       .build();
     * }</pre>
     */
    public Builder sslSocketFactory(
        SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
      if (sslSocketFactory == null) throw new NullPointerException("sslSocketFactory == null");
      if (trustManager == null) throw new NullPointerException("trustManager == null");
      this.sslSocketFactory = sslSocketFactory;
      this.certificateChainCleaner = CertificateChainCleaner.get(trustManager);
      return this;
    }
```
> 37行到53行就是告诉你怎么做的，看着api自己定义即可。
#### 下面附上我的自定义SSLSocketFactory文件：
 > 工程中找SSLSocketFactory.java文件。
