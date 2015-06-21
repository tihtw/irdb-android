package com.tih.irdb;

import android.content.Context;
import android.content.Intent;
import android.hardware.ConsumerIrManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tih.tihir.ConsumerIrManagerCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;


public class MainActivity extends ActionBarActivity {

    static final String irdbUrl = "https://irdb.tih.tw";
    public static String addCodeUrl = "/api/addCode";
    final String registerUrl = "/auth/register";
    final String loginUrl = "/auth/login";

    final String tag = "MainActivity";

    static CookieManager cookieManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(tag, "MANUFACTURER:" + Build.MANUFACTURER);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Toast.makeText(this, "Network OK", Toast.LENGTH_SHORT).show();
            Log.d(tag, "Network OK");
            // fetch data
        } else {
            Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show();;
            // display error
            Log.d(tag, "Network Fail");
        }
        final ConsumerIrManagerCompat irManager = ConsumerIrManagerCompat.createInstance(this);

        irManager.start();
        Button register = (Button) findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(irdbUrl + registerUrl));
                startActivity(browser);
            }
        });

        Button login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = ((EditText)findViewById(R.id.email)).getText().toString();
                final String password = ((EditText)findViewById(R.id.password)).getText().toString();

                new AsyncTask<String, Void, Boolean>() {

                    @Override
                    protected Boolean doInBackground(String... params) {
                        try {
                            getXSRFToken();

                            return login(email,password);


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return false;
                    }

                    @Override
                    protected void onPostExecute(Boolean result){
                        if(result)Toast.makeText(MainActivity.this, "Login Success",Toast.LENGTH_SHORT).show();
                        else Toast.makeText(MainActivity.this,"Login Failure", Toast.LENGTH_SHORT).show();

                        if(result){

                            Log.d(tag, "Start Intent");
                            Intent intent = new Intent(MainActivity.this, MainScreenActivity.class);
                            startActivity(intent);
                            Log.d(tag, "Started Intent");
                            //intent.putExtra("cookie")


                        }
                    }
                }.execute("");

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private CookieManager getXSRFToken() throws IOException {
        URL url = new URL(irdbUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("HEAD");
        conn.setDoInput(true);
        conn.connect();
        int response = conn.getResponseCode();
        Log.d(tag, "Get XSRFToken response Code = " + response);

        renewCookieManager(conn);
        return cookieManager;
    }

    public static void renewCookieManager(HttpURLConnection conn){
        if(cookieManager == null)
            cookieManager = new CookieManager();
        Map<String, List<String>> headerFields = conn.getHeaderFields();
        List<String> cookiesHeader = headerFields.get("Set-Cookie");
        if(cookiesHeader != null){
            for(String cookie: cookiesHeader){
                cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                Log.d("renewCookieManager", cookie);
            }
        }
    }


    private boolean login(String email, String password) throws IOException {
        OutputStream os = null;

        String payload =  "email=" + URLEncoder.encode(email, "utf-8") +
                "&password=" + URLEncoder.encode(password, "utf-8");

        URL url = new URL(irdbUrl + loginUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length","" + payload.getBytes().length);
//            conn.setRequestProperty("Accept", "application/json");

        conn.setInstanceFollowRedirects(false);
        List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
        if (cookieManager.getCookieStore().getCookies().size() > 0) {
            Log.d(tag, "Send Cookie:" + TextUtils.join("; ", cookies));
            conn.setRequestProperty("Cookie",
                    TextUtils.join("; ", cookies));
        }
        for (HttpCookie cookie : cookies) {
            Log.d(tag, "name: " + cookie.getName() + " value: " + cookie.getValue());
            if (cookie.getName().equals("XSRF-TOKEN")) {
                conn.setRequestProperty("X-XSRF-TOKEN", URLDecoder.decode(cookie.getValue(), "utf-8"));
            }
        }
        //conn.setRequestProperty("X-XSRF-TOKEN",);

        conn.setDoInput(true);
        conn.setDoOutput(true);

        os = conn.getOutputStream();

        DataOutputStream request = new DataOutputStream(os);

        request.writeBytes(payload);
        request.flush();
        conn.connect();

        int response = conn.getResponseCode();
        Log.d(tag, "Get Login response Code = " + response);

        renewCookieManager(conn);
        return response == 302 && conn.getHeaderField("Location").contains("home");


    }

    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

}
