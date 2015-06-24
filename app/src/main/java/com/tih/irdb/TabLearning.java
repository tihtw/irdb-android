package com.tih.irdb;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.htc.circontrol.CIRControl;
import com.htc.htcircontrol.HtcIrData;
import com.tih.tihir.ConsumerIrManagerCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by pichu on 西元15/6/17.
 */
public class TabLearning extends Fragment{

    final String tag = "TabLearning";
    ImageView controllerPhoto;
    ImageView controllerPhotoDots;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(tag,"onCreateView");
        View v =inflater.inflate(R.layout.tab_learning,container,false);

        controllerPhoto = (ImageView)v.findViewById(R.id.controllerPhoto);
        controllerPhotoDots = (ImageView)v.findViewById(R.id.controllerPhotoDots);

        if(MainScreenActivity.currentControllerPhoto != null){
            Log.d(tag, "Set Bitmap");
            controllerPhoto.setImageBitmap(MainScreenActivity.currentControllerPhoto);
            controllerPhotoDots.setImageBitmap(MainScreenActivity.dotLayer);
        }else{
            Log.d(tag,"currentControllerPhoto == null");
        }
        Button btnTakePhoto = (Button)v.findViewById(R.id.takePhoto);
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

//        controllerPhoto.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                Log.d(tag, "controllerPhoto has been touch");
//
//                return false;
//            }
//        });


        controllerPhotoDots.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(MainScreenActivity.currentControllerPhoto == null){

                    Toast.makeText(getActivity(),"Take photo first.", Toast.LENGTH_SHORT).show();
                    takePhoto();
                    return false;
                }

                Log.d(tag,"controllerPhotoDots has been touch");
                float pointX = event.getX();
                float pointY = event.getY();

                Log.d("Pichu", "OnTouch, Event: " + event.getAction() + "" + pointX + "," + pointY);

                final float bh = MainScreenActivity.dotLayer.getHeight();
                final float bw = MainScreenActivity.dotLayer.getWidth();

                float ih = controllerPhotoDots.getMeasuredHeight();
                final float iw = controllerPhotoDots.getMeasuredWidth();


                final float scaleRate, bhp, bwp, pointXp, pointYp;

                if (ih * bw < iw * bh) {
                    Log.d(tag, "I Mode");
                    bhp = ih;
                    scaleRate = ih / bh;
                    bwp = bw * scaleRate;
                    if(pointX < (iw - bwp)/2 || pointX > (iw + bwp)/2){
                        Log.d(tag, "Out of range");
                        return false;
                    }
                }else{
                    Log.d(tag, "H Mode");
                    bwp = iw;
                    scaleRate = iw / bw;
                    bhp = bh * scaleRate;
                    if(pointY < (ih - bhp)/2 || pointY > (ih + bhp)/2){
                        Log.d(tag, "Out of range");
                        return false;
                    }
                }

                pointXp = (pointX - (iw - bwp)/ 2) / scaleRate;
                pointYp = (pointY - (ih - bhp)/2) / scaleRate;




                MainScreenActivity.irManager.learnIRCmd(10);

                Toast.makeText(getActivity(), "學習中", Toast.LENGTH_LONG).show();

                MainScreenActivity.irLearnRetry = 0;

                MainScreenActivity.irManager.setOnLearnListener(new ConsumerIrManagerCompat.OnLearnListener() {

                    @Override
                    public void onLearn(String code) {

                        Log.d(tag, "Code String: " +code);

                        Canvas canvas = new Canvas(MainScreenActivity.dotLayer);
                        Paint paint = new Paint();
                        paint.setColor(Color.BLUE);
                        paint.setStrokeWidth(10 / scaleRate);

                        canvas.drawCircle(pointXp, pointYp, 5 / scaleRate, paint);
                        MainScreenActivity.codeRecordList.add(
                                new InfraredCodeRecord(pointXp / bw, pointYp / bh, code));

                        controllerPhotoDots.setImageBitmap(MainScreenActivity.dotLayer);

                    }


                    @Override
                    public void onError(String errorCode) {
                        if(MainScreenActivity.irLearnRetry < 3) {
                            ++MainScreenActivity.irLearnRetry;
                            if(errorCode.equals(""+CIRControl.ERR_LEARNING_TIMEOUT)){
                                Toast.makeText(getActivity(), "逾時未收到信號，停止學習。",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }else {
                                Toast.makeText(getActivity(), "發生錯誤，重新學習，錯誤代碼：" +
                                        errorCode + "，可能是遭受干擾。", Toast.LENGTH_SHORT).show();
                            }
                            MainScreenActivity.irManager.learnIRCmd(10);
                        }else{
                            Toast.makeText(getActivity(), "超過3次，放棄。", Toast.LENGTH_LONG).show();
                        }
                    }
                });



                return false;
            }
        });

        return v;
    }



    private void takePhoto(){
        Intent intent_camera = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        File tmpFile = new File(
                Environment.getExternalStorageDirectory(),
                "irdb/currentController.jpg");
        Uri outputFileUri = Uri.fromFile(tmpFile);
        intent_camera.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(intent_camera, 0);
    }


}
