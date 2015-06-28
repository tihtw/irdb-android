package com.tih.irdb;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.ConsumerIrManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.tih.tihir.ConsumerIrManagerCompat;
import com.tih.tihir.ConsumerIrManagerHtc;
import com.tih.tihir.ConsumerIrManagerIRKit;

import java.io.File;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pichu on 西元15/6/17.
 */
public class MainScreenActivity extends ActionBarActivity {

    CookieManager cookieManager;

    static Bitmap currentControllerPhoto = null;
    static Bitmap dotLayer = null;
    static Double aspectRatio;

    static List<InfraredCodeRecord> codeRecordList;

    static ConsumerIrManagerCompat irReceiverManager;
    static ConsumerIrManagerCompat irTransferManager;
    static int irLearnRetry;

    Toolbar toolbar;
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[]={"學習", "上傳" ,"設定"};
    int Numboftabs =3;

    static ArrayAdapter listAdapter;




    final String tag = "MainScreenActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        //irManager = ConsumerIrManagerCompat.createInstance(this);
        irReceiverManager = new ConsumerIrManagerHtc(this);
        irTransferManager = new ConsumerIrManagerHtc(this);
        irReceiverManager.start();
        irTransferManager.start();

        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        if(codeRecordList == null)
            codeRecordList = new ArrayList<>();
        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter =  new ViewPagerAdapter(getSupportFragmentManager(),Titles,Numboftabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
//        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
//            @Override
//            public int getIndicatorColor(int position) {
//                return getResources().getColor(R.color.tabsScrollColor);
//            }
//        });

        // Setting the ViewPager For the SlidingTabsLayout

        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {



            }

            @Override
            public void onPageSelected(int position) {
                Log.d(tag, "onPageSelected, Position = " + position);
                if(position == 1){
                    listAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d(tag, "onPageScrollStateChanged");

            }
        });

        tabs.setViewPager(pager);




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d(tag, "requestCode: " +requestCode);
        Log.d(tag, "resultCode: " + resultCode);
        if (resultCode == RESULT_OK) {
            Log.d(tag, "RESULT_OK");
            File tmpFile = new File(
                    Environment.getExternalStorageDirectory(),
                    "irdb/currentController.jpg");
            Uri outputFileUri = Uri.fromFile(tmpFile);

            currentControllerPhoto = BitmapFactory.decodeFile(outputFileUri.getPath());
            aspectRatio = 1.0 * currentControllerPhoto.getWidth() /
                    currentControllerPhoto.getHeight();

//            //取出拍照後回傳資料
//            Bundle extras = data.getExtras();
//            //將資料轉換為圖像格式
//            currentControllerPhoto = (Bitmap) extras.get("data");
            codeRecordList.clear();
            listAdapter.notifyDataSetChanged();
            ImageView iv = (ImageView)findViewById(R.id.controllerPhoto);
            iv.setImageBitmap(MainScreenActivity.currentControllerPhoto);

            Log.d(tag, "Width: " + currentControllerPhoto.getWidth() + " Height: " + currentControllerPhoto.getHeight());

            dotLayer = Bitmap.createBitmap(currentControllerPhoto.getWidth(),currentControllerPhoto.getHeight(), Bitmap.Config.ARGB_8888);

            ImageView controllerPhotoDots = (ImageView)findViewById(R.id.controllerPhotoDots);
            controllerPhotoDots.setImageBitmap(dotLayer );
            if(currentControllerPhoto == null){
                Log.d(tag,"Huh?");
            }
        }


        super.onActivityResult(requestCode, resultCode, data);


    }


    @Override
    protected void onResume() {
        super.onResume();
        irTransferManager.start();
        irReceiverManager.start();
    }
    @Override
    protected void onPause() {
        super.onPause();
        irTransferManager.stop();
        irReceiverManager.stop();
    }

}
