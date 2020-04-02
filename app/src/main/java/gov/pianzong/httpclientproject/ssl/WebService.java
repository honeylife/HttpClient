package gov.pianzong.httpclientproject.ssl;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * @CreateDate: 2020/3/31
 * @Author: honeylife
 * @Description:
 * @Version:
 */
public class WebService {
    //members
    private ClientConnectionManager clientConnectionManager;
    private HttpContext context;
    private HttpParams params;

    //constructor
    public WebService() {
        setup();
    }

    //	prepare for the https connection
//call this in the constructor of the class that does the connection if
//it's used multiple times
    private void setup() {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
// http scheme
// schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
// https scheme
        schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
        params = new BasicHttpParams();
        params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 1);
        params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(1));
        params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "utf8");
/*
CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//set the user credentials for our site "example.com"
credentialsProvider.setCredentials(new AuthScope("example.com", AuthScope.ANY_PORT),
new UsernamePasswordCredentials("UserNameHere", "UserPasswordHere"));
*/
        clientConnectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
        context = new BasicHttpContext();
//context.setAttribute("http.auth.credentials-provider", credentialsProvider);
    }


    public HttpResponse getResponseFromUrl(String url) {
//connection (client has to be created for every new connection)
        DefaultHttpClient client = new DefaultHttpClient(clientConnectionManager, params);
        HttpGet get = new HttpGet(url);
        HttpResponse response = null;
        try {
            response = client.execute(get, context);
            Log.e("TAG", "===" + response);
            Log.i("Response:", "" + response.toString());
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
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.getConnectionManager().shutdown();

        return response;
    }
}
