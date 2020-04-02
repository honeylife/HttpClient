package gov.pianzong.httpclientproject.network;


import android.database.CharArrayBuffer;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;



public class HttpUtil {

    private static final String TAG = "HttpUtil";
    private static final String UTF_8 = "UTF-8";
    private static final int BUFFER_SIZE = 4096;

    public static String get(RequestURL url) throws ClientProtocolException, UnknownHostException,
            ConnectTimeoutException, SocketTimeoutException, IOException, HttpException {
        return get(url.getEncode());
    }

    public static String post(RequestURL url) throws ClientProtocolException, UnknownHostException,
            ConnectTimeoutException, SocketTimeoutException, IOException, HttpException {
        DefaultHttpClient client = HttpFactory.createHttpClient();
        HttpRequestBase requestBase = createHttpPost(url);
        HttpResponse response = client.execute(requestBase);
        return entityToString(response);
    }

    private static HttpPost createHttpPost(RequestURL mRequestURL) {
        HttpPost post = new HttpPost(mRequestURL.getEncode());
        HttpEntity entiry = mRequestURL.postEntiry();
        post.setEntity(entiry);
        return post;
    }

    public static String get(String url) throws IOException, HttpException {
        return get(url, null);
    }

    public static String get(String url, HttpContext context) throws IOException, HttpException {
        return get(url, null, context);
    }

    public static byte[] getByte(String url) throws IOException, HttpException {
        DefaultHttpClient client = HttpFactory.createHttpClient();
        HttpGet get = new HttpGet(url);
        try {
            HttpResponse resonse = client.execute(get);
            return entityTobyte(resonse);
        } catch (UnknownHostException exception) {
            throw new UnknownHostException();
        } catch (ConnectTimeoutException e) {
            throw new ConnectTimeoutException();
        } catch (SocketTimeoutException e) {
            throw new SocketTimeoutException();
        } finally {
            get.abort();
            client.getConnectionManager().shutdown();
        }
    }

    public static boolean download(String url, File outFile) {
        DefaultHttpClient client = HttpFactory.createHttpClient();
        HttpGet get = new HttpGet(url);
        FileOutputStream fout = null;
        InputStream instream = null;
        try {
            HttpResponse resonse = client.execute(get);
            HttpEntity entity = resonse.getEntity();
            if (entity != null) {
                instream = entity.getContent();
                if (instream == null) {
                    return false;
                }
                byte[] buff = new byte[BUFFER_SIZE];
                fout = new FileOutputStream(outFile);

                int length = -1;
                while ((length = instream.read(buff)) != -1) {
                    fout.write(buff, 0, length);
                }
                return true;
            }
        } catch (UnknownHostException exception) {
        } catch (ConnectTimeoutException e) {
        } catch (SocketTimeoutException e) {
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (instream != null) {
                try {
                    instream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            get.abort();
            client.getConnectionManager().shutdown();
        }
        return false;
    }


    public static String get(String url, DefaultHttpClient client, HttpContext context) throws IOException, HttpException {
        if (client == null) {
            client = HttpFactory.createHttpClient();
        }
        HttpGet get = new HttpGet(url);
        try {
            HttpResponse resonse = client.execute(get, context);
            return entityToString(resonse);
        } catch (UnknownHostException exception) {
            throw new UnknownHostException();
        } catch (ConnectTimeoutException e) {
            throw new ConnectTimeoutException();
        } catch (SocketTimeoutException e) {
            throw new SocketTimeoutException();
        } finally {
            get.abort();
            client.getConnectionManager().shutdown();
        }
    }

    public static String entityToString(HttpResponse resonse) throws IOException, HttpException {
        HttpEntity entity = resonse.getEntity();
        if (entity != null) {
            String msg = null;
            try {
                msg = EntityUtils.toString(entity, UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int code = resonse.getStatusLine().getStatusCode();
            if (code == 200) {
                return msg;
            } else {
//                if (code >= 300 && code < 400) {
//                    throw new Http3xxException(code, msg);
//                } else if (code >= 400 && code < 500) {
//                    throw new Http4xxException(code, msg);
//                } else if (code >= 500) {
//                    throw new Http5xxException(code, msg);
//                }
                throw new HttpException(code + "-"+msg);
            }
        } else {
            throw new HttpException(HttpError.error_12);
        }
    }

    public static byte[] entityTobyte(HttpResponse resonse) throws IOException, HttpException {
        HttpEntity entity = resonse.getEntity();
        if (entity != null) {
            byte[] buffer = null;
            try {
                buffer = EntityUtils.toByteArray(entity);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int code = resonse.getStatusLine().getStatusCode();
            if (code == 200) {
                return buffer;
            } else {
                String errerMsg = new String(buffer, UTF_8);
                Log.w(TAG,"code=" + code + ",errerMsg=" + errerMsg);
//                if (code >= 300 && code < 400) {
//                    throw new Http3xxException(code, errerMsg);
//                } else if (code >= 400 && code < 500) {
//                    throw new Http4xxException(code, errerMsg);
//                } else if (code >= 500) {
//                    throw new Http5xxException(code, errerMsg);
//                }
                throw new HttpException(code + "");
            }
        }
        throw new HttpException(HttpError.error_12);
    }

    public static void writeCache(String fileDir, String fileName, String xml) throws UnsupportedEncodingException,
            IOException {
        File dir = new File(fileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File f = new File(dir, fileName);
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(f);
            fout.write(xml.getBytes(UTF_8));
        } finally {
            if (fout != null) {
                fout.close();
            }
        }
    }

    public static String readCache(String path, String defaultChareset) throws IOException {
        File cache = new File(path);
        if (!cache.exists()) {
            return null;
        } else {
            String chareset = defaultChareset;
            if (defaultChareset == null) {
                chareset = UTF_8;
            }
            InputStream in = null;
            try {
                in = new FileInputStream(cache);
                Reader reader = new InputStreamReader(in, chareset);
                CharArrayBuffer buffer = new CharArrayBuffer(BUFFER_SIZE);
                char[] tmp = new char[1024];
                int l;
                while ((l = reader.read(tmp)) != -1) {
//                    buffer.append(tmp, 0, l);
                }
                return buffer.toString();
            } finally {
                in.close();
            }
        }
    }

}
