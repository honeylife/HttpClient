package gov.pianzong.httpclientproject.ui;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import gov.pianzong.httpclientproject.R;
import gov.pianzong.httpclientproject.network.NetworkTool;

public class MainActivity extends AppCompatActivity {

    //    private final static String PATH="https://adopt.xg.tagtic.cn/ads?adcode=802&adplatform=11&adtype=1&appid=1107758753&codeid=2060659049797511&dpid=62fcf47d7d91eb0a&imei=352097090412990&mac=DC%3AEF%3ACA%3A83%3A33%3AF1&pkgname=com.wanmei.a9vg&pkgver=100&ua=Mozilla%2F5.0+%28Linux%3B+Android+9%3B+SM-G9550+Build%2FPPR1.180610.011%3B+wv%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Chrome%2F74.0.3729.136+Mobile+Safari%2F537.36";
    private final static String PATH = "https://adopt.xg.tagtic.cn/init?sdkver=5";
    //    private final static String PATH="https://47.105.163.90";
    private final static String PATH1 = "https://appapi.anmirror.cn/time";
    private TextView mContentView;
    private DnTask dnTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContentView = findViewById(R.id.textView2);
//        dnTask = new DnTask();
//        dnTask.setTaskHandler(new HttpTaskHandler(){
//
//            @Override
//            public void taskSuccessful(String json) {
//                Toast.makeText(MainActivity.this,"请求成功",Toast.LENGTH_LONG).show();
//                mContentView.setText(json);
//
//            }
//
//            @Override
//            public void taskFailed() {
//                Toast.makeText(MainActivity.this,"请求出错",Toast.LENGTH_LONG).show();
//            }
//        });
    }

    /**
     * 点击按钮发送网络请求，这里就用原生的异步任务写了。
     */
    public void onClicks(View v) {
        Toast.makeText(this, "这里暂时不发网络请求", Toast.LENGTH_LONG).show();
        dnTask.execute(PATH1);
    }

    /**
     * 点击按钮发送网络请求，这里就用原生的异步任务写了。
     */
    public void onClickss(View v) {
        Toast.makeText(this, "发送网络请求", Toast.LENGTH_LONG).show();
        dnTask = new DnTask();
        dnTask.setTaskHandler(new HttpTaskHandler() {

            @Override
            public void taskSuccessful(String json) {
                Toast.makeText(MainActivity.this, "请求成功", Toast.LENGTH_LONG).show();
                mContentView.setText(json);

            }

            @Override
            public void taskFailed() {
                Toast.makeText(MainActivity.this, "请求出错", Toast.LENGTH_LONG).show();
            }
        });
        dnTask.execute(PATH);
//        new OKHttpUtils().get(PATH);
    }

    public interface HttpTaskHandler {
        void taskSuccessful(String json);

        void taskFailed();
    }

    public static class DnTask extends AsyncTask<String, String, String> {
        private static final String TAG = "DnTask";

        private HttpTaskHandler taskHandler;

        @Override
        protected String doInBackground(String... params) {
            // Performed on Background Thread
            String url = params[0];
            try {
                String data = new NetworkTool().getData(url);
//               String data= new NetworkTool().requestHTTPSPage(url);
//             String data=  HttpClientUtil.doGet(url);
//             String data=  HttpClientUtil.get(PATH);;


//              String data=  MyX509TrustManager.httpsRequest(PATH, "GET", null);

//                Test test = new Test();
//                test.run();
//              String data=  HttpClientHelper.requestHTTPSPage(url);

//                new WebService().getResponseFromUrl(PATH);

//                String data = HttpUrlConnectionUtil.getRequest(PATH);

                return data;
            } catch (Exception e) {
                // TODO handle different exception cases
                Log.e(TAG, e.toString());
                e.printStackTrace();
                return "数据出错啦！！";
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String json) {
            super.onPostExecute(json);
            if (json != null && !json.equals("")) {
                Log.d(TAG, "taskSuccessful");
                taskHandler.taskSuccessful(json);
            } else {
                Log.d(TAG, "taskFailed");
                taskHandler.taskFailed();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        private void setTaskHandler(HttpTaskHandler taskHandler) {
            this.taskHandler = taskHandler;
        }

        public void data() {

        }
    }


}

