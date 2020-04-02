package gov.pianzong.httpclientproject.urlConnection;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @CreateDate: 2020/3/30
 * @Author: honeylife
 * @Description:
 * @Version:
 */
public class MyX509TrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        // TODO Auto-generated method stub

    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        // TODO Auto-generated method stub

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        // TODO Auto-generated method stub
        return null;
    }

    // 处理http请求 requestUrl为请求地址 requestMethod请求方式，值为"GET"或"POST"
    public static String httpRequest(String requestUrl, String requestMethod,
                                     String outputStr) {

        StringBuffer buffer = null;
        try {
            URL url = new URL(requestUrl);// 请求地址
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();// 创建连接对象
            conn.setDoOutput(true);// 是否输出
            conn.setDoInput(true);// 是否输入
            conn.setRequestMethod(requestMethod);// post Or get
            conn.setRequestProperty("Charset", "utf-8");
            conn.setFollowRedirects(false);
            conn.setInstanceFollowRedirects(false);
            conn.connect();// 发起连接
            // 往服务器端写内容 也就是发起http请求需要带的参数
            if (null != outputStr) {
                OutputStream os = conn.getOutputStream();
                os.write(outputStr.getBytes("utf-8"));
                os.close();
            }

            // 读取服务器端返回的内容
            // PrintWriter out = new PrintWriter(new
            // OutputStreamWriter(conn.getOutputStream(),"utf-8"));
            // out.println(obj);

            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            buffer = new StringBuffer();
            String line = null;
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    /*
     * 处理https GET/POST请求 请求地址、请求方法、参数
     */
    public static String httpsRequest(String requestUrl, String requestMethod,
                                      String outputStr) {
        StringBuffer buffer = null;
        try {
            // 创建SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] tm = { new MyX509TrustManager() };
            // 初始化
            sslContext.init(null, tm, new java.security.SecureRandom());
            ;
            // 获取SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            URL url = new URL(requestUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod(requestMethod);
            // 设置当前实例使用的SSLSoctetFactory
            conn.setSSLSocketFactory(ssf);
            conn.connect();
            // 往服务器端写内容
            if (null != outputStr) {
                OutputStream os = conn.getOutputStream();
                os.write(outputStr.getBytes("utf-8"));
                os.close();
            }
            // 读取服务器端返回的内容
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "gbk");
            BufferedReader br = new BufferedReader(isr);
            buffer = new StringBuffer();
            String line = null;
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    public static void main(String[] args) {
        //测试


        new Thread(){
            @Override
            public void run() {
                super.run();
                String url = "https://adopt.xg.tagtic.cn/init?sdkver=5";
                String s = httpsRequest(url, "GET", null);
                System.out.println(s);
            }
        }.start();
    }
}
