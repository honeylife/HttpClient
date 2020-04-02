package gov.pianzong.httpclientproject.base;

import android.app.Application;
import android.support.multidex.MultiDex;


/**
 * @CreateDate: 2020/3/26
 * @Author: honeylife
 * @Description: 自定义Application
 * @Version:
 */
public class DnApplication extends Application {
    private static DnApplication mDnApplication;
    public DnApplication(){
        mDnApplication=this;
    }
    public static DnApplication getInstance(){
        return mDnApplication;
    }
    @Override
    public void onCreate() {
        super.onCreate();

//        SSLContext sslContext = null;
//        try {
//            ProviderInstaller.installIfNeeded(getApplicationContext());
//            sslContext = SSLContext.getInstance("TLSv1.1");
//            sslContext.init(null, null, null);
//            SSLEngine engine = sslContext.createSSLEngine();
//
//        } catch (NoSuchAlgorithmException | GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
//            e.printStackTrace();
//        } catch (KeyManagementException e) {
//            e.printStackTrace();
//        }
//        SSLContext sslContext = null;
//
//        try {
//            sslContext = SSLContext.getInstance("TLS");
//
//            sslContext.init(null, null, null);
//            String[] protocols = sslContext.getSupportedSSLParameters().getProtocols();
//            for (String protocol : protocols) {
//                Log.e("TAG","--Context supported protocol: " + protocol);
//            }
//
//            SSLEngine engine = sslContext.createSSLEngine();
//            String[] supportedProtocols = engine.getSupportedProtocols();
//            for (String protocol : supportedProtocols) {
//                Log.e("TAG","--Engine supported protocol: " + protocol);
//            }
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//
//    } catch (KeyManagementException e) {
//        e.printStackTrace();
//    }
        MultiDex.install(this);

    }
}
