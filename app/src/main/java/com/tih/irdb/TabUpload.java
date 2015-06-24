package com.tih.irdb;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.ConsumerIrManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by pichu on 西元15/6/17.
 */
public class TabUpload extends Fragment{

    ListView codeRecordlistView ;

    String tag = "TabUpload";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(tag, "onCreateView");
        View v =inflater.inflate(R.layout.tab_upload,container,false);

        codeRecordlistView = (ListView)v.findViewById(R.id.codeRecordlistView);
//        final String[] list = {"鉛筆","原子筆","鋼筆","毛筆","彩色筆"};
        Log.d(tag, "crl length: " + MainScreenActivity.codeRecordList.size());


        MainScreenActivity.listAdapter = new InfraredCodeRecordAdapter(getActivity(),R.layout.record_list,
                R.id.position,MainScreenActivity.codeRecordList);

        codeRecordlistView.setAdapter(MainScreenActivity.listAdapter);

        codeRecordlistView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                Log.d(tag, "onItemLongClick: " + position);

                final InfraredCodeRecord item = MainScreenActivity.codeRecordList.get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);


                final View dialogView = inflater.inflate(R.layout.edit_record_dialog, null, false);

                final EditText functionCode = (EditText) dialogView.findViewById(R.id.function_code);
                if (item.getFunctionCode() != null) {
                    functionCode.setText(item.getFunctionCode());
                }

                builder.setMessage("修改紀錄")
                        .setView(dialogView)
                        .setPositiveButton("關閉", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.d(tag, "functionCode: " + functionCode.getText().toString());
                                item.setFunctionCode(functionCode.getText().toString());
                                MainScreenActivity.listAdapter.notifyDataSetChanged();
                            }
                        });

                builder.show();
                ImageView ButtonReviewBackground = (ImageView) dialogView.findViewById(R.id.button_review_background);
                ImageView ButtonReviewMapDot = (ImageView) dialogView.findViewById(R.id.button_review_map_dot);
                ButtonReviewBackground.setImageBitmap(MainScreenActivity.currentControllerPhoto);

                Log.d(tag, "ButtonReviewBackground: " + ButtonReviewBackground);
                Log.d(tag, "ButtonReviewBackground.getWidth: " + ButtonReviewBackground.getWidth());
                Log.d(tag, "ButtonReviewBackground.getHeight: " + ButtonReviewBackground.getHeight());

                double ButtonReviewAspect = 1.0 * ButtonReviewBackground.getWidth() /
                        ButtonReviewBackground.getHeight();


                Log.d(tag, "BRA: " + ButtonReviewAspect);
                Bitmap dotLayer = Bitmap.createBitmap(MainScreenActivity.dotLayer.getWidth(),
                        MainScreenActivity.dotLayer.getHeight(), Bitmap.Config.ARGB_8888);


                Canvas canvas = new Canvas(dotLayer);
                Paint paint = new Paint();
                paint.setColor(Color.BLUE);
                paint.setStrokeWidth(2);

                canvas.drawCircle(item.getX() * dotLayer.getWidth(), item.getY() * dotLayer.getHeight(), 8, paint);

                ButtonReviewMapDot.setImageBitmap(dotLayer);

                return true;
            }
        });

        codeRecordlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                ConsumerIrManager irManager = (ConsumerIrManager) getActivity().getSystemService(Context.CONSUMER_IR_SERVICE);
                if (!irManager.hasIrEmitter()) {
                    Toast.makeText(getActivity(), "暫時不支援此設備之紅外線發射", Toast.LENGTH_SHORT).show();
                    return;
                }

                InfraredCodeRecord record = MainScreenActivity.codeRecordList.get(position);
                transmitCode(record);

                Toast.makeText(getActivity(), "發射測試訊號", Toast.LENGTH_SHORT).show();

            }

            private void transmitCode(InfraredCodeRecord record) {



               // ConsumerIrManager irManager = (ConsumerIrManager) getActivity().getSystemService(Context.CONSUMER_IR_SERVICE);
                String code = record.getCode();
                try {
                    JSONObject jsonObject = new JSONObject(code);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    int[] data = new int[jsonArray.length()];

                    for (int i = 0; i < data.length; ++i) {
                        data[i] = jsonArray.optInt(i);
                    }
                    Log.d(tag, "Transmit IR, freq: " + jsonObject.getDouble("freq") * 1000 + "data:aaa " + jsonArray.toString());
                    MainScreenActivity.irManager.transmit((int) (jsonObject.getDouble("freq") * 1000), data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }



        });



        Button submitRecords = (Button) v.findViewById(R.id.submit_records);
        submitRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AsyncTask<Void,Void,Void>(){

                    @Override
                    protected Void doInBackground(Void... params) {
                        for(int i=0;i<MainScreenActivity.codeRecordList.size();++i){
                            try {
                                submit(i);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        return null;
                    }
                }.execute();


            }
        });


        return v;
    }

    private boolean submit(int position) throws IOException {
        OutputStream os = null;

        String payload = MainScreenActivity.codeRecordList.get(position).toString();

        URL url = new URL(MainActivity.irdbUrl + MainActivity.addCodeUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        conn.setRequestProperty("Content-Length","" + payload.getBytes().length);
//            conn.setRequestProperty("Accept", "application/json");

        conn.setInstanceFollowRedirects(false);
        List<HttpCookie> cookies = MainActivity.cookieManager.getCookieStore().getCookies();
        if (MainActivity.cookieManager.getCookieStore().getCookies().size() > 0) {
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

        MainActivity.renewCookieManager(conn);
        return response == 302 && conn.getHeaderField("Location").contains("home");


    }

}
